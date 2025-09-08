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
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * In an UPDATE statement using the row value constructor,
 * a group representing the value list
 */
class SqlUpdateValueGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    // TODO Customize indentation
    override val offset = 2

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlUpdateSetGroupBlock)?.valueGroupBlock = this
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlUpdateSetGroupBlock) {
                return parent.indent.indentLen
                    .plus(parent.getNodeText().length)
                    .plus(3)
            }
            return offset
        } ?: return offset
    }

    override fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlUpdateSetGroupBlock) {
                val parentGroupIndent = parent.indent.groupIndentLen
                // Add four spaces after the SET keyword: " = " and "("
                return parentGroupIndent.plus(4)
            }
        } ?: return offset
        return offset
    }
}
