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
        indent.indentLevel = indentLevel
        indent.indentLen = createIndentLen()
        indent.groupIndentLen = indent.indentLen
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? {
        if (parentBlock?.indent?.indentLevel == IndentType.SUB) {
            return Indent.getSpaceIndent(0)
        }
        return Indent.getNoneIndent()
    }

    private fun createIndentLen(): Int =
        when (indentLevel) {
            IndentType.TOP -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.FILE) {
                        0
                    } else if (it is SqlSubGroupBlock) {
                        it.indent.groupIndentLen.plus(1)
                    } else {
                        it.indent.groupIndentLen.plus(it.node.text.length)
                    }
                } ?: 0
            }

            IndentType.SECOND -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.FILE) {
                        0
                    } else {
                        it.indent.groupIndentLen
                            .plus(it.node.text.length)
                            .minus(this.node.text.length)
                    }
                } ?: 1
            }

            IndentType.SECOND_OPTION -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.FILE) {
                        0
                    } else {
                        it.indent.groupIndentLen
                            .plus(it.node.text.length)
                            .minus(this.node.text.length)
                    }
                } ?: 1
            }

            IndentType.INLINE_SECOND -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.FILE) 0
                    it.indent.groupIndentLen
                        .plus(it.node.text.length)
                        .plus(1)
                } ?: 1
            }

            else -> 1
        }
}
