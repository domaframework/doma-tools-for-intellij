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

import org.domaframework.doma.intellij.common.util.TypeUtil.isExpectedClassType
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock

open class SqlBlockBuilder {
    private val updateDirectiveParentTypes =
        listOf(
            SqlDefaultCommentBlock::class,
        )

    private val originalConditionLoopDirectiveParentType =
        listOf(
            SqlKeywordGroupBlock::class,
            SqlSubGroupBlock::class,
        )

    private val groupTopNodeIndexHistory = mutableListOf<SqlBlock>()

    private val commentBlocks = mutableListOf<SqlDefaultCommentBlock>()

    private val conditionOrLoopBlocks = mutableListOf<SqlElConditionLoopCommentBlock>()

    fun getGroupTopNodeIndexHistory(): List<SqlBlock> = groupTopNodeIndexHistory

    fun getLastGroupFilterDirective(): SqlBlock? = getGroupTopNodeIndexHistory().lastOrNull { it !is SqlElConditionLoopCommentBlock }

    fun addGroupTopNodeIndexHistory(block: SqlBlock) {
        groupTopNodeIndexHistory.add(block)
    }

    fun addCommentBlock(block: SqlDefaultCommentBlock) {
        commentBlocks.add(block)
    }

    /**
     * It becomes a child of the previous block,
     * but the indentation is aligned with the next block.
     */
    fun updateCommentBlockIndent(nextBlock: SqlBlock) {
        if (commentBlocks.isNotEmpty() &&
            nextBlock.parentBlock != null ||
            groupTopNodeIndexHistory.size <= 2
        ) {
            var index = 0
            commentBlocks
                .forEach { block ->
                    block.updateIndentLen(nextBlock, groupTopNodeIndexHistory.size)
                    index++
                }
            commentBlocks.clear()
        }
    }

    /**
     * For condition/loop directive blocks
     * determine the parent based on the type of block immediately below.
     *
     * When this process is invoked, a directive block has already been added to [groupTopNodeIndexHistory],
     * and the parent of the block passed as [nextBlock] has already been determined.
     */
    fun updateConditionLoopBlockIndent(nextBlock: SqlBlock) {
        if (!isExpectedClassType(updateDirectiveParentTypes, nextBlock)) {
            if (conditionOrLoopBlocks.isNotEmpty()) {
                val lastGroup = groupTopNodeIndexHistory.lastOrNull()
                conditionOrLoopBlocks
                    .filter { it.parentBlock == null }
                    .forEach { block ->
                        var setParentBlock: SqlBlock? = null
                        val conditionBlockIndex = groupTopNodeIndexHistory.indexOf(block)
                        val prevConditionBlockGroup =
                            if (conditionBlockIndex > 0) {
                                groupTopNodeIndexHistory[conditionBlockIndex - 1]
                            } else {
                                null
                            }

                        if (lastGroup == nextBlock) {
                            setParentBlock =
                                if (isExpectedClassType(
                                        originalConditionLoopDirectiveParentType,
                                        nextBlock,
                                    )
                                ) {
                                    nextBlock
                                } else {
                                    null
                                }
                        } else {
                            setParentBlock =
                                if (isExpectedClassType(
                                        originalConditionLoopDirectiveParentType,
                                        nextBlock,
                                    )
                                ) {
                                    nextBlock
                                } else {
                                    prevConditionBlockGroup
                                }
                        }

                        if (block != nextBlock) {
                            block.setParentGroupBlock(setParentBlock)
                        } else if (setParentBlock is SqlElConditionLoopCommentBlock) {
                            block.setParentGroupBlock(setParentBlock)
                        }
                    }
            }
        }
    }

    fun getLastGroupTopNodeIndexHistory(): SqlBlock? = groupTopNodeIndexHistory.lastOrNull()

    fun removeLastGroupTopNodeIndexHistory() {
        if (groupTopNodeIndexHistory.isNotEmpty()) {
            groupTopNodeIndexHistory.removeLast()
        }
    }

    fun clearSubListGroupTopNodeIndexHistory(start: Int) {
        groupTopNodeIndexHistory
            .subList(
                start,
                groupTopNodeIndexHistory.size,
            ).clear()
    }

    fun getGroupTopNodeIndex(condition: (SqlBlock) -> Boolean): Int =
        groupTopNodeIndexHistory.indexOfLast {
            condition(it)
        }

    fun addConditionOrLoopBlock(block: SqlElConditionLoopCommentBlock) {
        if (block.conditionType.isStartDirective() || block.conditionType.isElse()) {
            conditionOrLoopBlocks.add(block)
        }
    }

    fun removeConditionOrLoopBlockLast() {
        if (conditionOrLoopBlocks.isNotEmpty()) {
            conditionOrLoopBlocks.removeLast()
        }
    }
}
