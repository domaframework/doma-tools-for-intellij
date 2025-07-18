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
package org.domaframework.doma.intellij.formatter.block.group.subgroup

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlLateralGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlFromGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlJoinQueriesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

abstract class SqlSubGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlNewGroupBlock(
        node,
        context,
    ) {
    companion object {
        private val NEW_LINE_EXPECTED_TYPES =
            listOf(
                SqlWithQueryGroupBlock::class,
                SqlWithCommonTableGroupBlock::class,
                SqlWithColumnGroupBlock::class,
                SqlCreateViewGroupBlock::class,
            )
    }

    open val offset = 1

    // TODO Even if the first element of a subgroup is a comment,
    //  the indentation of subsequent elements is now aligned.
    var isFirstLineComment = false
    var prevChildren: List<SqlBlock>? = emptyList<SqlBlock>()
    var endPatternBlock: SqlRightPatternBlock? = null

    override val indent =
        ElementIndent(
            IndentType.SUB,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        prevChildren = parentBlock?.childBlocks?.toList()
        indent.indentLevel = indent.indentLevel
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = parentBlock?.let { parent ->
            parent.indent.indentLen.plus(parent.getNodeText().length.plus(1))
        } ?: indent.indentLen.plus(getNodeText().length)
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlDoGroupBlock)?.doQueryBlock = this
        (lastGroup as? SqlLateralGroupBlock)?.subQueryGroupBlock = this
        if (lastGroup is SqlFromGroupBlock) {
            if (lastGroup.tableBlocks.isEmpty()) lastGroup.tableBlocks.add(this)
        }
    }

    override fun addChildBlock(childBlock: SqlBlock) {
        if (childBlocks.isEmpty()) {
            isFirstLineComment = childBlock is SqlCommentBlock
        }
        super.addChildBlock(childBlock)
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? = null

    override fun isLeaf(): Boolean = true

    open fun endGroup() {}

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlElConditionLoopCommentBlock) return parent.indent.groupIndentLen
        }
        return offset
    }

    override fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            // The parent groupIndent includes the number of characters in the group itself.
            val baseGroupLen = parent.indent.groupIndentLen
            return if (parent is SqlSubGroupBlock) baseGroupLen.plus(2) else baseGroupLen
        } ?: return 1
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        lastGroup?.let { lastBlock ->
            if (lastBlock is SqlJoinQueriesGroupBlock) return true
            return TypeUtil.isExpectedClassType(NEW_LINE_EXPECTED_TYPES, lastBlock.parentBlock)
        }
        return false
    }
}
