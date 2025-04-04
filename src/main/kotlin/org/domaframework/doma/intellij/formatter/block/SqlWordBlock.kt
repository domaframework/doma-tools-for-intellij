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
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.block.group.SqlSubQueryGroupBlock

open class SqlWordBlock(
    node: ASTNode,
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
    override val indent =
        ElementIndent(
            IndentType.NONE,
            0,
            0,
        )

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? = Indent.getSpaceIndent(indent.indentLen)

    override fun createBlockIndentLen(): Int {
        parentBlock?.let {
            when (it) {
                is SqlSubQueryGroupBlock -> {
                    val parentIndentLen = it.indent.groupIndentLen
                    val grand = it.parentBlock
                    if (grand != null && grand.node.text.lowercase() == "create") {
                        val grandIndentLen = grand.indent.groupIndentLen
                        return grandIndentLen.plus(parentIndentLen).plus(1)
                    }
                    return parentIndentLen.plus(1)
                }

                else -> {
                    val parentLen = it.node.text.length
                    return it.indent.groupIndentLen.plus(parentLen.plus(1))
                }
            }
        }
        return 1
    }
}
