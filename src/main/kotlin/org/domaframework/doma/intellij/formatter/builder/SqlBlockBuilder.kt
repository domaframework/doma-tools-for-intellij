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

import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock

open class SqlBlockBuilder {

    private val groupTopNodeIndexHistory = mutableListOf<SqlBlock>()

    private val commentBlocks = mutableListOf<SqlDefaultCommentBlock>()

    // The list that manages conditional loop directive blocks.
    private val conditionOrLoopBlocks = mutableListOf<SqlElConditionLoopCommentBlock>()

    fun getLastConditionOrLoopBlock() = conditionOrLoopBlocks.lastOrNull()

    fun getFirstConditionOrLoopBlock() = conditionOrLoopBlocks.firstOrNull()

    /**
     *  Get directives that don't have dependencies yet
     */
    fun getLastNotDependOnConditionOrLoopBlock() = conditionOrLoopBlocks.findLast { it.getDependsOnBlock() == null }

    fun getLastOpenDependOnConditionOrLoopBlock() = conditionOrLoopBlocks.findLast { it.conditionEnd == null }

    fun removeGroupForClosedDirective() {
        if (groupTopNodeIndexHistory.isEmpty()) return
        val lastCloseDirective = conditionOrLoopBlocks.lastOrNull()
        val dependBlock = lastCloseDirective?.getDependsOnBlock()
        if (dependBlock != null) {
            val removeCount =
                groupTopNodeIndexHistory.takeLastWhile { it.node.startOffset >= dependBlock.node.startOffset }
            val startIndex = groupTopNodeIndexHistory.size - removeCount.size
            if (startIndex >= 0) {
                clearSubListGroupTopNodeIndexHistory(startIndex)
            }
        }
        // Also remove condition/loop directives associated with 'end' or `else` from the list
        removeConditionOrLoopBlockLast()
    }

    fun getNotClosedConditionOrLoopBlock() = conditionOrLoopBlocks.filter { it.conditionEnd == null }

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

    @Suppress("ktlint:standard:no-consecutive-comments")
    fun getLastGroupTopNodeIndexHistory(): SqlBlock? = groupTopNodeIndexHistory.lastOrNull()

    fun removeLastGroupTopNodeIndexHistory() {
        if (groupTopNodeIndexHistory.isNotEmpty()) {
            val openDirective = getLastOpenDependOnConditionOrLoopBlock()
            openDirective?.let { directive ->
                val removeBlock = groupTopNodeIndexHistory.last()
                if (removeBlock.node.startOffset > directive.node.startOffset) {
                    groupTopNodeIndexHistory.removeLast()
                }
                return
            }
            groupTopNodeIndexHistory.removeLast()
        }
    }

    /**
     * When deleting group list, prevent deletion from outside condition/loop directives to inside condition/loop directives
     */
    fun clearSubListGroupTopNodeIndexHistory(startIndex: Int) {
        val openDirective = getLastOpenDependOnConditionOrLoopBlock()
        openDirective?.let { directive ->
            val removeList =
                groupTopNodeIndexHistory
                    .subList(
                        startIndex,
                        groupTopNodeIndexHistory.size,
                    )
            // Also exclude blocks before the first condition/loop directive in conditionOrLoopBlocks
            val firstConditionLoopCommentBlock = conditionOrLoopBlocks.firstOrNull()
            val inlineDirectiveList =
                removeList.filter {
                    it.node.startOffset > directive.node.startOffset ||
                        it.node.startOffset < (firstConditionLoopCommentBlock?.node?.startOffset ?: 0)
                }
            inlineDirectiveList.forEach { groupTopNodeIndexHistory.remove(it) }
            return
        }
        clearGroupList(startIndex)
    }

    private fun clearGroupList(start: Int) {
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
