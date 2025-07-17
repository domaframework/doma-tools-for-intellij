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
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType

open class SqlBlockBuilder {
    private val updateDirectiveParentTypes =
        listOf(
            SqlLineCommentBlock::class,
            SqlBlockCommentBlock::class,
        )

    private val originalConditionLoopDirectiveParentType =
        listOf(
            SqlKeywordGroupBlock::class,
            SqlSubGroupBlock::class,
        )

    private val groupTopNodeIndexHistory = mutableListOf<SqlBlock>()

    private val commentBlocks = mutableListOf<SqlCommentBlock>()

    private val conditionOrLoopBlocks = mutableListOf<SqlElConditionLoopCommentBlock>()

    fun getGroupTopNodeIndexHistory(): List<SqlBlock> = groupTopNodeIndexHistory

    fun addGroupTopNodeIndexHistory(block: SqlBlock) {
        groupTopNodeIndexHistory.add(block)
    }

    fun addCommentBlock(block: SqlCommentBlock) {
        commentBlocks.add(block)
    }

    fun updateCommentBlockIndent(baseIndent: SqlBlock) {
        if (commentBlocks.isNotEmpty()) {
            var index = 0
            commentBlocks
                .filter { it.parentBlock == null }
                .forEach { block ->
                    if (block !is SqlElBlockCommentBlock) {
                        if (index == 0 &&
                            baseIndent.parentBlock is SqlSubGroupBlock &&
                            baseIndent.parentBlock?.childBlocks?.size == 1
                        ) {
                            block.indent.indentLevel = IndentType.NONE
                            block.indent.indentLen = 1
                            block.indent.groupIndentLen = 0
                        } else {
                            block.setParentGroupBlock(baseIndent)
                        }
                        index++
                    }
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
                        // Prioritize previous condition loop block over keyword group
//                        if (prevConditionBlockGroup is SqlElConditionLoopCommentBlock) {
//                            setParentBlock = prevConditionBlockGroup
//                        } else if (prevConditionBlockGroup?.parentBlock is SqlElConditionLoopCommentBlock) {
//                            setParentBlock = prevConditionBlockGroup.parentBlock
//                        } else
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
