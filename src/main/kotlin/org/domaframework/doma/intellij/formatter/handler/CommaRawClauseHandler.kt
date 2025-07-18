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
package org.domaframework.doma.intellij.formatter.handler

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

object CommaRawClauseHandler {
    /**
     * Creates an appropriate comma block based on the last group context.
     * Handles special cases for condition/loop comment blocks and different group types.
     */
    fun getCommaBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock =
        when (lastGroup) {
            is SqlElConditionLoopCommentBlock ->
                createCommaBlockForConditionLoop(
                    lastGroup,
                    child,
                    sqlBlockFormattingCtx,
                )

            else -> createCommaBlockForGroup(lastGroup, child, sqlBlockFormattingCtx)
        }

    /**
     * Creates a comma block for condition/loop comment blocks.
     * Uses the parent block if available, otherwise uses the temporary parent block.
     */
    private fun createCommaBlockForConditionLoop(
        lastGroup: SqlElConditionLoopCommentBlock,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock {
        val effectiveParent = lastGroup.parentBlock ?: lastGroup.tempParentBlock
        return createCommaBlockForGroup(effectiveParent, child, sqlBlockFormattingCtx)
    }

    /**
     * Creates an appropriate comma block based on the group type and indent level.
     */
    private fun createCommaBlockForGroup(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock =
        when {
            shouldCreateColumnRawBlock(lastGroup) ->
                SqlColumnRawGroupBlock(
                    child,
                    sqlBlockFormattingCtx,
                )

            lastGroup is SqlWithCommonTableGroupBlock ->
                SqlWithCommonTableGroupBlock(
                    child,
                    sqlBlockFormattingCtx,
                )

            else -> SqlCommaBlock(child, sqlBlockFormattingCtx)
        }

    /**
     * Determines if a column raw block should be created based on the group type and indent level.
     */
    private fun shouldCreateColumnRawBlock(lastGroup: SqlBlock?): Boolean =
        (lastGroup is SqlColumnRawGroupBlock || lastGroup is SqlKeywordGroupBlock) &&
            lastGroup.indent.indentLevel != IndentType.SECOND
}
