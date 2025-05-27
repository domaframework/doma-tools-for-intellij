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

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.SqlKeywordUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlViewGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlKeywordGroupBlock(
    node: ASTNode,
    val indentLevel: IndentType = IndentType.TOP,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
    enableFormat: Boolean,
    formatMode: FormattingMode,
) : SqlNewGroupBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
    override val indent =
        ElementIndent(
            indentLevel,
            0,
            0,
        )

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        val preChildBlock = block?.childBlocks?.dropLast(1)?.lastOrNull()
        indent.indentLevel = indentLevel

        val baseIndentLen = getBaseIndentLen(preChildBlock, block)
        indent.groupIndentLen = baseIndentLen.plus(getNodeText().length)
        indent.indentLen = adjustIndentIfFirstChildIsLineComment(baseIndentLen)
        createGroupIndentLen()
    }

    private fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        block: SqlBlock?,
    ): Int {
        if (block == null) {
            return createBlockIndentLen()
        }
        if (preChildBlock != null &&
            preChildBlock.indent.indentLevel == this.indent.indentLevel &&
            !SqlKeywordUtil.isSetLineKeyword(preChildBlock.getNodeText(), block.getNodeText())
        ) {
            if (indent.indentLevel == IndentType.SECOND) {
                val diffPreBlockTextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
                return preChildBlock.indent.indentLen.minus(diffPreBlockTextLen)
            } else {
                val diffPretextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
                return preChildBlock.indent.indentLen.minus(diffPretextLen)
            }
        } else {
            return createBlockIndentLen()
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
    private fun adjustIndentIfFirstChildIsLineComment(baseIndent: Int): Int {
        parentBlock?.let {
            if (indent.indentLevel == IndentType.TOP) {
                return if (it is SqlSubGroupBlock) {
                    return if (it.isFirstLineComment) {
                        it.indent.groupIndentLen.minus(it.getNodeText().length)
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

    override fun createBlockIndentLen(): Int {
        when (indentLevel) {
            IndentType.TOP -> {
                parentBlock?.let {
                    val groupLen = it.indent.groupIndentLen
                    return if (it.indent.indentLevel == IndentType.FILE) {
                        0
                    } else {
                        groupLen
                    }
                } ?: return 0
            }

            IndentType.SECOND -> {
                parentBlock?.let { parent ->
                    val groupLen = parent.indent.groupIndentLen

                    if (parent.indent.indentLevel == IndentType.FILE) {
                        return 0
                    } else {
                        parent.parentBlock?.let { grand ->
                            return if (grand is SqlViewGroupBlock) {
                                groupLen.minus(this.getNodeText().length)
                            } else if (grand is SqlSubGroupBlock) {
                                groupLen.minus(getNodeText().length).plus(1)
                            } else {
                                groupLen.minus(this.getNodeText().length)
                            }
                        } ?: return groupLen.minus(this.getNodeText().length)
                    }
                } ?: return 1
            }

            IndentType.SECOND_OPTION -> {
                parentBlock?.let { parent ->
                    val groupLen = parent.indent.groupIndentLen
                    if (parent.indent.indentLevel == IndentType.FILE) {
                        return 0
                    }
                    val subGroupBlock = parent.parentBlock as? SqlSubGroupBlock
                    val newIndent =
                        if (parent is SqlSubQueryGroupBlock) {
                            return if (getNodeText() == "and") {
                                groupLen
                            } else {
                                groupLen.plus(1)
                            }
                        } else if (getNodeText() == "and" && parent.getNodeText() == "or") {
                            return groupLen.plus(1)
                        } else if (parent is SqlKeywordGroupBlock && subGroupBlock != null && subGroupBlock.isFirstLineComment) {
                            groupLen
                        } else {
                            var parentLen = 0
                            val removeStartOffsetLess =
                                parent.childBlocks.dropLast(1).filter {
                                    it.node.startOffset >
                                        parent.node.startOffset
                                }
                            val keywords =
                                removeStartOffsetLess
                                    .takeWhile { it.node.elementType == SqlTypes.KEYWORD }
                            keywords.forEach { keyword ->
                                parentLen = parentLen.plus(keyword.getNodeText().length).plus(1)
                            }
                            val parentTextLen = parent.indent.groupIndentLen.plus(parentLen)
                            return parentTextLen.minus(getNodeText().length)
                        }
                    return newIndent
                } ?: 1
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

    private fun createGroupIndentLen(): Int {
        parentBlock?.let {
            if (indent.indentLevel == IndentType.SECOND_OPTION) {
                var parentLen = 0
                val keywords = it.childBlocks.dropLast(1).filter { it.node.elementType == SqlTypes.KEYWORD }
                keywords.forEach { keyword ->
                    parentLen = parentLen.plus(keyword.getNodeText().length).plus(1)
                }
                it.indent.groupIndentLen
                    .plus(parentLen)
                    .minus(getNodeText().length)
            }
        } ?: 1
        return 1
    }
}
