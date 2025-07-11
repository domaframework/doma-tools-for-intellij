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
package org.domaframework.doma.intellij.formatter.block.group.keyword.update

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnSelectionGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * In an UPDATE statement using the row value constructor,
 * a group representing the column list
 */
class SqlUpdateColumnGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlColumnSelectionGroupBlock(
        node,
        context,
    ) {
    // TODO:Customize indentation
    override val offset = 2
    val columnRawGroupBlocks: MutableList<SqlColumnRawGroupBlock> = mutableListOf()

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(1)
        updateParentGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlUpdateSetGroupBlock)?.columnDefinitionGroupBlock = this
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlUpdateSetGroupBlock) {
                var parentLen = getKeywordNameLength(parent.childBlocks, 1)
                return parent.indent.indentLen
                    .plus(parent.getNodeText().length)
                    .plus(1)
                    .plus(parentLen)
            }
            return offset
        } ?: return offset
    }

    private fun updateParentGroupIndentLen() {
        parentBlock?.let { parent ->
            if (parent is SqlUpdateSetGroupBlock) {
                val parentLen = getKeywordNameLength(parent.childBlocks, 1)
                parent.indent.groupIndentLen =
                    parent.indent.indentLen
                        .plus(parent.getNodeText().length)
                        .plus(parentLen)
            }
        }
    }
}
