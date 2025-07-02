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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlTableBlock
import org.domaframework.doma.intellij.formatter.block.SqlWordBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.OnConflictKeywordType
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictClauseBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlSelectQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlTypes

data class SqlBlockFormattingContext(
    val wrap: Wrap?,
    val alignment: Alignment?,
    val spacingBuilder: SpacingBuilder,
    val enableFormat: Boolean,
    val formatMode: FormattingMode,
)

class SqlBlockUtil(
    sqlBlock: SqlBlock,
    enableFormat: Boolean,
    formatMode: FormattingMode,
) {
    val sqlBlockFormattingCtx =
        SqlBlockFormattingContext(
            sqlBlock.wrap,
            sqlBlock.alignment,
            sqlBlock.spacingBuilder,
            enableFormat,
            formatMode,
        )

    fun getKeywordBlock(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        // Because we haven't yet set the parent-child relationship of the block,
        // the parent group references groupTopNodeIndexHistory.
        val keywordText = child.text.lowercase()
        val indentLevel = SqlKeywordUtil.getIndentType(keywordText)

        if (indentLevel.isNewLineGroup()) {
            return getKeywordGroupBlock(indentLevel, keywordText, child, lastGroupBlock)
        }

        when (indentLevel) {
            IndentType.INLINE -> {
                if (!SqlKeywordUtil.isSetLineKeyword(
                        child.text,
                        lastGroupBlock?.getNodeText() ?: "",
                    )
                ) {
                    return SqlInlineGroupBlock(child, sqlBlockFormattingCtx)
                }
            }

            IndentType.ATTACHED -> {
                if (lastGroupBlock is SqlCreateKeywordGroupBlock) {
                    lastGroupBlock.setCreateQueryType(child.text)
                    return SqlKeywordBlock(child, indentLevel, sqlBlockFormattingCtx)
                }
            }

            IndentType.OPTIONS -> {
                if (child.text.lowercase() == "as") {
                    val parentCreateBlock =
                        lastGroupBlock as? SqlCreateKeywordGroupBlock
                            ?: lastGroupBlock?.parentBlock as? SqlCreateKeywordGroupBlock
                    if (parentCreateBlock != null && parentCreateBlock.createType == CreateQueryType.VIEW) {
                        return SqlCreateViewGroupBlock(child, sqlBlockFormattingCtx)
                    }
                }
            }

            IndentType.CONFLICT -> {
                if (lastGroupBlock is SqlConflictClauseBlock) {
                    lastGroupBlock.conflictType =
                        when (keywordText) {
                            "conflict" -> OnConflictKeywordType.CONFLICT
                            "constraint" -> OnConflictKeywordType.CONSTRAINT
                            else -> OnConflictKeywordType.UNKNOWN
                        }
                    return SqlKeywordBlock(child, indentLevel, sqlBlockFormattingCtx)
                } else {
                    return SqlConflictClauseBlock(child, sqlBlockFormattingCtx)
                }
            }

            else -> return SqlKeywordBlock(child, indentLevel, sqlBlockFormattingCtx)
        }
        return SqlKeywordBlock(child, indentLevel, sqlBlockFormattingCtx)
    }

    private fun getKeywordGroupBlock(
        indentLevel: IndentType,
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        when (indentLevel) {
            IndentType.JOIN -> {
                return JoinGroupUtil.getJoinKeywordGroupBlock(
                    lastGroupBlock,
                    keywordText,
                    child,
                    sqlBlockFormattingCtx,
                )
            }

            IndentType.INLINE_SECOND -> {
                return SqlInlineSecondGroupBlock(
                    child,
                    sqlBlockFormattingCtx,
                )
            }

            IndentType.TOP -> {
                return when (keywordText) {
                    "select" ->
                        SqlSelectQueryGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )

                    "create" ->
                        SqlCreateKeywordGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )

                    "insert" ->
                        SqlInsertQueryGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )

                    "do" ->
                        SqlDoGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )

                    "update" ->
                        SqlUpdateQueryGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )

                    else ->
                        SqlKeywordGroupBlock(
                            child,
                            indentLevel,
                            sqlBlockFormattingCtx,
                        )
                }
            }

            IndentType.SECOND -> {
                return if (keywordText == "set") {
                    SqlUpdateSetGroupBlock(
                        child,
                        sqlBlockFormattingCtx,
                    )
                } else {
                    SqlKeywordGroupBlock(
                        child,
                        indentLevel,
                        sqlBlockFormattingCtx,
                    )
                }
            }

            IndentType.SECOND_OPTION -> {
                return if (keywordText == "on" && lastGroupBlock !is SqlJoinGroupBlock) {
                    SqlConflictClauseBlock(
                        child,
                        sqlBlockFormattingCtx,
                    )
                } else if (SqlKeywordUtil.isConditionKeyword(keywordText)) {
                    SqlConditionKeywordGroupBlock(
                        child,
                        sqlBlockFormattingCtx,
                    )
                } else {
                    SqlKeywordGroupBlock(
                        child,
                        indentLevel,
                        sqlBlockFormattingCtx,
                    )
                }
            }

            IndentType.CONFLICT -> {
                return if (lastGroupBlock is SqlConflictClauseBlock) {
                    SqlKeywordBlock(
                        child,
                        indentLevel,
                        sqlBlockFormattingCtx,
                    )
                } else {
                    SqlKeywordGroupBlock(
                        child,
                        indentLevel,
                        sqlBlockFormattingCtx,
                    )
                }
            }

            else -> {
                return SqlKeywordGroupBlock(
                    child,
                    indentLevel,
                    sqlBlockFormattingCtx,
                )
            }
        }
    }

    fun getSubGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        if (PsiTreeUtil.prevLeaf(child.psi)?.elementType == SqlTypes.WORD) {
            return SqlFunctionParamBlock(child, sqlBlockFormattingCtx)
        }

        when (lastGroup) {
            is SqlKeywordGroupBlock -> {
                // List-type test data for IN clause
                NotQueryGroupUtil
                    .getSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                    ?.let { return it }

                CreateTableUtil
                    .getCreateTableClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                    ?.let { return it }

                InsertClauseUtil
                    .getInsertClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                    ?.let { return it }

                UpdateClauseUtil
                    .getUpdateClauseSubGroup(
                        lastGroup,
                        child,
                        sqlBlockFormattingCtx,
                    )?.let { return it }

                if (lastGroup is SqlConditionKeywordGroupBlock) {
                    return SqlConditionalExpressionGroupBlock(
                        child,
                        sqlBlockFormattingCtx,
                    )
                }

                return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
            }

            is SqlColumnDefinitionRawGroupBlock ->
                return SqlDataTypeParamBlock(child, sqlBlockFormattingCtx)

            else ->
                return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
        }
    }

    fun getCommaGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        CreateTableUtil
            .getColumnRawGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?.let { return it }

        return when (lastGroup) {
            is SqlColumnRawGroupBlock, is SqlKeywordGroupBlock -> {
                if (lastGroup.indent.indentLevel == IndentType.SECOND) {
                    SqlCommaBlock(child, sqlBlockFormattingCtx)
                } else {
                    SqlColumnRawGroupBlock(child, sqlBlockFormattingCtx)
                }
            }

            else -> SqlCommaBlock(child, sqlBlockFormattingCtx)
        }
    }

    fun getWordBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock =
        when (lastGroup) {
            is SqlKeywordGroupBlock -> {
                when {
                    SqlKeywordUtil.isBeforeTableKeyword(lastGroup.getNodeText()) -> {
                        SqlTableBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )
                    }

                    else -> SqlWordBlock(child, sqlBlockFormattingCtx)
                }
            }

            is SqlCreateTableColumnDefinitionGroupBlock -> {
                // Top Column Definition Group Block
                SqlCreateTableColumnDefinitionRawGroupBlock(
                    child,
                    sqlBlockFormattingCtx,
                )
            }

            is SqlColumnDefinitionRawGroupBlock -> {
                if (lastGroup.columnBlock == null) {
                    SqlColumnBlock(
                        child,
                        sqlBlockFormattingCtx,
                    )
                } else {
                    SqlWordBlock(child, sqlBlockFormattingCtx)
                }
            }

            else -> SqlWordBlock(child, sqlBlockFormattingCtx)
        }

    fun getBlockCommentBlock(
        child: ASTNode,
        blockCommentSpacingBuilder: SqlCustomSpacingBuilder?,
    ): SqlCommentBlock {
        if (PsiTreeUtil.getChildOfType(child.psi, PsiComment::class.java) != null) {
            return SqlBlockCommentBlock(child, sqlBlockFormattingCtx)
        }
        if (child.psi is SqlCustomElCommentExpr &&
            (child.psi as SqlCustomElCommentExpr).isConditionOrLoopDirective()
        ) {
            return SqlElConditionLoopCommentBlock(
                child,
                sqlBlockFormattingCtx,
                blockCommentSpacingBuilder,
            )
        }
        return SqlElBlockCommentBlock(
            child,
            sqlBlockFormattingCtx,
            blockCommentSpacingBuilder,
        )
    }
}
