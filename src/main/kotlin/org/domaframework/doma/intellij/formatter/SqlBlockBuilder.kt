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
package org.domaframework.doma.intellij.formatter

import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock

open class SqlBlockBuilder {
    private val groupTopNodeIndexHistory = mutableListOf<Pair<Int, SqlBlock>>()

    private val commentBlocks = mutableListOf<SqlCommentBlock>()

    private val conditionOrLoopBlocks = mutableListOf<SqlElConditionLoopCommentBlock>()

    fun getGroupTopNodeIndexHistory(): List<Pair<Int, SqlBlock>> = groupTopNodeIndexHistory

    fun getLastGroup(): SqlBlock? = groupTopNodeIndexHistory.lastOrNull()?.second

    fun addGroupTopNodeIndexHistory(block: Pair<Int, SqlBlock>) {
        groupTopNodeIndexHistory.add(block)
    }

    fun addCommentBlock(block: SqlCommentBlock) {
        commentBlocks.add(block)
    }

    fun updateCommentBlockIndent(baseIndent: SqlBlock) {
        if (commentBlocks.isNotEmpty()) {
            var index = 0
            commentBlocks.forEach { block ->
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
        if (conditionOrLoopBlocks.isNotEmpty()) {
            conditionOrLoopBlocks.forEach { block ->
                println("Update ParentGroup:${block.getNodeText()}")
                if (block.parentBlock == null) {
                    block.setParentGroupBlock(baseIndent)
                }
            }
        }
    }

    fun getLastGroupTopNodeIndexHistory(): Pair<Int, SqlBlock>? = groupTopNodeIndexHistory.lastOrNull()

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
            condition(it.second)
        }

    fun getConditionOrLoopBlocksLast(): SqlElConditionLoopCommentBlock? = conditionOrLoopBlocks.lastOrNull()

    fun addConditionOrLoopBlock(block: SqlElConditionLoopCommentBlock) {
        if (!block.conditionType.isInvalid() && !block.conditionType.isEnd()
        ) {
            conditionOrLoopBlocks.add(block)
        }
    }

    fun removeConditionOrLoopBlockLast() {
        if (conditionOrLoopBlocks.isNotEmpty()) {
            conditionOrLoopBlocks.removeLast()
        }
    }
}
