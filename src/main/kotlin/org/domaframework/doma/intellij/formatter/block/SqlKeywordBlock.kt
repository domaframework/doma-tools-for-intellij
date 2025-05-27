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
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock

open class SqlKeywordBlock(
    node: ASTNode,
    val indentLevel: IndentType = IndentType.ATTACHED,
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

        indent.indentLevel = indentLevel
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(getNodeText().length)
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? {
        if (!isAdjustIndentOnEnter() && parentBlock?.indent?.indentLevel == IndentType.SUB) {
            return Indent.getIndent(Indent.Type.SPACES, 0, false, true)
        }
        return Indent.getNoneIndent()
    }

    override fun createBlockIndentLen(): Int =
        when (indentLevel) {
            IndentType.TOP -> {
                parentBlock?.let {
                    if (it.indent.indentLevel == IndentType.SUB) {
                        it.indent.groupIndentLen.plus(1)
                    } else {
                        0
                    }
                } ?: 0
            }

            IndentType.SECOND -> {
                parentBlock?.let {
                    it.indent.groupIndentLen
                        .plus(it.getNodeText().length)
                        .minus(this.getNodeText().length)
                } ?: 1
            }

            IndentType.INLINE_SECOND -> {
                parentBlock?.let {
                    it.indent.groupIndentLen
                        .plus(it.getNodeText().length)
                        .plus(1)
                } ?: 1
            }

            else -> 1
        }
}
