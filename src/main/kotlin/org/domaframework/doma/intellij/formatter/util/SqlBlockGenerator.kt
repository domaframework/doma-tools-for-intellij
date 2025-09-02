/*
 * Copyright Doma Tools Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.domaframework.doma.intellij.formatter.util

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.psi.util.nextLeaf
import com.intellij.psi.util.nextLeafs
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictClauseBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlExistsGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlSecondOptionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlFromGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlSecondKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlTableModifySecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlWhereGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlDeleteQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlJoinQueriesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlSelectQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTableModificationKeyword
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.other.SqlEscapeBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlAliasBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlArrayWordBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlFunctionGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlTableBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlWordBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.handler.CommaRawClauseHandler
import org.domaframework.doma.intellij.formatter.handler.CreateClauseHandler
import org.domaframework.doma.intellij.formatter.handler.InsertClauseHandler
import org.domaframework.doma.intellij.formatter.handler.JoinClauseHandler
import org.domaframework.doma.intellij.formatter.handler.NotQueryGroupHandler
import org.domaframework.doma.intellij.formatter.handler.UpdateClauseHandler
import org.domaframework.doma.intellij.formatter.handler.WithClauseHandler
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlTypes

data class SqlBlockFormattingContext(
    val wrap: Wrap?,
    val alignment: Alignment?,
    val spacingBuilder: SpacingBuilder,
    val enableFormat: Boolean,
    val formatMode: FormattingMode,
)

class SqlBlockGenerator(
    sqlBlock: SqlBlock,
    enableFormat: Boolean,
    formatMode: FormattingMode,
) {
    val sqlBlockFormattingCtx =
        SqlBlockFormattingContext(
            wrap = sqlBlock.wrap,
            alignment = sqlBlock.alignment,
            spacingBuilder = sqlBlock.spacingBuilder,
            enableFormat = enableFormat,
            formatMode = formatMode,
        )

    private val keywordBlockFactory = SqlKeywordBlockFactory(sqlBlockFormattingCtx)

    fun getKeywordBlock(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        val keywordText = child.text.lowercase()
        val indentLevel = SqlKeywordUtil.getIndentType(keywordText)

        return if (indentLevel.isNewLineGroup()) {
            getKeywordGroupBlock(indentLevel, keywordText, child, lastGroupBlock)
        } else {
            getSimpleKeywordBlock(indentLevel, keywordText, child, lastGroupBlock)
        }
    }

    private fun getSimpleKeywordBlock(
        indentLevel: IndentType,
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        when (indentLevel) {
            IndentType.INLINE -> keywordBlockFactory.createInlineBlock(child, lastGroupBlock)
            IndentType.ATTACHED -> keywordBlockFactory.createAttachedBlock(child, lastGroupBlock)
            IndentType.OPTIONS -> keywordBlockFactory.createOptionsBlock(keywordText, child, lastGroupBlock)
            IndentType.CONFLICT -> keywordBlockFactory.createConflictBlock(keywordText, child, lastGroupBlock)
            else -> SqlKeywordBlock(child, indentLevel, sqlBlockFormattingCtx)
        }

    private fun getKeywordGroupBlock(
        indentLevel: IndentType,
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        when (indentLevel) {
            IndentType.JOIN ->
                JoinClauseHandler.getJoinKeywordGroupBlock(
                    lastGroupBlock,
                    keywordText,
                    child,
                    sqlBlockFormattingCtx,
                )

            IndentType.INLINE_SECOND -> SqlInlineSecondGroupBlock(child, sqlBlockFormattingCtx)

            IndentType.TOP -> getTopLevelKeywordBlock(keywordText, child, indentLevel)

            IndentType.SECOND -> getSecondLevelKeywordBlock(keywordText, child, lastGroupBlock)

            IndentType.SECOND_OPTION -> getSecondOptionKeywordBlock(keywordText, child, lastGroupBlock)

            IndentType.CONFLICT ->
                if (lastGroupBlock is SqlConflictClauseBlock) {
                    SqlKeywordBlock(child, indentLevel, sqlBlockFormattingCtx)
                } else {
                    SqlKeywordGroupBlock(child, indentLevel, sqlBlockFormattingCtx)
                }

            else -> SqlKeywordGroupBlock(child, indentLevel, sqlBlockFormattingCtx)
        }

    private fun getTopLevelKeywordBlock(
        keywordText: String,
        child: ASTNode,
        indentLevel: IndentType,
    ): SqlBlock =
        when (keywordText) {
            "with" -> SqlWithQueryGroupBlock(child, sqlBlockFormattingCtx)
            "select" -> SqlSelectQueryGroupBlock(child, sqlBlockFormattingCtx)
            "create" -> SqlCreateKeywordGroupBlock(child, sqlBlockFormattingCtx)
            "insert" -> SqlInsertQueryGroupBlock(child, sqlBlockFormattingCtx)
            "do" -> SqlDoGroupBlock(child, sqlBlockFormattingCtx)
            "update" -> SqlUpdateQueryGroupBlock(child, sqlBlockFormattingCtx)
            "delete" -> SqlDeleteQueryGroupBlock(child, sqlBlockFormattingCtx)
            "union", "intersect", "except" -> SqlJoinQueriesGroupBlock(child, sqlBlockFormattingCtx)
            else -> SqlKeywordGroupBlock(child, indentLevel, sqlBlockFormattingCtx)
        }

    private fun getSecondLevelKeywordBlock(
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        when (keywordText) {
            "set" -> processSetKeyword(child, lastGroupBlock)
            "from" -> processFromKeyword(child, lastGroupBlock)
            "where" -> SqlWhereGroupBlock(child, sqlBlockFormattingCtx)
            "values" -> SqlValuesGroupBlock(child, sqlBlockFormattingCtx)
            else -> processDefaultSecondKeyword(child, lastGroupBlock)
        }

    private fun processSetKeyword(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        if (lastGroupBlock is SqlUpdateQueryGroupBlock) {
            SqlUpdateSetGroupBlock(child, sqlBlockFormattingCtx)
        } else {
            WithClauseHandler.getWithClauseKeywordGroup(lastGroupBlock, child, sqlBlockFormattingCtx)
                ?: SqlSecondKeywordBlock(child, sqlBlockFormattingCtx)
        }

    private fun processFromKeyword(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        if (lastGroupBlock is SqlSubGroupBlock) {
            SqlKeywordBlock(child, IndentType.ATTACHED, sqlBlockFormattingCtx)
        } else {
            SqlFromGroupBlock(child, sqlBlockFormattingCtx)
        }

    private fun processDefaultSecondKeyword(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        WithClauseHandler
            .getWithClauseKeywordGroup(lastGroupBlock, child, sqlBlockFormattingCtx)
            ?.let { return it }

        NotQueryGroupHandler
            .getKeywordGroup(child, sqlBlockFormattingCtx)
            ?.let { return it }

        return if (lastGroupBlock is SqlFunctionGroupBlock) {
            SqlKeywordBlock(child, IndentType.NONE, sqlBlockFormattingCtx)
        } else {
            SqlSecondKeywordBlock(child, sqlBlockFormattingCtx)
        }
    }

    private fun getSecondOptionKeywordBlock(
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        when {
            keywordText == "on" && !isJoinRelated(lastGroupBlock) ->
                SqlConflictClauseBlock(child, sqlBlockFormattingCtx)
            SqlKeywordUtil.isConditionKeyword(keywordText) ->
                SqlConditionKeywordGroupBlock(child, sqlBlockFormattingCtx)
            else -> SqlSecondOptionKeywordGroupBlock(child, sqlBlockFormattingCtx)
        }

    private fun isJoinRelated(lastGroupBlock: SqlBlock?): Boolean =
        lastGroupBlock is SqlJoinGroupBlock || lastGroupBlock?.parentBlock is SqlJoinGroupBlock

    fun getSubGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
        groups: List<SqlBlock>,
    ): SqlBlock {
        val ignoreConditionLoopLastBlock = groups.lastOrNull { it !is SqlElConditionLoopCommentBlock }

        return when (lastGroup) {
            is SqlKeywordGroupBlock -> processKeywordGroupBlock(lastGroup, child, ignoreConditionLoopLastBlock)
            is SqlColumnDefinitionRawGroupBlock -> SqlDataTypeParamBlock(child, sqlBlockFormattingCtx)
            else -> processDefaultSubGroup(lastGroup, child, ignoreConditionLoopLastBlock)
        }
    }

    private fun processKeywordGroupBlock(
        lastGroup: SqlKeywordGroupBlock,
        child: ASTNode,
        ignoreConditionLoopLastBlock: SqlBlock?,
    ): SqlBlock {
        CreateClauseHandler
            .getCreateTableClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?.let { return it }

        WithClauseHandler
            .getWithClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?.let { return it }

        InsertClauseHandler
            .getInsertClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?.let { return it }

        UpdateClauseHandler
            .getUpdateClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?.let { return it }

        NotQueryGroupHandler
            .getSubGroup(ignoreConditionLoopLastBlock, child, sqlBlockFormattingCtx)
            ?.let { return it }

        return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
    }

    private fun processDefaultSubGroup(
        lastGroup: SqlBlock?,
        child: ASTNode,
        ignoreConditionLoopLastBlock: SqlBlock?,
    ): SqlBlock {
        if (lastGroup is SqlSubGroupBlock) {
            WithClauseHandler
                .getWithClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                ?.let { return it }
        }

        NotQueryGroupHandler
            .getSubGroup(ignoreConditionLoopLastBlock, child, sqlBlockFormattingCtx)
            ?.let { return it }

        NotQueryGroupHandler
            .getSubGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?.let { return it }

        return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
    }

    fun getCommaGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock =
        CreateClauseHandler.getColumnRawGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?: CommaRawClauseHandler.getCommaBlock(lastGroup, child, sqlBlockFormattingCtx)

    fun hasEscapeBeforeWhiteSpace(
        lastEscapeBlock: SqlBlock?,
        start: ASTNode,
    ): Boolean {
        if (!isEscapeActive(lastEscapeBlock)) return false

        return findEscapeEndCharacter(start)
    }

    private fun isEscapeActive(lastEscapeBlock: SqlBlock?): Boolean =
        lastEscapeBlock != null && (lastEscapeBlock as? SqlEscapeBlock)?.isEndEscape != true

    private fun findEscapeEndCharacter(start: ASTNode): Boolean {
        var node = start.treeNext
        while (node != null) {
            when {
                isEscapeEndCharacter(node) -> return true
                node.psi is PsiWhiteSpace -> return false
            }
            node = node.treeNext
        }
        return false
    }

    private fun isEscapeEndCharacter(node: ASTNode): Boolean = node.elementType == SqlTypes.OTHER && node.text in setOf("\"", "`", "]")

    fun getFunctionName(
        child: ASTNode,
        defaultFormatCtx: SqlBlockFormattingContext,
    ): SqlBlock? {
        val hasLeftParen = isFollowedByLeftParen(child)
        return if (hasLeftParen) SqlFunctionGroupBlock(child, defaultFormatCtx) else null
    }

    private fun isFollowedByLeftParen(child: ASTNode): Boolean {
        val notWhiteSpaceElement =
            child.psi.nextLeafs
                .takeWhile { it is PsiWhiteSpace }
                .lastOrNull()
                ?.nextLeaf(true)

        return notWhiteSpaceElement?.elementType == SqlTypes.LEFT_PAREN ||
            PsiTreeUtil.nextLeaf(child.psi)?.elementType == SqlTypes.LEFT_PAREN
    }

    fun getWordBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        if (child.text.lowercase() == "array") {
            return SqlArrayWordBlock(child, sqlBlockFormattingCtx)
        }

        return processWordByContext(lastGroup, child)
    }

    private fun processWordByContext(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        when (lastGroup) {
            is SqlKeywordGroupBlock ->
                getKeywordGroupWordBlock(lastGroup, child)
                    ?.let { return it }

            is SqlCreateTableColumnDefinitionGroupBlock ->
                return SqlCreateTableColumnDefinitionRawGroupBlock(child, sqlBlockFormattingCtx)

            is SqlColumnDefinitionRawGroupBlock -> {
                if (lastGroup.columnBlock == null) {
                    return SqlColumnBlock(child, sqlBlockFormattingCtx)
                }
            }
        }

        return getDefaultWordBlock(lastGroup, child)
    }

    private fun getKeywordGroupWordBlock(
        lastGroup: SqlKeywordGroupBlock,
        child: ASTNode,
    ): SqlBlock? =
        when {
            SqlKeywordUtil.isBeforeTableKeyword(lastGroup.getNodeText()) ->
                SqlTableBlock(child, sqlBlockFormattingCtx)
            lastGroup is SqlWithQueryGroupBlock ->
                SqlWithCommonTableGroupBlock(child, sqlBlockFormattingCtx)
            lastGroup is SqlInsertQueryGroupBlock ->
                SqlTableBlock(child, sqlBlockFormattingCtx)
            else -> null
        }

    private fun getDefaultWordBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        if (lastGroup is SqlFromGroupBlock || lastGroup?.parentBlock is SqlFromGroupBlock) {
            return SqlAliasBlock(child, sqlBlockFormattingCtx)
        }

        return getFunctionName(child, sqlBlockFormattingCtx) ?: SqlWordBlock(child, sqlBlockFormattingCtx)
    }

    private fun createTableModificationBlock(
        lastGroupBlock: SqlBlock?,
        child: ASTNode,
    ): SqlBlock =
        if (shouldCreateTableModificationKeyword(lastGroupBlock)) {
            SqlTableModificationKeyword(child, sqlBlockFormattingCtx)
        } else {
            SqlTableModifySecondGroupBlock(child, sqlBlockFormattingCtx)
        }

    private fun shouldCreateTableModificationKeyword(lastGroupBlock: SqlBlock?): Boolean =
        lastGroupBlock !is SqlColumnRawGroupBlock &&
            lastGroupBlock !is SqlTableModificationKeyword

    private fun createSecondOptionBlock(
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        when {
            isOnKeywordForNonJoin(keywordText, lastGroupBlock) -> {
                handleOnKeyword(child, lastGroupBlock)
            }
            SqlKeywordUtil.isConditionKeyword(keywordText) -> {
                createConditionBlock(child, lastGroupBlock)
            }
            else -> SqlSecondOptionKeywordGroupBlock(child, sqlBlockFormattingCtx)
        }

    private fun isOnKeywordForNonJoin(
        keywordText: String,
        lastGroupBlock: SqlBlock?,
    ): Boolean =
        keywordText == "on" &&
            lastGroupBlock !is SqlJoinGroupBlock &&
            lastGroupBlock?.parentBlock !is SqlJoinGroupBlock

    private fun handleOnKeyword(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        val rootBlock = getRootBlock(lastGroupBlock)
        return when {
            rootBlock is SqlTableModifySecondGroupBlock ||
                rootBlock is SqlTableModificationKeyword -> {
                SqlTableModifySecondGroupBlock(child, sqlBlockFormattingCtx)
            }
            rootBlock is SqlExistsGroupBlock -> {
                SqlKeywordBlock(child, IndentType.ATTACHED, sqlBlockFormattingCtx)
            }
            else -> SqlConflictClauseBlock(child, sqlBlockFormattingCtx)
        }
    }

    private fun getRootBlock(lastGroupBlock: SqlBlock?): SqlBlock? =
        if (lastGroupBlock is SqlElConditionLoopCommentBlock) {
            lastGroupBlock.tempParentBlock
        } else {
            lastGroupBlock
        }

    private fun createConditionBlock(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        if (lastGroupBlock is SqlCreateKeywordGroupBlock) {
            SqlKeywordBlock(child, IndentType.ATTACHED, sqlBlockFormattingCtx)
        } else {
            SqlConditionKeywordGroupBlock(child, sqlBlockFormattingCtx)
        }

    fun getBlockCommentBlock(
        child: ASTNode,
        blockCommentSpacingBuilder: SqlCustomSpacingBuilder?,
    ): SqlCommentBlock =
        when {
            hasChildComment(child) -> SqlBlockCommentBlock(child, sqlBlockFormattingCtx)
            isConditionOrLoopDirective(child) ->
                SqlElConditionLoopCommentBlock(
                    child,
                    sqlBlockFormattingCtx,
                    blockCommentSpacingBuilder,
                )
            else -> SqlElBlockCommentBlock(child, sqlBlockFormattingCtx, blockCommentSpacingBuilder)
        }

    private fun hasChildComment(child: ASTNode): Boolean = PsiTreeUtil.getChildOfType(child.psi, PsiComment::class.java) != null

    private fun isConditionOrLoopDirective(child: ASTNode): Boolean =
        child.psi is SqlCustomElCommentExpr &&
            (child.psi as SqlCustomElCommentExpr).isConditionOrLoopDirective()
}
