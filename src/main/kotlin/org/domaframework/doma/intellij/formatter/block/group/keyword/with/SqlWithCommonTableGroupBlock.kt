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
package org.domaframework.doma.intellij.formatter.block.group.keyword.with

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlWordBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlWithCommonTableGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(node, context) {
    override val offset = 4

    var commonTableNameBlock: SqlBlock? = getCommonTableName()
    var columnGroupBlock: SqlWithColumnGroupBlock? = null
    val optionKeywordBlocks: MutableList<SqlBlock> = mutableListOf()
    val queryGroupBlock: MutableList<SqlBlock> = mutableListOf()
    var isFirstTable = false

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
        isFirstTable = findWithQueryChildBlocks() == null
    }

    private fun getCommonTableName(): SqlBlock? {
        if (getNodeText() == ",") return null
        val expectedTypes =
            listOf(
                SqlBlockCommentBlock::class,
                SqlWordBlock::class,
            )
        if (TypeUtil.isExpectedClassType(expectedTypes, this)) return this
        return null
    }

    private fun findWithQueryChildBlocks(): SqlBlock? {
        parentBlock?.let { parent ->
            if (parent is SqlWithQueryGroupBlock) {
                return parent.getChildBlocksDropLast().find { it is SqlWithCommonTableGroupBlock }
            }
        }
        return null
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlWithQueryGroupBlock) {
            lastGroup.commonTableBlocks.add(this)
        }
        if (lastGroup is SqlWithCommonTableGroupBlock) {
            (lastGroup.parentBlock as? SqlWithQueryGroupBlock)?.commonTableBlocks?.add(this)
        }
    }

    override fun createBlockIndentLen(): Int = 0

    override fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            getChildBlocksDropLast().sumOf { it.getNodeText().length.plus(1) }
        }
        return offset
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = !isFirstTable
}
