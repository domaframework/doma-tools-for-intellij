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
package org.domaframework.doma.intellij.formatter.block.group.keyword

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 *  Join Queries Keyword Group Block
 *  [UNION, INTERSECT, EXCEPT]
 */
class SqlJoinQueriesGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(node, IndentType.TOP, context) {
    // TODO Customize offset
    val offset = 0

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlWithQuerySubGroupBlock) {
                return parent.indent.groupIndentLen
            }
            return parent.indent.groupIndentLen.plus(1)
        }
        return offset
    }

    override fun createGroupIndentLen(): Int =
        topKeywordBlocks
            .sumOf { it.getNodeText().length.plus(1) }
            .plus(indent.indentLen)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true
}
