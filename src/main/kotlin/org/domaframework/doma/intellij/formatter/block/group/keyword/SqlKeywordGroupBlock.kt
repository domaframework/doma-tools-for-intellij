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
package org.domaframework.doma.intellij.formatter.block.group.keyword

import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlSelectQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

open class SqlKeywordGroupBlock(
    node: ASTNode,
    val indentLevel: IndentType = IndentType.TOP,
    context: SqlBlockFormattingContext,
) : SqlNewGroupBlock(node, context) {
    val topKeywordBlocks: MutableList<SqlBlock> = mutableListOf(this)
    var canAddTopKeyword = true

    fun updateTopKeywordBlocks(block: SqlBlock) {
        val lastChild =
            getChildBlocksDropLast()
                .findLast { it !is SqlLineCommentBlock && it !is SqlBlockCommentBlock }
        val topKeywordTypes =
            listOf(
                SqlKeywordBlock::class,
                SqlKeywordGroupBlock::class,
            )

        if (lastChild == null || TypeUtil.isExpectedClassType(topKeywordTypes, lastChild) && canAddTopKeyword) {
            topKeywordBlocks.add(block)
        } else {
            canAddTopKeyword = false
        }

        indent.groupIndentLen = createGroupIndentLen()
    }

    override val indent =
        ElementIndent(
            indentLevel,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        val preChildBlock =
            if (lastGroup?.indent?.indentLevel == IndentType.FILE) {
                null
            } else {
                lastGroup?.childBlocks?.dropLast(1)?.lastOrNull()
            }
        indent.indentLevel = indentLevel

        val baseIndentLen = getBaseIndentLen(preChildBlock, lastGroup)
        indent.groupIndentLen = createGroupIndentLen()
        indent.indentLen = adjustIndentIfFirstChildIsLineComment(baseIndentLen)
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlSelectQueryGroupBlock &&
            SqlKeywordUtil.isSelectSecondOptionKeyword(getNodeText())
        ) {
            lastGroup.secondGroupBlocks.add(this)
        }

        if (lastGroup is SqlWithCommonTableGroupBlock) {
            lastGroup.queryGroupBlock.add(this)
        }
    }

    open fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        lastGroup: SqlBlock?,
    ): Int {
        if (lastGroup == null) {
            return createBlockIndentLen(preChildBlock)
        }
        if (preChildBlock == null) return createBlockIndentLen(preChildBlock)

        if (preChildBlock.indent.indentLevel == this.indent.indentLevel &&
            !SqlKeywordUtil.isSetLineKeyword(getNodeText(), preChildBlock.getNodeText())
        ) {
            val diffPretextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
            return preChildBlock.indent.indentLen.minus(diffPretextLen)
        } else {
            return createBlockIndentLen(preChildBlock)
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? {
        if (!isAdjustIndentOnEnter() && parentBlock?.indent?.indentLevel == IndentType.SUB) {
            return Indent.getSpaceIndent(0)
        }
        return Indent.getNoneIndent()
    }

    /**
     * Adjust the indent position of the subgroup block element itself if it has a comment
     */
    open fun adjustIndentIfFirstChildIsLineComment(baseIndent: Int): Int {
        parentBlock?.let { parent ->
            if (indent.indentLevel == IndentType.TOP) {
                return if (parent is SqlSubGroupBlock) {
                    return if (parent.isFirstLineComment) {
                        parent.indent.groupIndentLen.minus(parent.getNodeText().length)
                    } else {
                        val newIndentLen = baseIndent.minus(1)
                        return if (newIndentLen >= 0) newIndentLen else 0
                    }
                } else {
                    return baseIndent
                }
            }
        }
        return baseIndent
    }

    open fun createBlockIndentLen(preChildBlock: SqlBlock?): Int {
        when (indentLevel) {
            IndentType.TOP -> {
                parentBlock?.let { parent ->
                    if (SqlKeywordUtil.isSetLineKeyword(getNodeText(), preChildBlock?.getNodeText() ?: "")) {
                        val prevBlockIndent = preChildBlock?.indent?.indentLen ?: 0
                        val prevBlockLen = preChildBlock?.getNodeText()?.length ?: 0
                        return prevBlockIndent.plus(prevBlockLen).plus(1)
                    }
                    return if (parent.indent.indentLevel == IndentType.FILE) {
                        0
                    } else {
                        parent.indent.groupIndentLen
                    }
                } ?: return 0
            }

            IndentType.INLINE_SECOND -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.FILE) 0
                    return it.indent.groupIndentLen
                        .plus(1)
                } ?: return 1
            }

            else -> return 1
        }
        return 1
    }

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(topKeywordBlocks.sumOf { it.getNodeText().length.plus(1) }.minus(1))

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true
}
