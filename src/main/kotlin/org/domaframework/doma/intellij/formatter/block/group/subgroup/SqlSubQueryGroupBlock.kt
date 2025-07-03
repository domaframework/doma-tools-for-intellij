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

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlSubQueryGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int = 1

    private fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlJoinGroupBlock) {
                var parentLen = getKeywordNameLength(parent.childBlocks, 1)
                return parent.indent.indentLen
                    .plus(parent.getNodeText().length)
                    .plus(2)
                    .plus(parentLen)
            }

            var parentLen = 0
            val prevBlocks =
                prevChildren
                    ?.dropLast(1)
                    ?.filter { it.node.startOffset > parent.node.startOffset }
            prevBlocks
                ?.forEach { prev ->
                    parentLen = parentLen.plus(prev.getNodeText().length).plus(1)
                }
            return parent.indent.groupIndentLen
                .plus(parentLen)
                .plus(2)
        } ?: return 1
    }
}
