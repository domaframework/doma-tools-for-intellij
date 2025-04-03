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
package org.domaframework.doma.intellij.formatter.block.group

import com.intellij.formatting.Alignment
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.SqlKeywordUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock

open class SqlKeywordGroupBlock(
    node: ASTNode,
    val indentLevel: IndentType = IndentType.TOP,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
) : SqlNewGroupBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
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
        val baseIndentLen =
            if (preChildBlock != null &&
                preChildBlock.indent.indentLevel == this.indent.indentLevel &&
                !SqlKeywordUtil.isSetLineKeyword(preChildBlock.node.text, block.node.text)
            ) {
                if (indent.indentLevel == IndentType.SECOND) {
                    val diffPreBlockTextLen = node.text.length.minus(preChildBlock.node.text.length)
                    preChildBlock.indent.indentLen.minus(diffPreBlockTextLen)
                } else {
                    preChildBlock.indent.indentLen
                }
            } else {
                createIndentLen()
            }
        indent.groupIndentLen = baseIndentLen.plus(node.text.length)
        indent.indentLen = adjustIndentIfFirstChildIsLineComment(baseIndentLen)
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? {
        if (parentBlock?.indent?.indentLevel == IndentType.SUB) {
            return Indent.getSpaceIndent(0)
        }
        return Indent.getNoneIndent()
    }

    private fun adjustIndentIfFirstChildIsLineComment(baseIndent: Int): Int {
        if (indent.indentLevel == IndentType.TOP) {
            parentBlock?.let {
                return if (it is SqlSubGroupBlock && it.isFirstLineComment) {
                    it.indent.groupIndentLen.minus(it.node.text.length)
                } else {
                    val newIndentLen = baseIndent.minus(1)
                    if (newIndentLen >= 0) newIndentLen else 0
                }
            }
        }
        return baseIndent
    }

    private fun createIndentLen(): Int =
        when (indentLevel) {
            IndentType.TOP -> {
                parentBlock?.let {
                    val groupLen = it.indent.groupIndentLen
                    if (it.indent.indentLevel == IndentType.FILE) {
                        0
                    } else if (it is SqlSubGroupBlock) {
                        groupLen
                    } else {
                        groupLen.plus(it.node.text.length)
                    }
                } ?: 0
            }

            IndentType.SECOND -> {
                parentBlock?.let {
                    val groupLen =
                        it.indent.groupIndentLen
                            .minus(this.node.text.length)
                    if (it.indent.indentLevel == IndentType.FILE) {
                        0
                    } else if (it.parentBlock is SqlSubGroupBlock) {
                        groupLen.plus(1)
                    } else {
                        groupLen
                    }
                } ?: 1
            }

            IndentType.SECOND_OPTION -> {
                parentBlock?.let {
                    val groupLen = it.indent.groupIndentLen.plus(1)
                    if (it.indent.indentLevel == IndentType.FILE) {
                        return@let 0
                    }
                    val subGroupBlock = it.parentBlock as? SqlSubGroupBlock
                    if (it is SqlSubQueryGroupBlock) {
                        groupLen
                    } else if (it is SqlKeywordGroupBlock && subGroupBlock != null && subGroupBlock.isFirstLineComment) {
                        groupLen
                    } else {
                        it.indent.groupIndentLen
                            .minus(this.node.text.length)
                    }
                } ?: 1
            }

            IndentType.INLINE_SECOND -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.FILE) 0
                    it.indent.groupIndentLen
                        .plus(1)
                } ?: 1
            }

            else -> 1
        }
}
