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
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateColumnGroupBlock

open class SqlOtherBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
    lastGroup: SqlBlock? = null,
    enableFormat: Boolean,
    formatMode: FormattingMode,
) : SqlBlock(
        node,
        wrap,
        alignment,
        null,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
    var isUpdateColumnSubstitutions = isBeforeUpdateValuesBlock(lastGroup)

    override val indent =
        ElementIndent(
            IndentType.NONE,
            0,
            0,
        )

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLevel = IndentType.NONE
        indent.indentLen = createIndentLen()
        indent.groupIndentLen = 0
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    private fun createIndentLen(): Int {
        if (isUpdateColumnSubstitutions) {
            parentBlock?.let { return it.indent.groupIndentLen.plus(1) }
                ?: return indent.indentLen
        } else {
            return 1
        }
    }

    fun isBeforeUpdateValuesBlock(lastGroupBlock: SqlBlock?): Boolean =
        lastGroupBlock?.childBlocks?.lastOrNull() is SqlUpdateColumnGroupBlock

    override fun isLeaf(): Boolean = true
}
