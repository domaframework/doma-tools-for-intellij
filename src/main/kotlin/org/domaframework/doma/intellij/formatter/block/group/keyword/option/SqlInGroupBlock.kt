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
package org.domaframework.doma.intellij.formatter.block.group.keyword.option

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlInGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(
        node,
        IndentType.OPTIONS,
        context,
    ) {
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlElConditionLoopCommentBlock &&
                parent.checkConditionLoopDirectiveParentBlock(this)
            ) {
                return parent.indent.indentLen
            }
            val prevChildren = this.prevBlocks
            val children = prevChildren.filter { it !is SqlDefaultCommentBlock }
            val firstChild = children.firstOrNull()
            val sumChildren =
                if (firstChild is SqlElConditionLoopCommentBlock) {
                    children.drop(1).dropLastWhile { it == this }
                } else {
                    children
                }

            val dotCount = sumChildren.count { it.node.elementType == SqlTypes.DOT }
            val parentText = prevChildren.dropLast(1).filter { it !is SqlDefaultCommentBlock }

            return calculatePrevBlocksLength(parentText, parent)
        }
        return 0
    }

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(getNodeText().length)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        if (lastGroup is SqlElConditionLoopCommentBlock) {
            if (lastGroup.conditionType.isElse()) return false
            return !lastGroup.isBeforeParentBlock()
        }
        return false
    }
}
