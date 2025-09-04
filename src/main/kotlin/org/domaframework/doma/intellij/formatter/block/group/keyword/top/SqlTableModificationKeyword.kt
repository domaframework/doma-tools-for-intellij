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
package org.domaframework.doma.intellij.formatter.block.group.keyword.top

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlTableModificationKeyword(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlTopQueryGroupBlock(
        node,
        context,
    ) {
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        return parentBlock?.let { parent ->
            val rootBlock =
                if (parent.indent.indentLevel == IndentType.FILE) {
                    null
                } else {
                    parent
                }

            return if (rootBlock is SqlKeywordGroupBlock) {
                rootBlock.getTotalTopKeywordLength().minus(getNodeText().length)
            } else {
                rootBlock?.indent?.groupIndentLen ?: 0
            }
        } ?: 0
    }

    override fun createGroupIndentLen(): Int = getTotalTopKeywordLength()
}
