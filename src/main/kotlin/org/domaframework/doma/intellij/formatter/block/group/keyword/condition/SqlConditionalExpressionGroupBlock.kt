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
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * A grouped conditional expression following keywords such as `AND` or `OR`
 */
class SqlConditionalExpressionGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    val conditionKeywordGroupBlocks: MutableList<SqlKeywordGroupBlock> = mutableListOf()

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlConditionKeywordGroupBlock) {
            lastGroup.conditionalExpressionGroupBlock = this
        }
    }

    override fun createBlockIndentLen(): Int =
        parentBlock?.let { parent ->
            if (parent is SqlElConditionLoopCommentBlock) {
                val groupIndentLen = parent.indent.groupIndentLen
                val grand = parent.parentBlock
                val directiveParentTextLen =
                    if (grand !is SqlElConditionLoopCommentBlock) {
                        grand
                            ?.getNodeText()
                            ?.length
                            ?.plus(1) ?: 0
                    } else {
                        0
                    }
                groupIndentLen + directiveParentTextLen
            } else {
                parent.indent.groupIndentLen.plus(1)
            }
        }
            ?: offset

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(1)
}
