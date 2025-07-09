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
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlJoinQueriesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
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

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlWithCommonTableGroupBlock) {
            lastGroup.queryGroupBlock.add(this)
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int =
        parentBlock?.let { parent ->
            return if (parent is SqlJoinQueriesGroupBlock) {
                parent.indent.indentLen
            } else if (parent is SqlWithQuerySubGroupBlock) {
                parent.indent.groupIndentLen
            } else {
                parent.indent.indentLen
                    .plus(
                        parent
                            .getChildBlocksDropLast()
                            .filter { it !is SqlLineCommentBlock && it !is SqlBlockCommentBlock }
                            .sumOf { it.getNodeText().length.plus(1) },
                    ).plus(parent.getNodeText().length.plus(1))
            }
        } ?: offset

    override fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlJoinQueriesGroupBlock) {
                return parent.indent.indentLen.plus(1)
            }
            if (parent is SqlConditionalExpressionGroupBlock) {
                return indent.indentLen
            }
        }
        return indent.indentLen.plus(1)
    }
}
