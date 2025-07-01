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
package org.domaframework.doma.intellij.formatter.block.group.column

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Column definition group block in the column list group attached to Create Table
 * The parent must be SqlCreateTableColumnDefinitionGroupBlock
 */
open class SqlColumnDefinitionRawGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlRawGroupBlock(
        node,
        context,
    ) {
    // TODO:Customize indentation within an inline group
    open val defaultOffset = 0
    val isFirstColumnRaw = node.elementType != SqlTypes.COMMA

    open var columnBlock: SqlBlock? = if (isFirstColumnRaw) this else null

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    /**
     * Right-justify the longest column name in the column definition.
     */
    override fun createBlockIndentLen(): Int = if (isFirstColumnRaw) 1 else defaultOffset
}
