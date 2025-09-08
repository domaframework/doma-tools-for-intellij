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
package org.domaframework.doma.intellij.formatter.block.group.keyword.create

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlDataTypeBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlCreateTableColumnDefinitionRawGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlColumnDefinitionRawGroupBlock(
        node,
        context,
    ) {
    var columnDataTypeBlock: SqlDataTypeBlock? = null

    override var parentBlock: SqlBlock?
        get() = super.parentBlock as? SqlCreateTableColumnDefinitionGroupBlock
        set(value) {
            if (value is SqlCreateTableColumnDefinitionGroupBlock) {
                super.parentBlock = value
            }
        }

    override var columnBlock: SqlBlock?
        get() = super.columnBlock
        set(value) {
            if (value?.node?.elementType == SqlTypes.COMMA) {
                super.columnBlock = null
            } else {
                super.columnBlock = value
            }
        }

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.groupIndentLen = indent.indentLen
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlCreateTableColumnDefinitionGroupBlock)
            ?.columnRawGroupBlocks
            ?.add(
                this,
            )
    }

    override fun createBlockIndentLen(): Int =
        (parentBlock as? SqlCreateTableColumnDefinitionGroupBlock)
            ?.let { parent ->
                return getColumnRawNewIndent(parent)
            } ?: 1

    /**
     * Aligns each column name to the right based on the longest column name
     * within the column definition group of a Create Table query.
     */
    private fun getColumnRawNewIndent(groupRawBlock: SqlCreateTableColumnDefinitionGroupBlock): Int {
        val groupMaxAlimentLen = groupRawBlock.getMaxColumnNameLength()
        val diffColumnName = groupMaxAlimentLen.minus(getNodeText().length)
        return diffColumnName.plus(2)
    }
}
