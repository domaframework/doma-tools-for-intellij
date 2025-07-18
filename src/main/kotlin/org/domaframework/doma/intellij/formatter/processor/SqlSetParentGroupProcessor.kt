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
package org.domaframework.doma.intellij.formatter.processor

import org.domaframework.doma.intellij.common.util.TypeUtil.isExpectedClassType
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlLateralGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlReturningGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTopQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlFunctionGroupBlock
import org.domaframework.doma.intellij.formatter.builder.SqlBlockBuilder
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil
import org.domaframework.doma.intellij.formatter.util.UpdateClauseUtil
import org.domaframework.doma.intellij.psi.SqlTypes
import kotlin.collections.lastOrNull

class SqlSetParentGroupProcessor(
    private val blockBuilder: SqlBlockBuilder,
) {
    data class SetParentContext(
        val childBlock: SqlBlock,
        val blockBuilder: SqlBlockBuilder,
    )

    private val newGroupExpectedTypes =
        listOf(
            SqlSubGroupBlock::class,
            SqlCreateViewGroupBlock::class,
            SqlInlineGroupBlock::class,
            SqlInlineSecondGroupBlock::class,
            SqlColumnDefinitionRawGroupBlock::class,
            SqlLateralGroupBlock::class,
        )

    /**
     * Sets the parent of the latest group block and registers itself as a child element.
     */
    fun updateGroupBlockParentAndAddGroup(childBlock: SqlBlock) {
        setParentGroups(
            SetParentContext(
                childBlock,
                blockBuilder,
            ),
        ) { history ->
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
    fun updateGroupBlockLastGroupParentAddGroup(
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
        val context =
            SetParentContext(
                childBlock,
                blockBuilder,
            )

        if (lastGroupBlock is SqlElConditionLoopCommentBlock) {
            if (lastGroupBlock.parentBlock != null) {
                setParentGroups(context) { history ->
                    return@setParentGroups lastGroupBlock
                }
            } else {
                val history = blockBuilder.getGroupTopNodeIndexHistory()
                val findParent =
                    history
                        .lastOrNull {
                            it.indent.indentLevel < childBlock.indent.indentLevel ||
                                (it is SqlElConditionLoopCommentBlock && it.parentBlock != null)
                        }
                // Search for keyword groups with a level lower than or equal to the current level,
                // or conditional directives that already have a parent assigned.
                if (findParent is SqlElConditionLoopCommentBlock) {
                    // Set the parent of the most recent conditional directive to the current node.
                    lastGroupBlock.setParentGroupBlock(findParent)
                    setParentGroups(context) { history ->
                        return@setParentGroups lastGroupBlock
                    }
                }
                if (findParent !is SqlElConditionLoopCommentBlock) {
                    // If a keyword group with a level lower than or equal to the current level is found,
                    // set that keyword group as the parent, and set the parent of the most recent conditional directive to the current node.
                    setParentGroups(context) { history ->
                        return@setParentGroups findParent
                    }
                    lastGroupBlock.setParentGroupBlock(childBlock)
                }
            }
            return
        }

        val currentIndentLevel = childBlock.indent.indentLevel
        if (currentIndentLevel == IndentType.TOP) {
            var parentBlock: SqlBlock? = null
            val expectedTypes =
                listOf(
                    SqlSubGroupBlock::class,
                    SqlCreateViewGroupBlock::class,
                )
            if (isExpectedClassType(expectedTypes, lastGroupBlock)) {
                parentBlock = lastGroupBlock
            } else {
                when (childBlock) {
                    is SqlUpdateQueryGroupBlock -> {
                        UpdateClauseUtil
                            .getParentGroupBlock(blockBuilder, childBlock)
                            ?.let { parentBlock = it }
                    }

                    else -> {
                        val topKeywordIndex =
                            blockBuilder.getGroupTopNodeIndex { block ->
                                block.indent.indentLevel == IndentType.TOP
                            }
                        val subGroupIndex =
                            blockBuilder.getGroupTopNodeIndex { block ->
                                block is SqlSubGroupBlock
                            }

                        var deleteIndex = topKeywordIndex
                        if (topKeywordIndex >= 0 && subGroupIndex < 0) {
                            val copyParentBlock =
                                blockBuilder.getGroupTopNodeIndexHistory()[topKeywordIndex]
                            parentBlock = copyParentBlock.parentBlock
                        } else if (topKeywordIndex > subGroupIndex) {
                            val copyParentBlock =
                                blockBuilder.getGroupTopNodeIndexHistory()[subGroupIndex]
                            parentBlock = copyParentBlock
                        }
                        if (deleteIndex >= 0) {
                            blockBuilder.clearSubListGroupTopNodeIndexHistory(deleteIndex)
                        }
                    }
                }
            }
            setParentGroups(context) { history ->
                return@setParentGroups parentBlock
            }
            return
        }

        if (lastGroupBlock.indent.indentLevel == IndentType.SUB) {
            setParentGroups(context) { history ->
                return@setParentGroups lastGroupBlock
            }
        } else if (lastIndentLevel == currentIndentLevel) {
            val prevKeyword = lastGroupBlock.childBlocks.findLast { it is SqlKeywordBlock }
            prevKeyword?.let { prev ->
                if (SqlKeywordUtil.isSetLineKeyword(childBlock.getNodeText(), prev.getNodeText())) {
                    updateGroupBlockLastGroupParentAddGroup(
                        lastGroupBlock,
                        childBlock,
                    )
                    return
                }
            }

            blockBuilder.removeLastGroupTopNodeIndexHistory()
            if (childBlock is SqlReturningGroupBlock) {
                // Since `DO UPDATE` does not include a `RETURNING` clause, it should be registered as a child of the parent `INSERT` query.
                // The `DO` keyword should align with the `INSERT` query, and therefore it will serve as the **indentation anchor** for the following update block.
                setParentGroups(context) { history ->
                    val lastGroup = history.findLast { it is SqlTopQueryGroupBlock }
                    return@setParentGroups if (lastGroup is SqlUpdateQueryGroupBlock && lastGroup.parentBlock is SqlDoGroupBlock) {
                        lastGroup.parentBlock
                    } else {
                        lastGroup
                    }
                }
                return
            }
            updateGroupBlockLastGroupParentAddGroup(
                lastGroupBlock,
                childBlock,
            )
        } else if (lastIndentLevel < currentIndentLevel) {
            updateGroupBlockParentAndAddGroup(
                childBlock,
            )
        } else if (lastIndentLevel == IndentType.JOIN &&
            SqlKeywordUtil.isSecondOptionKeyword(childBlock.getNodeText())
        ) {
            // left,right < inner,outer < join
            updateGroupBlockParentAndAddGroup(
                childBlock,
            )
            return
        } else {
            setParentGroups(context) { history ->
                return@setParentGroups history
                    .lastOrNull {
                        it.indent.indentLevel < childBlock.indent.indentLevel
                    }
            }
        }
    }

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
        val expectedTypes =
            listOf(
                SqlColumnRawGroupBlock::class,
            )
        if (isExpectedClassType(expectedTypes, lastGroupBlock)) {
            blockBuilder.removeLastGroupTopNodeIndexHistory()
        }
        setParentGroups(
            SetParentContext(
                childBlock,
                blockBuilder,
            ),
        ) { history ->
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

            if (childBlock.parentBlock is SqlSubGroupBlock) {
                (childBlock.parentBlock as SqlSubGroupBlock).endGroup()
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

        // The parent block for SqlElConditionLoopCommentBlock will be set later
        if (targetChildBlock is SqlElConditionLoopCommentBlock && targetChildBlock.conditionType.isStartDirective()) {
            targetChildBlock.tempParentBlock = parentGroup
            if (parentGroup is SqlElConditionLoopCommentBlock && parentGroup.parentBlock != null) {
                targetChildBlock.setParentGroupBlock(parentGroup)
            }
        } else {
            targetChildBlock.setParentGroupBlock(parentGroup)
        }

        if (isNewGroup(targetChildBlock, context.blockBuilder) ||
            isExpectedClassType(
                newGroupExpectedTypes,
                targetChildBlock,
            )
        ) {
            context.blockBuilder.addGroupTopNodeIndexHistory(targetChildBlock)
            context.blockBuilder.updateCommentBlockIndent(targetChildBlock)
        }

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
        if (childBlock is SqlElConditionLoopCommentBlock) {
            if (childBlock.conditionType.isStartDirective() || childBlock.conditionType.isElse()) {
                return true
            }
        }

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
    fun getLastGroupKeywordText(lastGroup: SqlBlock?): String =
        lastGroup
            ?.childBlocks
            ?.lastOrNull { it.node.elementType == SqlTypes.KEYWORD }
            ?.getNodeText() ?: lastGroup?.getNodeText() ?: ""
}
