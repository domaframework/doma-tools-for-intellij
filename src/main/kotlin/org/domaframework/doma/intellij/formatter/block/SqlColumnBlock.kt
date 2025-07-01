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
import org.domaframework.doma.intellij.formatter.block.SqlWordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlColumnBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlWordBlock(
        node,
        context,
    ) {
    override val indent =
        ElementIndent(
            IndentType.NONE,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.NONE
        // Calculate right justification space during indentation after getting all column rows
        indent.indentLen = 1
        indent.groupIndentLen = 0
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlColumnDefinitionRawGroupBlock)?.columnBlock = this
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let {
            val parentGroupDefinition = it.parentBlock as? SqlCreateTableColumnDefinitionGroupBlock
            if (parentGroupDefinition == null) return 1

            val groupMaxAlimentLen = parentGroupDefinition.getMaxColumnNameLength()
            val diffColumnName = groupMaxAlimentLen.minus(getNodeText().length)
            return diffColumnName.plus(1)
        }
        return 1
    }
}
