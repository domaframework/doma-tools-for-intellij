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
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * Column List Group Block attached to Create Table
 * example :
 * ```
 * CREATE TABLE departments
 *   ( -- [SqlCreateTableColumnDefinitionGroupBlock]
 *          id INT PRIMARY KEY -- [SqlCreateTableColumnDefinitionRawGroupBlock]
 *      , name VARCHAR(100) -- [SqlCreateTableColumnDefinitionRawGroupBlock]
 *      ,  loc INT NOT NULL -- [SqlCreateTableColumnDefinitionRawGroupBlock]
 *   )
 * ```
 */
class SqlCreateTableColumnDefinitionGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    // TODO Customize indentation
    override val offset = 2
    private val groupOffset = 5
    val columnRawGroupBlocks = mutableListOf<SqlColumnDefinitionRawGroupBlock>()

    fun getMaxColumnNameLength(): Int =
        columnRawGroupBlocks.maxOfOrNull { raw ->
            raw.columnBlock?.getNodeText()?.length ?: 0
        } ?: 0

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(groupOffset)
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int = offset

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true
}
