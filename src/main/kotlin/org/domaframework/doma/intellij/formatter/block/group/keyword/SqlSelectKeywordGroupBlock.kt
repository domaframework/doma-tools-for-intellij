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
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlSelectKeywordGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(node, IndentType.TOP, context) {
    val secondGroupBlocks: MutableList<SqlKeywordGroupBlock> = mutableListOf()
    val selectionColumns: MutableList<SqlBlock> = mutableListOf()

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        val preChildBlock = lastGroup?.childBlocks?.dropLast(1)?.lastOrNull()
        indent.indentLevel = indentLevel

        val baseIndentLen = getBaseIndentLen(preChildBlock, lastGroup)
        indent.groupIndentLen = baseIndentLen.plus(getNodeText().length)
        indent.indentLen = adjustIndentIfFirstChildIsLineComment(baseIndentLen)
        createGroupIndentLen()
    }

    override fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        block: SqlBlock?,
    ): Int {
        if (parentBlock is SqlSubGroupBlock) {
            return parentBlock?.indent?.groupIndentLen
                ?: createBlockIndentLen(preChildBlock)
        }
        return createBlockIndentLen(preChildBlock)
    }

    override fun createBlockIndentLen(preChildBlock: SqlBlock?): Int =
        if (parentBlock?.indent?.indentLevel == IndentType.FILE) {
            0
        } else {
            parentBlock?.indent?.groupIndentLen ?: 0
        }

    override fun createGroupIndentLen(): Int = 0
}
