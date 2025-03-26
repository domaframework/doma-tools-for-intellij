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
package org.domaframework.doma.intellij.formatter.block

import com.intellij.formatting.Alignment
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock

open class SqlKeywordBlock(
    node: ASTNode,
    override val indentLevel: IndentType = IndentType.TOP,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
) : SqlBlock(
        node,
        wrap,
        alignment,
        null,
        spacingBuilder,
    ) {
    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indentLen = createIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? {
        if (indentLevel > IndentType.FILE) {
            println("Indent:${node.text}, $indentLevel, $indentLen")
            return Indent.getIndent(Indent.Type.SPACES, indentLen, false, true)
        }
        return Indent.getNormalIndent()
    }

    private fun createIndentLen(): Int =
        when (indentLevel) {
            IndentType.TOP -> {
                if (parentBlock?.indentLevel == IndentType.SUB) {
                    (parentBlock?.indentLen?.plus(1)) ?: 1
                } else {
                    (parentBlock?.indentLen) ?: 0
                }
            }

            IndentType.SECOND -> {
                if (parentBlock?.indentLevel == IndentType.SUB) {
                    (parentBlock?.indentLen) ?: 1
                } else {
                    parentBlock?.let {
                        (
                            it.indentLen +
                                it.node.text.length
                                    .minus(this.node.text.length)
                        )
                    } ?: 1
                }
            }

            IndentType.SUB -> {
                val parentLen = parentBlock?.node?.text?.length ?: 0
                parentBlock?.indentLen?.plus(parentLen.plus(1)) ?: 1
            }

            else -> {
                1
            }
        }
}
