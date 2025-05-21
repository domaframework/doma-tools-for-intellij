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
package org.domaframework.doma.intellij.formatter

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlTableBlock
import org.domaframework.doma.intellij.formatter.block.SqlWordBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInsertKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlUpdateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlViewGroupBlock
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlBlockUtil(
    sqlBlock: SqlBlock,
) {
    val wrap = sqlBlock.wrap
    val alignment = sqlBlock.alignment
    val spacingBuilder = sqlBlock.spacingBuilder

    fun getKeywordBlock(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        // Because we haven't yet set the parent-child relationship of the block,
        // the parent group references groupTopNodeIndexHistory.
        val indentLevel = SqlKeywordUtil.getIndentType(child.text)
        val keywordText = child.text.lowercase()
        if (indentLevel.isNewLineGroup()) {
            when (indentLevel) {
                IndentType.JOIN -> {
                    return if (SqlKeywordUtil.isJoinKeyword(child.text)) {
                        SqlJoinGroupBlock(child, wrap, alignment, spacingBuilder)
                    } else if (lastGroupBlock is SqlJoinGroupBlock) {
                        SqlKeywordBlock(child, IndentType.ATTACHED, wrap, alignment, spacingBuilder)
                    } else {
                        SqlJoinGroupBlock(child, wrap, alignment, spacingBuilder)
                    }
                }

                IndentType.INLINE_SECOND -> {
                    return SqlInlineSecondGroupBlock(child, wrap, alignment, spacingBuilder)
                }

                IndentType.TOP -> {
                    if (keywordText == "create") {
                        return SqlCreateKeywordGroupBlock(child, wrap, alignment, spacingBuilder)
                    }
                    if (keywordText == "insert") {
                        return SqlInsertKeywordGroupBlock(child, wrap, alignment, spacingBuilder)
                    }

                    return SqlKeywordGroupBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                }

                IndentType.SECOND -> {
                    return if (keywordText == "set") {
                        SqlUpdateKeywordGroupBlock(child, wrap, alignment, spacingBuilder)
                    } else {
                        SqlKeywordGroupBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                    }
                }

                else -> {
                    return SqlKeywordGroupBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                }
            }
        }

        when (indentLevel) {
            IndentType.INLINE -> {
                if (!SqlKeywordUtil.isSetLineKeyword(
                        child.text,
                        lastGroupBlock?.getNodeText() ?: "",
                    )
                ) {
                    return SqlInlineGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            IndentType.ATTACHED -> {
                if (lastGroupBlock is SqlCreateKeywordGroupBlock) {
                    lastGroupBlock.setCreateQueryType(child.text)
                    return SqlKeywordBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                }
            }

            IndentType.OPTIONS -> {
                if (child.text.lowercase() == "as") {
                    val parentCreateBlock =
                        lastGroupBlock as? SqlCreateKeywordGroupBlock
                            ?: lastGroupBlock?.parentBlock as? SqlCreateKeywordGroupBlock
                    if (parentCreateBlock != null && parentCreateBlock.createType == CreateQueryType.VIEW) {
                        return SqlViewGroupBlock(child, wrap, alignment, spacingBuilder)
                    }
                }
            }

            else -> return SqlKeywordBlock(child, indentLevel, wrap, alignment, spacingBuilder)
        }
        return SqlKeywordBlock(child, indentLevel, wrap, alignment, spacingBuilder)
    }

    fun getSubGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        if (child.treePrev.elementType == SqlTypes.WORD) {
            return SqlFunctionParamBlock(child, wrap, alignment, spacingBuilder)
        }

        when (lastGroup) {
            is SqlKeywordGroupBlock -> {
                val lastKeyword =
                    lastGroup.childBlocks
                        .lastOrNull { SqlKeywordUtil.isOptionSqlKeyword(it.getNodeText()) }
                if (lastKeyword != null && lastKeyword.getNodeText().lowercase() == "in") {
                    return SqlParallelListBlock(child, wrap, alignment, spacingBuilder)
                }
                if (lastGroup is SqlCreateKeywordGroupBlock) {
                    return SqlColumnDefinitionGroupBlock(child, wrap, alignment, spacingBuilder)
                }
                if (lastGroup is SqlInsertKeywordGroupBlock) {
                    return SqlInsertColumnGroupBlock(child, wrap, alignment, spacingBuilder)
                }
                if (lastGroup is SqlUpdateKeywordGroupBlock) {
                    return if (lastGroup.childBlocks.firstOrNull { it is SqlUpdateColumnGroupBlock } == null) {
                        SqlUpdateColumnGroupBlock(child, wrap, alignment, spacingBuilder)
                    } else if (lastGroup.childBlocks.lastOrNull { it is SqlUpdateColumnGroupBlock } != null) {
                        SqlUpdateValueGroupBlock(child, wrap, alignment, spacingBuilder)
                    } else {
                        SqlSubQueryGroupBlock(child, wrap, alignment, spacingBuilder)
                    }
                }
                return SqlSubQueryGroupBlock(child, wrap, alignment, spacingBuilder)
            }

            is SqlColumnDefinitionRawGroupBlock ->
                return SqlDataTypeParamBlock(child, wrap, alignment, spacingBuilder)

            else ->
                return SqlSubQueryGroupBlock(child, wrap, alignment, spacingBuilder)
        }
    }

    fun getCommaGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock =
        when (lastGroup) {
            is SqlColumnDefinitionGroupBlock, is SqlColumnDefinitionRawGroupBlock ->
                SqlColumnDefinitionRawGroupBlock(
                    child,
                    wrap,
                    alignment,
                    spacingBuilder,
                )

            is SqlColumnGroupBlock, is SqlKeywordGroupBlock -> {
                if (lastGroup.indent.indentLevel == IndentType.SECOND) {
                    SqlCommaBlock(child, wrap, alignment, spacingBuilder)
                } else {
                    SqlColumnGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            else -> SqlCommaBlock(child, wrap, alignment, spacingBuilder)
        }

    fun getWordBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock =
        when (lastGroup) {
            is SqlKeywordGroupBlock -> {
                when {
                    SqlKeywordUtil.isBeforeTableKeyword(lastGroup.getNodeText()) ->
                        SqlTableBlock(
                            child,
                            wrap,
                            alignment,
                            spacingBuilder,
                        )

                    else -> SqlWordBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            is SqlColumnDefinitionGroupBlock -> {
                lastGroup.alignmentColumnName = child.text
                SqlColumnDefinitionRawGroupBlock(
                    child,
                    wrap,
                    alignment,
                    spacingBuilder,
                )
            }

            is SqlColumnDefinitionRawGroupBlock -> {
                if (lastGroup.childBlocks.isEmpty()) {
                    lastGroup.columnName = child.text
                    SqlColumnBlock(
                        child,
                        wrap,
                        alignment,
                        spacingBuilder,
                    )
                } else {
                    SqlWordBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            else -> SqlWordBlock(child, wrap, alignment, spacingBuilder)
        }

    fun getBlockCommentBlock(
        child: ASTNode,
        blockCommentSpacingBuilder: SqlCustomSpacingBuilder?,
    ): SqlCommentBlock {
        if (PsiTreeUtil.getChildOfType(child.psi, PsiComment::class.java) != null) {
            return SqlBlockCommentBlock(
                child,
                wrap,
                alignment,
                spacingBuilder,
            )
        }
        if (child.psi is SqlCustomElCommentExpr &&
            (child.psi as SqlCustomElCommentExpr).isConditionOrLoopDirective()
        ) {
            return SqlElConditionLoopCommentBlock(
                child,
                wrap,
                alignment,
                blockCommentSpacingBuilder,
                spacingBuilder,
            )
        }
        return SqlElBlockCommentBlock(
            child,
            wrap,
            alignment,
            blockCommentSpacingBuilder,
            spacingBuilder,
        )
    }
}
