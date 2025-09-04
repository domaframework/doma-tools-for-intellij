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
import org.domaframework.doma.intellij.formatter.block.comma.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlExistsGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlInGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlLateralGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlReturningGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlTableModifySecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTopQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlAliasBlock
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
                SqlExistsGroupBlock::class,
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
        setParentGroups(context) { history ->
            return@setParentGroups history.lastOrNull()
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
        if (childBlock.indent.indentLevel == IndentType.TOP) {
            handleTopLevelKeyword(lastGroupBlock, childBlock, context)
            return
        }

        handleNonTopLevelKeyword(lastGroupBlock, lastIndentLevel, childBlock, context)
    }

    private fun handleTopLevelKeyword(
        lastGroupBlock: SqlBlock,
        childBlock: SqlKeywordGroupBlock,
        context: SetParentContext,
    ) {
        val parentBlock =
            if (TypeUtil.isTopLevelExpectedType(lastGroupBlock)) {
                lastGroupBlock
            } else if (childBlock is SqlUpdateQueryGroupBlock) {
                UpdateClauseHandler.getParentGroupBlock(blockBuilder, childBlock)
            } else {
                findTopLevelParent()
            }
        setParentGroups(context) { return@setParentGroups parentBlock }
    }

    /**
     * Search for groups before the top-level keyword, select the parent, and swap the list
     */
    private fun findTopLevelParent(): SqlBlock? {
        val topKeywordIndex =
            blockBuilder.getGroupTopNodeIndex {
                it.indent.indentLevel == IndentType.TOP
            }
        val subGroupIndex =
            blockBuilder.getGroupTopNodeIndex {
                it is SqlSubGroupBlock
            }

        // Determine the parent block based on the order of appearance of top-level keywords or subqueries
        val (parentBlock, deleteIndex) =
            when {
                // If only a top-level keyword is found, get the same parent as it
                topKeywordIndex >= 0 && subGroupIndex < 0 -> {
                    val block = blockBuilder.getGroupTopNodeIndexHistory()[topKeywordIndex]
                    block.parentBlock to topKeywordIndex
                }
                // If a top-level keyword is found before a subquery, make the subquery group the parent
                topKeywordIndex > subGroupIndex -> {
                    val block = blockBuilder.getGroupTopNodeIndexHistory()[subGroupIndex]
                    block to topKeywordIndex
                }
                // If neither a subquery nor a top-level keyword is found, don't set a parent
                else -> null to -1
            }

        // Remove top-level keywords from the group list
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
            childBlock is SqlTableModifySecondGroupBlock ->
                setParentGroups(context) { history ->
                    history.lastOrNull {
                        it is SqlColumnRawGroupBlock ||
                            it.indent.indentLevel < childBlock.indent.indentLevel
                    }
                }
            else -> {
                setParentGroups(context) { history ->
                    getLastGroupKeywordText(history, childBlock) ?: history.lastOrNull()
                }
            }
        }
    }

    /**
     * Searches for the most recent group block with a lower indent level than the current block
     * and returns it as a candidate for the parent block.
     *
     * @note
     * If the most recent group block is a comma, it checks its parent and grandparent blocks
     * to determine the appropriate parent block.
     *
     * @example
     *  OVER(ORDER BY e.id, e.manager_id, created_at
     *       ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING)
     */
    private fun getLastGroupKeywordText(
        history: MutableList<SqlBlock>,
        childBlock: SqlBlock,
    ): SqlBlock? {
        val lastGroupBlock =
            history.lastOrNull {
                it.indent.indentLevel < childBlock.indent.indentLevel ||
                    // Add [SqlColumnRawGroupBlock] as a parent block candidate
                    // to support cases like WITHIN GROUP used in column lines.
                    TypeUtil.isTopLevelExpectedType(it)
            }
        if (lastGroupBlock == null) return lastGroupBlock

        if (lastGroupBlock.indent.indentLevel > childBlock.indent.indentLevel && lastGroupBlock !is SqlSubGroupBlock) {
            val lastParent = lastGroupBlock.parentBlock
            val lastGroupParentLevel = lastParent?.indent?.indentLevel ?: IndentType.NONE
            val lastKeyword =
                lastGroupBlock.childBlocks
                    .lastOrNull {
                        it is SqlKeywordBlock || it is SqlKeywordGroupBlock
                    }?.getNodeText() ?: ""
            val setKeyword = SqlKeywordUtil.isSetLineKeyword(childBlock.getNodeText(), lastKeyword)
            if (lastGroupParentLevel < childBlock.indent.indentLevel) {
                return if (setKeyword) {
                    lastGroupBlock
                } else {
                    lastParent
                }
            }
            return lastParent?.parentBlock
        }
        return lastGroupBlock
    }

    private fun handleSameLevelKeyword(
        lastGroupBlock: SqlBlock,
        childBlock: SqlKeywordGroupBlock,
        context: SetParentContext,
    ) {
        val prevKeyword = lastGroupBlock.childBlocks.findLast { it is SqlKeywordBlock }
        if (prevKeyword != null &&
            SqlKeywordUtil.isSetLineKeyword(
                childBlock.getNodeText(),
                prevKeyword.getNodeText(),
            )
        ) {
            updateGroupBlockLastGroupParentAddGroup(prevKeyword, childBlock)
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
                lastGroup is SqlUpdateQueryGroupBlock &&
                    lastGroup.parentBlock is SqlDoGroupBlock ->
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

        val caseBlockIndex =
            blockBuilder.getGroupTopNodeIndex { block ->
                block.indent.indentLevel == IndentType.INLINE
            }
        val caseBlock =
            if (caseBlockIndex >= 0) {
                blockBuilder.getGroupTopNodeIndexHistory()[caseBlockIndex]
            } else {
                null
            }
        val inlineSecondIndex =
            blockBuilder.getGroupTopNodeIndex { block ->
                (caseBlock?.node?.startOffset ?: 0) < block.node.startOffset &&
                    block.indent.indentLevel == IndentType.INLINE_SECOND
            }
        if (inlineSecondIndex >= 0) {
            blockBuilder.clearSubListGroupTopNodeIndexHistory(inlineSecondIndex)
        }
        updateGroupBlockParentAndAddGroup(
            childBlock,
        )
    }

    fun updateSqlBlockAndOverIndentLevel(
        childBlock: SqlCommaBlock,
        findSubQuery: Boolean = false,
    ) {
        val context =
            SetParentContext(
                childBlock,
                blockBuilder,
            )
        setParentGroups(context) { history ->
            return@setParentGroups history.findLast {
                it.indent.indentLevel < childBlock.indent.indentLevel ||
                    (findSubQuery && it is SqlSubGroupBlock)
            }
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

            val parentSubGroup = blockBuilder.getGroupTopNodeIndexHistory()[paramIndex]
            if (parentSubGroup is SqlWithQuerySubGroupBlock) {
                val withCommonBlockIndex =
                    blockBuilder.getGroupTopNodeIndex { block ->
                        block is SqlWithCommonTableGroupBlock
                    }
                if (withCommonBlockIndex >= 0) {
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(withCommonBlockIndex)
                }
                return
            }
            if (parentSubGroup is SqlFunctionParamBlock) {
                // If the parent is a function parameter group, remove up to the parent function name and keyword group.
                val parent = blockBuilder.getGroupTopNodeIndexHistory()[paramIndex].parentBlock
                val searchFunctionName =
                    if (parent is SqlElConditionLoopCommentBlock) {
                        parent.parentBlock
                    } else {
                        parent
                    }
                val functionParent =
                    blockBuilder.getGroupTopNodeIndex {
                        it == searchFunctionName
                    }
                if (functionParent >= 0) {
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(functionParent)
                    return
                }
            }
            blockBuilder.clearSubListGroupTopNodeIndexHistory(paramIndex)
        }
    }

    fun updateSubGroupBlockParent(
        lastGroupBlock: SqlBlock,
        childBlock: SqlSubGroupBlock,
    ) {
        val prevBlock = lastGroupBlock.childBlocks.lastOrNull()
        if (prevBlock is SqlFunctionGroupBlock || prevBlock is SqlAliasBlock) {
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
     * Determines its parent group and, if conditions are met,
     * registers itself as a new group block in the list.
     */
    private fun setParentGroups(
        context: SetParentContext,
        getParentGroup: (MutableList<SqlBlock>) -> SqlBlock?,
    ) {
        val parentGroup =
            getParentGroup(
                context.blockBuilder.getGroupTopNodeIndexHistory() as MutableList<SqlBlock>,
            )
        val targetChildBlock = context.childBlock
        if (targetChildBlock is SqlDefaultCommentBlock) return

        // If a value can be retrieved from the condition/loop directive list,
        // first calculate the original indent of the current block
        val dependDirective = blockBuilder.getLastConditionOrLoopBlock()
        if (targetChildBlock !is SqlElConditionLoopCommentBlock) {
            setParentNonLoopConditionDirective(
                context,
                targetChildBlock,
                parentGroup,
                dependDirective,
            )
        } else {
            val nestParentBlock = blockBuilder.getLastConditionOrLoopBlock()
            targetChildBlock.nestParentBlock = nestParentBlock
            setParentLoopConditionDirective(
                targetChildBlock,
                parentGroup,
                dependDirective,
            )
        }
        context.blockBuilder.updateCommentBlockIndent(targetChildBlock)
    }

    /**
     * Set parent-child relationships for non-condition/loop directives
     */
    private fun setParentNonLoopConditionDirective(
        context: SetParentContext,
        targetChildBlock: SqlBlock,
        parentGroup: SqlBlock?,
        dependDirective: SqlElConditionLoopCommentBlock?,
    ) {
        // If the child block is at the same level as the previous block,
        // the condition/loop directive makes the upper block that was retained its parent
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()
        val lastGroupLevel = lastGroup?.indent?.indentLevel ?: IndentType.FILE
        val inlineDirective = lastGroupLevel >= targetChildBlock.indent.indentLevel && dependDirective?.nestParentBlock != null
        if (inlineDirective) {
            dependDirective.setParentSelfNestBlock()
        }

        // If dependDirective has a parent set (nested), align the indent to dependDirective
        // If dependDirective doesn't have a parent set, align dependDirective to its own indent
        val lastParentChild = parentGroup?.childBlocks?.lastOrNull()
        targetChildBlock.setParentGroupBlock(parentGroup)
        if (dependDirective != null) {
            targetChildBlock.createBlockIndentLenDirective(parentGroup, dependDirective)
        }
        // Within condition/loop directives, if there is a parent condition/loop directive but the parent's dependency is different from targetChildBlock's parent,
        // release the parent condition/loop directive and perform normal condition/loop directive association
        val dependDirectiveParent = dependDirective?.parentBlock
        // If targetChildBlock is a group block and the last group block differs from parentGroup,
        // reset the parent-child relationship of the previous condition/loop directive and start a new nest
        var recalculate = false
        if (parentGroup != lastGroup) {
            if (targetChildBlock !is SqlRawGroupBlock) {
                if (targetChildBlock !is SqlNewGroupBlock &&
                    dependDirectiveParent is SqlElConditionLoopCommentBlock
                ) {
                    dependDirective.parentBlock = null
                    recalculate = true
                } else {
                    lastParentChild?.let { last ->
                        // Judge by whether the parent's last child is at the same indent level as yourself?
                        if (last.indent.indentLevel > targetChildBlock.indent.indentLevel) {
                            dependDirective?.parentBlock = null
                            recalculate = true
                        }
                    }
                }
            }
        } else {
            val firstConditionLoopCommentBlock = blockBuilder.getFirstConditionOrLoopBlock()
            if (lastParentChild != null && firstConditionLoopCommentBlock != null &&
                firstConditionLoopCommentBlock.conditionType.isStartDirective()
            ) {
                recalculate = lastParentChild.node.startOffset < firstConditionLoopCommentBlock.node.startOffset &&
                    targetChildBlock is SqlNewGroupBlock
            }
            if (!recalculate && targetChildBlock is SqlWithCommonTableGroupBlock) {
                val tmpParentDirective = dependDirective?.nestParentBlock
                val tempParentDependBlock = tmpParentDirective?.getDependsOnBlock()
                if (tempParentDependBlock != null && firstConditionLoopCommentBlock != null &&
                    lastParentChild is SqlWithCommonTableGroupBlock
                ) {
                    dependDirective.setParentSelfNestBlock()
                    recalculate =
                        tempParentDependBlock.indent.indentLevel >= targetChildBlock.indent.indentLevel
                }
            }
        }
        // If the previous group is lower than your own group level, trace back the directive to yourself as the start of a new group,
        // and recalculate the indent from the top of the nest to match your parent block
        if (recalculate) {
            if (dependDirective?.conditionType?.isStartDirective() == true) {
                targetChildBlock.recalculateDirectiveIndent()
            }
        }

        // If a value is retrieved from the conditional/loop directive list,
        // set the current block as the dependency target for the directive.
        if (isNewGroup(targetChildBlock, context.blockBuilder) ||
            TypeUtil.isExpectedClassType(NEW_GROUP_EXPECTED_TYPES, targetChildBlock)
        ) {
            context.blockBuilder.addGroupTopNodeIndexHistory(targetChildBlock)
        }
    }

    private fun setParentLoopConditionDirective(
        targetChildBlock: SqlElConditionLoopCommentBlock,
        parentGroup: SqlBlock?,
        dependDirective: SqlElConditionLoopCommentBlock?,
    ) {
        // For else and end directives, make the start directive in the list the parent. The dependency is set later
        if (!targetChildBlock.conditionType.isStartDirective()) {
            targetChildBlock.setParentGroupBlock(dependDirective)
        } else {
            // If the subgroup block is parentGroup, make it the direct parent
            if (parentGroup is SqlSubGroupBlock && dependDirective?.parentBlock != parentGroup) {
                targetChildBlock.setParentGroupBlock(parentGroup)
                return
            }
            // If the child is also a condition/loop directive,
            // calculate the indent of the previous directive according to the previous group block
            if (dependDirective != null) {
                val parentDepend = dependDirective.getDependsOnBlock()
                if (parentDepend == null) {
                    dependDirective.setParentGroupBlock(parentGroup)
                    targetChildBlock.setParentGroupBlock(dependDirective, blockBuilder)
                } else {
                    // Also set the parent of itself with the previous directive and calculate the indent
                    if (parentGroup !is SqlNewGroupBlock || (
                            parentDepend !is SqlNewGroupBlock &&
                                parentDepend.node.startOffset >= parentGroup.node.startOffset
                        )
                    ) {
                        targetChildBlock.setParentGroupBlock(dependDirective, blockBuilder)
                    }
                }
            }
        }
    }

    /**
     * Determines whether it is a group that requires a line break or a specific combination of keywords that does not require a line break.
     */
    private fun isNewGroup(
        childBlock: SqlBlock,
        blockBuilder: SqlBlockBuilder,
    ): Boolean {
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()
        return isNewGroupAndNotSetLineKeywords(childBlock, lastGroup)
    }

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
            SqlKeywordUtil.isSetLineKeyword(
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
