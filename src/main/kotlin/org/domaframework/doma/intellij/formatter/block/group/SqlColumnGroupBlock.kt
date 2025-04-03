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

/**
 * Group blocks when generating columns with subqueries
 */
class SqlColumnGroupBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
) : SqlSubGroupBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
    ) {
    var isFirstColumnGroup = node.text != ","

    override val indent =
        ElementIndent(
            IndentType.COLUMN,
            0,
            0,
        )

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLevel = IndentType.COLUMN
        indent.indentLen = createIndentLen()
        indent.groupIndentLen =
            if (isFirstColumnGroup) indent.indentLen else indent.indentLen.plus(1)
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? = Indent.getSpaceIndent(indent.indentLen)

    override fun createIndentLen(): Int =
        parentBlock?.let {
            if (it is SqlKeywordGroupBlock) {
                val parentIndentLen = it.indent.indentLen.plus(it.node.text.length)
                val subGroup = it.parentBlock as? SqlSubGroupBlock
                if (subGroup is SqlSubGroupBlock && !subGroup.isFirstLineComment) {
                    parentIndentLen.plus(3)
                } else {
                    parentIndentLen.plus(1)
                }
            } else {
                it.indent.groupIndentLen.plus(1)
            }
        } ?: 1
}
