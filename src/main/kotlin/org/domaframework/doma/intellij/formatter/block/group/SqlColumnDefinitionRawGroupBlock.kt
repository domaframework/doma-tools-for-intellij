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
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Column definition group block in the column list group attached to Create Table
 * The parent must be SqlColumnDefinitionGroupBlock
 */
class SqlColumnDefinitionRawGroupBlock(
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
    // TODO:Customize indentation within an inline group
    val defaultOffset = 5
    val isFirstColumnRaw = node.elementType != SqlTypes.COMMA

    var columnName = node.text

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLen = createIndentLen()
        indent.groupIndentLen = indent.indentLen
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? = Indent.getSpaceIndent(indent.indentLen)

    /**
     * Right-justify the longest column name in the column definition.
     */
    fun createIndentLen(): Int {
        if (!isFirstColumnRaw) return defaultOffset

        parentBlock?.let {
            return when (it) {
                is SqlColumnDefinitionGroupBlock -> {
                    getColumnRawNewIndent(it)
                }

                else -> {
                    1
                }
            }
        }
        return 1
    }

    private fun getColumnRawNewIndent(groupRawBlock: SqlColumnDefinitionGroupBlock): Int {
        val groupMaxAlimentLen = groupRawBlock.alignmentColumnName.length
        val diffColumnName = groupMaxAlimentLen.minus(columnName.length)
        val newSpaces = defaultOffset.plus(diffColumnName)
        return newSpaces.plus(2)
    }
}
