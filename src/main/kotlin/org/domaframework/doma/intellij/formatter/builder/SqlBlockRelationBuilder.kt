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
package org.domaframework.doma.intellij.formatter.builder

import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlInGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlLateralGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlReturningGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTopQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlFunctionGroupBlock
import org.domaframework.doma.intellij.formatter.handler.UpdateClauseHandler
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Processor responsible for establishing parent-child relationships between SQL formatting blocks
 * and managing the block hierarchy during SQL formatting.
 */
class SqlBlockRelationBuilder(
    private val blockBuilder: SqlBlockBuilder,
) {
    data class SetParentContext(
        val childBlock: SqlBlock,
        val blockBuilder: SqlBlockBuilder,
    )

    companion object {
        private val NEW_GROUP_EXPECTED_TYPES =
            listOf(
                SqlSubGroupBlock::class,
                SqlCreateViewGroupBlock::class,
                SqlInlineGroupBlock::class,
                SqlInlineSecondGroupBlock::class,
                SqlColumnDefinitionRawGroupBlock::class,
                SqlLateralGroupBlock::class,
                SqlInGroupBlock::class,
            )

        private val TOP_LEVEL_EXPECTED_TYPES =
            listOf(
                SqlSubGroupBlock::class,
                SqlCreateViewGroupBlock::class,
            )

        private val COLUMN_RAW_EXPECTED_TYPES =
            listOf(
                SqlColumnRawGroupBlock::class,
            )
    }

    /**
     * Sets the parent of the latest group block and registers itself as a child element.
     */
    fun updateGroupBlockParentAndAddGroup(childBlock: SqlBlock) {
        val context =
            SetParentContext(
                childBlock,
                blockBuilder,
            )
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()
        if (lastGroup is SqlElConditionLoopCommentBlock) {
            updateParentGroupLastConditionLoop(lastGroup, context) { block ->
                block.indent.indentLevel < childBlock.indent.indentLevel
            }
            return
        }
        setParentGroups(context) { history ->
            return@setParentGroups history.lastOrNull()
        }
    }

    /**
     * Does not set a parent.
     */
    fun updateGroupBlockAddGroup(childBlock: SqlBlock) {
        setParentGroups(
            SetParentContext(
                childBlock,
                blockBuilder,
            ),
        ) { history ->
            return@setParentGroups null
        }
    }

    /**
     * Registers itself as a child element in the same block as the parent of the last group at the time of processing.
     */
    private fun updateGroupBlockLastGroupParentAddGroup(
        lastGroupBlock: SqlBlock,
        childBlock: SqlBlock,
    ) {
        setParentGroups(
            SetParentContext(
                childBlock,
                blockBuilder,
            ),
        ) { history ->
            return@setParentGroups lastGroupBlock.parentBlock
        }
    }

    fun updateKeywordGroupBlockParentAndAddGroup(
        lastGroupBlock: SqlBlock,
        lastIndentLevel: IndentType,
        childBlock: SqlKeywordGroupBlock,
    ) {
        val context = SetParentContext(childBlock, blockBuilder)
        if (lastGroupBlock is SqlElConditionLoopCommentBlock) {
            handleConditionLoopParent(lastGroupBlock, context, childBlock)
        } else if (childBlock.indent.indentLevel == IndentType.TOP) {
            handleTopLevelKeyword(lastGroupBlock, childBlock, context)
        } else {
            handleNonTopLevelKeyword(lastGroupBlock, lastIndentLevel, childBlock, context)
        }
    }

    private fun handleConditionLoopParent(
        lastGroupBlock: SqlElConditionLoopCommentBlock,
        context: SetParentContext,
        childBlock: SqlKeywordGroupBlock,
    ) {
        updateParentGroupLastConditionLoop(lastGroupBlock, context) {
            it.indent.indentLevel < childBlock.indent.indentLevel
        }
    }

    private fun handleTopLevelKeyword(
        lastGroupBlock: SqlBlock,
        childBlock: SqlKeywordGroupBlock,
        context: SetParentContext,
    ) {
        val parentBlock =
            if (TypeUtil.isExpectedClassType(TOP_LEVEL_EXPECTED_TYPES, lastGroupBlock)) {
                lastGroupBlock
            } else if (childBlock is SqlUpdateQueryGroupBlock) {
                UpdateClauseHandler.getParentGroupBlock(blockBuilder, childBlock)
            } else {
                findTopLevelParent()
            }
        setParentGroups(context) { return@setParentGroups parentBlock }
    }

    private fun findTopLevelParent(): SqlBlock? {
        val topKeywordIndex =
            blockBuilder.getGroupTopNodeIndex {
                it.indent.indentLevel == IndentType.TOP
            }
        val subGroupIndex =
            blockBuilder.getGroupTopNodeIndex {
                it is SqlSubGroupBlock
            }

        val (parentBlock, deleteIndex) =
            when {
                topKeywordIndex >= 0 && subGroupIndex < 0 -> {
                    val block = blockBuilder.getGroupTopNodeIndexHistory()[topKeywordIndex]
                    block.parentBlock to topKeywordIndex
                }
                topKeywordIndex > subGroupIndex -> {
                    val block = blockBuilder.getGroupTopNodeIndexHistory()[subGroupIndex]
                    block to topKeywordIndex
                }
                else -> null to -1
            }

        if (deleteIndex >= 0) {
            blockBuilder.clearSubListGroupTopNodeIndexHistory(deleteIndex)
        }

        return parentBlock
    }

    private fun handleNonTopLevelKeyword(
        lastGroupBlock: SqlBlock,
        lastIndentLevel: IndentType,
        childBlock: SqlKeywordGroupBlock,
        context: SetParentContext,
    ) {
        when {
            lastGroupBlock.indent.indentLevel == IndentType.SUB -> {
                setParentGroups(context) { lastGroupBlock }
            }
            lastIndentLevel == childBlock.indent.indentLevel -> {
                handleSameLevelKeyword(lastGroupBlock, childBlock, context)
            }
            lastIndentLevel < childBlock.indent.indentLevel -> {
                updateGroupBlockParentAndAddGroup(childBlock)
            }
            shouldHandleJoinKeyword(lastIndentLevel, childBlock) -> {
                updateGroupBlockParentAndAddGroup(childBlock)
            }
            else -> {
                setParentGroups(context) { history ->
                    history.lastOrNull { it.indent.indentLevel < childBlock.indent.indentLevel }
                }
            }
        }
    }

    private fun handleSameLevelKeyword(
        lastGroupBlock: SqlBlock,
        childBlock: SqlKeywordGroupBlock,
        context: SetParentContext,
    ) {
        val prevKeyword = lastGroupBlock.childBlocks.findLast { it is SqlKeywordBlock }
        if (prevKeyword != null && SqlKeywordUtil.Companion.isSetLineKeyword(childBlock.getNodeText(), prevKeyword.getNodeText())) {
            updateGroupBlockLastGroupParentAddGroup(lastGroupBlock, childBlock)
            return
        }

        blockBuilder.removeLastGroupTopNodeIndexHistory()

        if (childBlock is SqlReturningGroupBlock) {
            handleReturningBlock(context)
        } else {
            updateGroupBlockLastGroupParentAddGroup(lastGroupBlock, childBlock)
        }
    }

    private fun handleReturningBlock(context: SetParentContext) {
        setParentGroups(context) { history ->
            val lastGroup = history.findLast { it is SqlTopQueryGroupBlock }
            when {
                lastGroup is SqlUpdateQueryGroupBlock && lastGroup.parentBlock is SqlDoGroupBlock ->
                    lastGroup.parentBlock
                else -> lastGroup
            }
        }
    }

    private fun shouldHandleJoinKeyword(
        lastIndentLevel: IndentType,
        childBlock: SqlKeywordGroupBlock,
    ): Boolean = lastIndentLevel == IndentType.JOIN && SqlKeywordUtil.Companion.isSecondOptionKeyword(childBlock.getNodeText())

    fun updateColumnDefinitionRawGroupBlockParentAndAddGroup(
        lastGroupBlock: SqlBlock,
        lastIndentLevel: IndentType,
        childBlock: SqlColumnDefinitionRawGroupBlock,
    ) {
        when (lastIndentLevel) {
            childBlock.indent.indentLevel -> {
                blockBuilder.removeLastGroupTopNodeIndexHistory()
                updateGroupBlockLastGroupParentAddGroup(
                    lastGroupBlock,
                    childBlock,
                )
            }

            else -> {
                updateGroupBlockParentAndAddGroup(childBlock)
            }
        }
    }

    fun updateColumnRawGroupBlockParentAndAddGroup(
        lastGroupBlock: SqlBlock,
        childBlock: SqlColumnRawGroupBlock,
    ) {
        val context =
            SetParentContext(
                childBlock,
                blockBuilder,
            )

        if (lastGroupBlock is SqlElConditionLoopCommentBlock) {
            updateParentGroupLastConditionLoop(lastGroupBlock, context) {
                it.indent.indentLevel < childBlock.indent.indentLevel
            }
            return
        }

        if (TypeUtil.isExpectedClassType(COLUMN_RAW_EXPECTED_TYPES, lastGroupBlock)) {
            blockBuilder.removeLastGroupTopNodeIndexHistory()
        }
        setParentGroups(context) { history ->
            return@setParentGroups history
                .lastOrNull { it.indent.indentLevel < childBlock.indent.indentLevel }
        }
    }

    fun updateInlineSecondGroupBlockParentAndAddGroup(childBlock: SqlInlineSecondGroupBlock) {
        val context =
            SetParentContext(
                childBlock,
                blockBuilder,
            )
        if (childBlock.isEndCase) {
            val inlineIndex =
                blockBuilder.getGroupTopNodeIndex { block ->
                    block.indent.indentLevel == IndentType.INLINE
                }
            if (inlineIndex >= 0) {
                setParentGroups(
                    context,
                ) { history ->
                    return@setParentGroups history[inlineIndex]
                }
                blockBuilder.clearSubListGroupTopNodeIndexHistory(inlineIndex)
            }
            return
        }

        val lastGroupBlock = blockBuilder.getLastGroupTopNodeIndexHistory()
        if (lastGroupBlock is SqlElConditionLoopCommentBlock) {
            updateParentGroupLastConditionLoop(lastGroupBlock, context) {
                it.indent.indentLevel == IndentType.INLINE_SECOND
            }
            return
        }

        val inlineSecondIndex =
            blockBuilder.getGroupTopNodeIndex { block ->
                block.indent.indentLevel == IndentType.INLINE_SECOND
            }
        if (inlineSecondIndex >= 0) {
            blockBuilder.clearSubListGroupTopNodeIndexHistory(inlineSecondIndex)
        }
        updateGroupBlockParentAndAddGroup(
            childBlock,
        )
    }

    /**
     * Updates the parent of the last conditional directive block and sets the parent of the current block.
     */
    private fun updateParentGroupLastConditionLoop(
        lastGroupBlock: SqlElConditionLoopCommentBlock,
        context: SetParentContext,
        findDefaultParent: (SqlBlock) -> Boolean,
    ) {
        if (lastGroupBlock.parentBlock != null) {
            setParentGroups(context) { lastGroupBlock }
            return
        }

        val findParent = findParentForConditionLoop(findDefaultParent)
        handleConditionLoopParentAssignment(lastGroupBlock, context, findParent)
    }

    private fun findParentForConditionLoop(findDefaultParent: (SqlBlock) -> Boolean): SqlBlock? =
        blockBuilder.getGroupTopNodeIndexHistory().lastOrNull { block ->
            findDefaultParent(block) ||
                block is SqlSubGroupBlock ||
                (block is SqlElConditionLoopCommentBlock && block.parentBlock != null)
        }

    private fun handleConditionLoopParentAssignment(
        lastGroupBlock: SqlElConditionLoopCommentBlock,
        context: SetParentContext,
        findParent: SqlBlock?,
    ) {
        when (findParent) {
            is SqlElConditionLoopCommentBlock -> {
                lastGroupBlock.setParentGroupBlock(findParent)
                setParentGroups(context) { lastGroupBlock }
            }
            else -> {
                setParentGroups(context) { findParent }
                lastGroupBlock.setParentGroupBlock(context.childBlock)
            }
        }
    }

    fun updateConditionLoopCommentBlockParent(
        lastGroupBlock: SqlBlock,
        childBlock: SqlElConditionLoopCommentBlock,
    ) {
        val context =
            SetParentContext(
                childBlock,
                blockBuilder,
            )
        setParentGroups(context) { history ->
            if (childBlock.conditionType.isEnd() || childBlock.conditionType.isElse()) {
                // remove self and previous conditional directive block
                blockBuilder.removeConditionOrLoopBlockLast()
                blockBuilder.removeConditionOrLoopBlockLast()

                val directiveIndex =
                    blockBuilder
                        .getGroupTopNodeIndex { it is SqlElConditionLoopCommentBlock && it != childBlock }
                if (directiveIndex >= 0) {
                    val lastConditionLoopCommentBlock =
                        blockBuilder.getGroupTopNodeIndexHistory()[directiveIndex]
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(directiveIndex)
                    return@setParentGroups lastConditionLoopCommentBlock
                }
                return@setParentGroups null
            }

            // If the most recent block is a conditional directive, set it as the parent block.
            if (lastGroupBlock is SqlElConditionLoopCommentBlock) {
                val prevGroupIndex = blockBuilder.getGroupTopNodeIndex { it == lastGroupBlock }
                if (prevGroupIndex > 0 && lastGroupBlock.parentBlock == null) {
                    // Determine the parent of the most recent conditional directive.
                    val prevGroup = blockBuilder.getGroupTopNodeIndexHistory()[prevGroupIndex - 1]
                    lastGroupBlock.setParentGroupBlock(prevGroup)
                }
                return@setParentGroups lastGroupBlock
            }
            // Temporary Parent Block
            return@setParentGroups history.lastOrNull()
        }
    }

    fun updateSqlRightPatternBlockParent(childBlock: SqlRightPatternBlock) {
        val paramIndex =
            blockBuilder.getGroupTopNodeIndex { block ->
                block is SqlSubGroupBlock
            }
        if (paramIndex >= 0) {
            val context =
                SetParentContext(
                    childBlock,
                    blockBuilder,
                )
            setParentGroups(context) { history ->
                return@setParentGroups history[paramIndex]
            }

            if (blockBuilder.getGroupTopNodeIndexHistory()[paramIndex] is SqlWithQuerySubGroupBlock) {
                val withCommonBlockIndex =
                    blockBuilder.getGroupTopNodeIndex { block ->
                        block is SqlWithCommonTableGroupBlock
                    }
                if (withCommonBlockIndex >= 0) {
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(withCommonBlockIndex)
                }
            } else {
                blockBuilder.clearSubListGroupTopNodeIndexHistory(paramIndex)
            }
        }
    }

    fun updateSubGroupBlockParent(
        lastGroupBlock: SqlBlock,
        childBlock: SqlSubGroupBlock,
    ) {
        val prevBlock = lastGroupBlock.childBlocks.lastOrNull()
        if (prevBlock is SqlFunctionGroupBlock) {
            setParentGroups(
                SetParentContext(
                    childBlock,
                    blockBuilder,
                ),
            ) { history ->
                return@setParentGroups prevBlock
            }
            return
        }
        updateGroupBlockParentAndAddGroup(childBlock)
    }

    /**
     * Determines its parent group and, if conditions are met, registers itself as a new group block in the list.
     */
    private fun setParentGroups(
        context: SetParentContext,
        getParentGroup: (MutableList<SqlBlock>) -> SqlBlock?,
    ) {
        val parentGroup =
            getParentGroup(context.blockBuilder.getGroupTopNodeIndexHistory() as MutableList<SqlBlock>)

        val targetChildBlock = context.childBlock

        if (targetChildBlock is SqlDefaultCommentBlock) return

        // The parent block for SqlElConditionLoopCommentBlock will be set later
        if (targetChildBlock is SqlElConditionLoopCommentBlock && targetChildBlock.conditionType.isStartDirective()) {
            targetChildBlock.tempParentBlock = parentGroup
            if ((parentGroup is SqlElConditionLoopCommentBlock || parentGroup is SqlSubGroupBlock) && parentGroup.parentBlock != null) {
                targetChildBlock.setParentGroupBlock(parentGroup)
            }
        } else {
            targetChildBlock.setParentGroupBlock(parentGroup)
        }

        if (isNewGroup(targetChildBlock, context.blockBuilder) ||
            TypeUtil.isExpectedClassType(NEW_GROUP_EXPECTED_TYPES, targetChildBlock)
        ) {
            context.blockBuilder.addGroupTopNodeIndexHistory(targetChildBlock)
        }

        context.blockBuilder.updateCommentBlockIndent(targetChildBlock)
        // Set parent-child relationship and indent for preceding comment at beginning of block group
        context.blockBuilder.updateConditionLoopBlockIndent(targetChildBlock)
    }

    /**
     * Determines whether it is a group that requires a line break or a specific combination of keywords that does not require a line break.
     */
    private fun isNewGroup(
        childBlock: SqlBlock,
        blockBuilder: SqlBlockBuilder,
    ): Boolean {
        if (isConditionLoopNewGroup(childBlock)) {
            return true
        }

        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()
        return isNewGroupAndNotSetLineKeywords(childBlock, lastGroup)
    }

    private fun isConditionLoopNewGroup(childBlock: SqlBlock): Boolean =
        childBlock is SqlElConditionLoopCommentBlock &&
            (childBlock.conditionType.isStartDirective() || childBlock.conditionType.isElse())

    fun isNewGroupAndNotSetLineKeywords(
        childBlock: SqlBlock,
        lastGroup: SqlBlock?,
    ): Boolean {
        val isNewGroupType = childBlock.indent.indentLevel.isNewLineGroup()
        val lastKeywordText =
            if (lastGroup?.indent?.indentLevel == IndentType.JOIN) {
                lastGroup.getNodeText()
            } else {
                getLastGroupKeywordText(lastGroup)
            }

        val isSetLineGroup =
            SqlKeywordUtil.Companion.isSetLineKeyword(
                childBlock.getNodeText(),
                lastKeywordText,
            )
        return isNewGroupType && !isSetLineGroup
    }

    /**
     * Searches for a keyword element in the most recent group block and returns its text.
     * If not found, returns the text of the group block itself.
     */
    private fun getLastGroupKeywordText(lastGroup: SqlBlock?): String {
        if (lastGroup == null) return ""

        val keywordBlock =
            lastGroup.childBlocks.lastOrNull {
                it.node.elementType == SqlTypes.KEYWORD
            }

        return keywordBlock?.getNodeText() ?: lastGroup.getNodeText()
    }
}
