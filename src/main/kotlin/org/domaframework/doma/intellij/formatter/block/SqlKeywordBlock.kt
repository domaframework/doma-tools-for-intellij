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
    override val indentLevel: Int = 0,
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
        if (indentLevel > 0) {
            return Indent.getIndent(Indent.Type.SPACES, indentLen, false, false)
        }
        return Indent.getNoneIndent()
    }

    override fun getChildIndent(): Indent? = getIndent()

    private fun createIndentLen(): Int =
        when (indentLevel) {
            1 -> {
                if (parentBlock?.indentLevel == 3) {
                    (parentBlock?.indentLen) ?: 0
                } else {
                    (parentBlock?.indentLen) ?: 0
                }
            }

            2 -> {
                if (parentBlock?.indentLevel == 3) {
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

            3 -> {
                val parentLen = parentBlock?.node?.text?.length ?: 0
                parentBlock?.indentLen?.plus(parentLen.plus(1)) ?: 1
            }

            else -> {
                1
            }
        }

    override fun isLeaf(): Boolean = true
}
