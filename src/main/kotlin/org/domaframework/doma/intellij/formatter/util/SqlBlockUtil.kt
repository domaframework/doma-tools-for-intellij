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
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
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
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlLateralGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlSecondOptionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlFromGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlSecondKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlDeleteQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlJoinQueriesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlSelectQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlAliasBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlTableBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlWordBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr

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
                if (keywordText == "as") {
                    val parentCreateBlock =
                        lastGroupBlock as? SqlCreateKeywordGroupBlock
                            ?: lastGroupBlock?.parentBlock as? SqlCreateKeywordGroupBlock
                    if (parentCreateBlock != null && parentCreateBlock.createType == CreateQueryType.VIEW) {
                        return SqlCreateViewGroupBlock(child, sqlBlockFormattingCtx)
                    }
                }
                if (keywordText == "lateral") {
                    return SqlLateralGroupBlock(child, sqlBlockFormattingCtx)
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
                    "with" ->
                        SqlWithQueryGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )

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

                    "delete" ->
                        SqlDeleteQueryGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )
                    "union", "intersect", "except" ->
                        SqlJoinQueriesGroupBlock(
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
                return when (keywordText) {
                    "set" -> {
                        if (lastGroupBlock is SqlUpdateQueryGroupBlock) {
                            SqlUpdateSetGroupBlock(
                                child,
                                sqlBlockFormattingCtx,
                            )
                        } else {
                            WithClauseUtil
                                .getWithClauseKeywordGroup(
                                    lastGroupBlock,
                                    child,
                                    sqlBlockFormattingCtx,
                                )?.let { return it }
                            return SqlSecondKeywordBlock(
                                child,
                                sqlBlockFormattingCtx,
                            )
                        }
                    }
                    "from" -> {
                        SqlFromGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )
                    }

                    "values" ->
                        SqlValuesGroupBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )

                    else -> {
                        WithClauseUtil
                            .getWithClauseKeywordGroup(lastGroupBlock, child, sqlBlockFormattingCtx)
                            ?.let { return it }

                        NotQueryGroupUtil
                            .getKeywordGroup(
                                child,
                                sqlBlockFormattingCtx,
                            )?.let { return it }

                        SqlSecondKeywordBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )
                    }
                }
            }

            IndentType.SECOND_OPTION -> {
                return if (keywordText == "on" &&
                    lastGroupBlock !is SqlJoinGroupBlock &&
                    lastGroupBlock?.parentBlock !is SqlJoinGroupBlock
                ) {
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
                    SqlSecondOptionKeywordGroupBlock(
                        child,
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
        when (lastGroup) {
            is SqlKeywordGroupBlock -> {
                CreateTableUtil
                    .getCreateTableClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                    ?.let { return it }

                WithClauseUtil
                    .getWithClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
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

                // List-type test data for IN clause
                NotQueryGroupUtil
                    .getSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                    ?.let { return it }

                return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
            }

            is SqlColumnDefinitionRawGroupBlock ->
                return SqlDataTypeParamBlock(child, sqlBlockFormattingCtx)

            else -> {
                if (lastGroup is SqlSubGroupBlock) {
                    WithClauseUtil
                        .getWithClauseSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                        ?.let { return it }
                }

                NotQueryGroupUtil
                    .getSubGroup(lastGroup, child, sqlBlockFormattingCtx)
                    ?.let { return it }

                return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
            }
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

            is SqlWithCommonTableGroupBlock -> SqlWithCommonTableGroupBlock(child, sqlBlockFormattingCtx)

            else -> SqlCommaBlock(child, sqlBlockFormattingCtx)
        }
    }

    fun getWordBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        when (lastGroup) {
            is SqlKeywordGroupBlock -> {
                when {
                    SqlKeywordUtil.isBeforeTableKeyword(lastGroup.getNodeText()) -> {
                        return SqlTableBlock(
                            child,
                            sqlBlockFormattingCtx,
                        )
                    }

                    lastGroup is SqlWithQueryGroupBlock -> return SqlWithCommonTableGroupBlock(
                        child,
                        sqlBlockFormattingCtx,
                    )
                }
            }

            is SqlCreateTableColumnDefinitionGroupBlock -> {
                // Top Column Definition Group Block
                return SqlCreateTableColumnDefinitionRawGroupBlock(
                    child,
                    sqlBlockFormattingCtx,
                )
            }

            is SqlColumnDefinitionRawGroupBlock -> {
                if (lastGroup.columnBlock == null) {
                    return SqlColumnBlock(
                        child,
                        sqlBlockFormattingCtx,
                    )
                }
            }
        }
        if (lastGroup is SqlFromGroupBlock || lastGroup?.parentBlock is SqlFromGroupBlock) {
            return SqlAliasBlock(child, sqlBlockFormattingCtx)
        }
        return SqlWordBlock(child, sqlBlockFormattingCtx)
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
