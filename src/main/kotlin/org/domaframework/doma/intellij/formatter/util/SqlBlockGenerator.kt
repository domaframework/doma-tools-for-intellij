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
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
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
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlSecondOptionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlFromGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlSecondKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlWhereGroupBlock
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
            sqlBlock.wrap,
            sqlBlock.alignment,
            sqlBlock.spacingBuilder,
            enableFormat,
            formatMode,
        )

    private val keywordBlockFactory = SqlKeywordBlockFactory(sqlBlockFormattingCtx)

    fun getKeywordBlock(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        val keywordText = child.text.lowercase()
        val indentLevel = SqlKeywordUtil.getIndentType(keywordText)

        if (indentLevel.isNewLineGroup()) {
            return getKeywordGroupBlock(indentLevel, keywordText, child, lastGroupBlock)
        }

        return when (indentLevel) {
            IndentType.INLINE -> keywordBlockFactory.createInlineBlock(child, lastGroupBlock)
            IndentType.ATTACHED -> keywordBlockFactory.createAttachedBlock(child, lastGroupBlock)
            IndentType.OPTIONS -> keywordBlockFactory.createOptionsBlock(keywordText, child, lastGroupBlock)
            IndentType.CONFLICT -> keywordBlockFactory.createConflictBlock(keywordText, child, lastGroupBlock)
            else -> SqlKeywordBlock(child, indentLevel, sqlBlockFormattingCtx)
        }
    }

    private fun getKeywordGroupBlock(
        indentLevel: IndentType,
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        when (indentLevel) {
            IndentType.JOIN -> {
                return JoinClauseHandler.getJoinKeywordGroupBlock(
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
                            WithClauseHandler
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
                    "where" -> {
                        SqlWhereGroupBlock(
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
                        WithClauseHandler
                            .getWithClauseKeywordGroup(lastGroupBlock, child, sqlBlockFormattingCtx)
                            ?.let { return it }

                        NotQueryGroupHandler
                            .getKeywordGroup(
                                child,
                                sqlBlockFormattingCtx,
                            )?.let { return it }
                        if (lastGroupBlock is SqlFunctionGroupBlock) {
                            return SqlKeywordBlock(child, IndentType.NONE, sqlBlockFormattingCtx)
                        }
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
        groups: List<SqlBlock>,
    ): SqlBlock {
        val ignoreConditionLoopLastBlock = groups.lastOrNull { it !is SqlElConditionLoopCommentBlock }
        when (lastGroup) {
            is SqlKeywordGroupBlock -> {
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
                    .getUpdateClauseSubGroup(
                        lastGroup,
                        child,
                        sqlBlockFormattingCtx,
                    )?.let { return it }

                // List-type test data for IN clause
                NotQueryGroupHandler
                    .getSubGroup(ignoreConditionLoopLastBlock, child, sqlBlockFormattingCtx)
                    ?.let { return it }

                return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
            }

            is SqlColumnDefinitionRawGroupBlock ->
                return SqlDataTypeParamBlock(child, sqlBlockFormattingCtx)

            else -> {
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
        }
    }

    fun getCommaGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        CreateClauseHandler
            .getColumnRawGroup(lastGroup, child, sqlBlockFormattingCtx)
            ?.let { return it }

        return CommaRawClauseHandler.getCommaBlock(lastGroup, child, sqlBlockFormattingCtx)
    }

    fun getWordBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        if (child.text.lowercase() == "array") {
            return SqlArrayWordBlock(
                child,
                sqlBlockFormattingCtx,
            )
        }
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
            return SqlBlockCommentBlock(child, createBlockCommentSpacingBuilder(), sqlBlockFormattingCtx)
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

    private fun createBlockCommentSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.BLOCK_COMMENT_CONTENT,
                Spacing.createSpacing(0, 0, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_CONTENT,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(0, 0, 0, true, 0),
            )
}
