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
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlSelectQueryGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlTopQueryGroupBlock(
        node,
        context,
    ) {
    val secondGroupBlocks: MutableList<SqlKeywordGroupBlock> = mutableListOf()
    val selectionColumns: MutableList<SqlBlock> = mutableListOf()

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        parentBlock = lastGroup
        parentBlock?.addChildBlock(this)
        setParentPropertyBlock(lastGroup)
        indent.indentLevel = indentLevel
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        lastGroup: SqlBlock?,
    ): Int {
        if (parentBlock is SqlSubGroupBlock) {
            return parentBlock?.indent?.groupIndentLen
                ?: createBlockIndentLen(preChildBlock)
        }
        return createBlockIndentLen(preChildBlock)
    }

    override fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlSubQueryGroupBlock) {
                return indent.indentLen.plus(getNodeText().length)
            }
        }
        return indent.indentLen.plus(getNodeText().length)
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        lastGroup?.let { lastBlock ->
            if (lastGroup is SqlWithQuerySubGroupBlock) return true
            if (lastBlock is SqlSubGroupBlock) return lastBlock.childBlocks.dropLast(1).isNotEmpty()
        }
        return true
    }
}
