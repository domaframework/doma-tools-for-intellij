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
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
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
            return when (parent) {
                is SqlElConditionLoopCommentBlock -> {
                    return if (parent.isBeforeParentBlock()) {
                        parent.parentBlock
                            ?.indent
                            ?.groupIndentLen
                            ?.plus(1) ?: 1
                    } else {
                        parent.indent.indentLen
                    }
                }
                is SqlWithQuerySubGroupBlock -> return parent.indent.groupIndentLen
                is SqlJoinQueriesGroupBlock -> return parent.indent.indentLen
                is SqlJoinGroupBlock -> return parent.indent.groupIndentLen.plus(1)
                else -> {
                    val children =
                        prevChildren?.filter {
                            it !is SqlDefaultCommentBlock &&
                                (parent as? SqlKeywordGroupBlock)?.topKeywordBlocks?.contains(it) == false
                        }
                    // Retrieve the list of child blocks excluding the conditional directive that appears immediately before this block,
                    // as it is already included as a child block.
                    val sumChildren =
                        if (children?.firstOrNull() is SqlElConditionLoopCommentBlock) {
                            children.drop(1).dropLast(1)
                        } else {
                            children?.dropLast(1) ?: emptyList()
                        }
                    return sumChildren
                        .sumOf { prev ->
                            prev
                                .getChildrenTextLen()
                                .plus(prev.getNodeText().length.plus(1))
                        }.plus(parent.indent.groupIndentLen)
                        .plus(1)
                }
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
        return indent.indentLen.plus(getNodeText().length)
    }
}
