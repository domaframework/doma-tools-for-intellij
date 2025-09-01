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
package org.domaframework.doma.intellij.formatter.block.group.keyword.condition

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlSecondOptionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlWhereGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * Keywords representing conditions such as `AND` or `OR`
 */
class SqlConditionKeywordGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSecondOptionKeywordGroupBlock(
    node,
    context,
) {
    var conditionalExpressionGroupBlock: SqlConditionalExpressionGroupBlock? = null

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlConditionalExpressionGroupBlock) {
            lastGroup.conditionKeywordGroupBlocks.add(this)
        }
    }

    // If AND appears after OR, change it so that it is right-justified.
    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            val groupLen = parent.indent.groupIndentLen
            return if (parent is SqlElConditionLoopCommentBlock) {
                parent.indent.groupIndentLen
            } else if (parent is SqlSubGroupBlock) {
                if (getNodeText() == "and") {
                    groupLen
                } else {
                    groupLen.plus(1)
                }
            } else {
                return parent.indent.groupIndentLen.minus(getNodeText().length)
            }
        } ?: return 1
    }

    override fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlWhereGroupBlock) {
                return indent.indentLen.plus(getNodeText().length)
            }
            return super.createGroupIndentLen()
        }
        return 0
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        if (lastGroup is SqlCreateKeywordGroupBlock) {
            return false
        }
        return super.isSaveSpace(lastGroup)
    }
}
