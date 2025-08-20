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
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

open class SqlSecondOptionKeywordGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(node, IndentType.SECOND_OPTION, context) {
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            val groupLen = parent.indent.groupIndentLen
            if (parent.indent.indentLevel == IndentType.FILE) {
                return 0
            }
            if (parent is SqlElConditionLoopCommentBlock) return groupLen
            val subGroupBlock = parent.parentBlock as? SqlSubGroupBlock
            val newIndent =
                if (parent is SqlSubQueryGroupBlock) {
                    groupLen.plus(1)
                } else if (parent is SqlKeywordGroupBlock && subGroupBlock != null && subGroupBlock.isFirstLineComment) {
                    groupLen
                } else {
                    return parent.indent.groupIndentLen
                        .minus(getNodeText().length)
                }
            return newIndent
        } ?: 1
        return 1
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        val prevKeyword = lastGroup?.childBlocks?.dropLast(1)?.findLast { it is SqlKeywordBlock }
        return !SqlKeywordUtil.isSetLineKeyword(getNodeText(), prevKeyword?.getNodeText() ?: "")
    }
}
