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
package org.domaframework.doma.intellij.formatter.block.word

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import kotlin.collections.emptyList
import kotlin.collections.toList

class SqlFunctionGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlWordBlock(node, context) {
    var parameterGroupBlock: SqlFunctionParamBlock? = null
    var prevChildren: List<SqlBlock> = emptyList()

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        prevChildren = lastGroup?.childBlocks?.toList() ?: emptyList()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int = parentBlock?.indent?.groupIndentLen?.plus(1) ?: 0

    override fun createGroupIndentLen(): Int {
        val baseIndent =
            parentBlock?.let { parent ->
                val children = prevChildren.dropLast(1).filter { it !is SqlDefaultCommentBlock }
                val prevBlocksLength = calculatePrevBlocksLength(children, parent)
                calculateBaseIndent(parent, prevBlocksLength)
            } ?: 0
        return baseIndent.plus(getNodeText().length)
    }

    private fun calculateBaseIndent(
        parent: SqlBlock,
        prevBlocksLength: Int,
    ): Int =
        when (parent) {
            is SqlSubGroupBlock ->
                prevBlocksLength

            else -> prevBlocksLength.plus(1)
        }
}
