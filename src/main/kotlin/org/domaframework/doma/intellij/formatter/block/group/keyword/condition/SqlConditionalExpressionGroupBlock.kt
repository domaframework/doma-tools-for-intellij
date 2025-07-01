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
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * A grouped conditional expression following keywords such as AND or OR
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
        if (lastGroup is SqlConditionKeywordGroupBlock) {
            lastGroup.conditionalExpressionGroupBlock = this
        }
    }

    /**
     * Processing applied to the entire group when group processing ends.
     * If there are AND or OR group blocks, set the indentation so that the keywords are right-aligned.
     */
    override fun endGroup() {
        val thisGroupIndent = indent.groupIndentLen
        if (conditionKeywordGroupBlocks.isNotEmpty()) {
            conditionKeywordGroupBlocks.forEach { conditionBlock ->
                conditionBlock.indent.indentLen =
                    when (conditionBlock.getNodeText()) {
                        "and" -> thisGroupIndent.plus(1)
                        "or" -> thisGroupIndent.plus(2)
                        else -> conditionBlock.indent.indentLen
                    }
            }
        }
    }
}
