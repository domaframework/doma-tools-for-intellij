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
package org.domaframework.doma.intellij.formatter.block.group.subgroup

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInsertKeywordGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Block of columns to insert
 */
class SqlInsertColumnGroupBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
    enableFormat: Boolean,
    formatMode: FormattingMode,
) : SqlSubGroupBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(1)
        updateParentGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let {
            if (it is SqlInsertKeywordGroupBlock) {
                var parentLen = 0
                val keywords =
                    it.childBlocks.dropLast(1).takeWhile { it.node.elementType == SqlTypes.KEYWORD }
                keywords.forEach { keyword ->
                    parentLen = parentLen.plus(keyword.getNodeText().length).plus(1)
                }
                return it.indent.indentLen
                    .plus(it.getNodeText().length)
                    .plus(1)
                    .plus(parentLen)
            }
            // TODO:Customize indentation
            return 2
        } ?: return 2
    }

    private fun updateParentGroupIndentLen() {
        parentBlock?.let {
            if (it is SqlInsertKeywordGroupBlock) {
                var parentLen = 0
                val keywords =
                    it.childBlocks.dropLast(1).takeWhile { it.node.elementType == SqlTypes.KEYWORD }
                keywords.forEach { keyword ->
                    parentLen = parentLen.plus(keyword.getNodeText().length).plus(1)
                }
                it.indent.groupIndentLen =
                    it.indent.indentLen
                        .plus(it.getNodeText().length)
                        .plus(parentLen)
            }
        }
    }
}
