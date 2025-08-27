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

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlWhitespaceBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.other.SqlEscapeBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

object CreateClauseHandler {
    private const val COLUMN_DEFINITION_OFFSET = 5
    private const val COMMA_OFFSET = 2
    private const val SINGLE_SPACE = 1

    fun getCreateTableClauseSubGroup(
        lastGroup: SqlBlock,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlCreateTableColumnDefinitionGroupBlock? {
        if (lastGroup is SqlCreateKeywordGroupBlock) {
            return SqlCreateTableColumnDefinitionGroupBlock(child, sqlBlockFormattingCtx)
        }
        return null
    }

    fun getColumnRawGroup(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlCreateTableColumnDefinitionRawGroupBlock? =
        if (lastGroup is SqlCreateTableColumnDefinitionGroupBlock ||
            lastGroup is SqlCreateTableColumnDefinitionRawGroupBlock
        ) {
            SqlCreateTableColumnDefinitionRawGroupBlock(
                child,
                sqlBlockFormattingCtx,
            )
        } else {
            null
        }

    /**
     * Right-justify based on the longest column name in the column definition line
     */
    fun getColumnDefinitionRawGroupSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? =
        when {
            child1 is SqlWhitespaceBlock && child2 is SqlCreateTableColumnDefinitionRawGroupBlock -> {
                calculateColumnDefinitionSpacing(child2)
            }
            child1 is SqlCreateTableColumnDefinitionRawGroupBlock &&
                (child2 is SqlColumnBlock || child2 is SqlEscapeBlock) -> {
                calculateColumnSpacing(child1, child2)
            }
            else -> null
        }

    private fun calculateColumnDefinitionSpacing(columnDefBlock: SqlCreateTableColumnDefinitionRawGroupBlock): Spacing? {
        val columnDefinitionGroupBlock =
            columnDefBlock.parentBlock as? SqlCreateTableColumnDefinitionGroupBlock ?: return null

        // If the child is a comma, it is not a column definition raw group block.
        if (columnDefBlock.node.elementType == SqlTypes.COMMA) {
            return createFixedSpacing(COLUMN_DEFINITION_OFFSET)
        }

        val maxColumnNameLength = columnDefinitionGroupBlock.getMaxColumnNameLength()
        val currentColumnLength = columnDefBlock.getColumnNameLength()
        val columnDifference = maxColumnNameLength - currentColumnLength

        var indentLen = COLUMN_DEFINITION_OFFSET + columnDifference

        // If the longest column name is not in the top row, add comma offset
        if (!isMaxColumnInFirstRow(columnDefinitionGroupBlock, maxColumnNameLength)) {
            indentLen += COMMA_OFFSET
        }

        return createFixedSpacing(indentLen)
    }

    private fun calculateColumnSpacing(
        rawGroupBlock: SqlCreateTableColumnDefinitionRawGroupBlock,
        columnBlock: Block,
    ): Spacing? {
        val columnDefinitionGroupBlock =
            rawGroupBlock.parentBlock as? SqlCreateTableColumnDefinitionGroupBlock ?: return null

        val maxColumnNameLength = columnDefinitionGroupBlock.getMaxColumnNameLength()
        val columnLength =
            when (columnBlock) {
                is SqlColumnBlock -> columnBlock.getNodeText().length
                else -> rawGroupBlock.getColumnNameLength()
            }

        val columnDifference = maxColumnNameLength - columnLength
        val indentLen = columnDifference + SINGLE_SPACE

        return createFixedSpacing(indentLen)
    }

    private fun isMaxColumnInFirstRow(
        columnDefinitionGroupBlock: SqlCreateTableColumnDefinitionGroupBlock,
        maxColumnNameLength: Int,
    ): Boolean {
        val maxColumnNameRaw =
            columnDefinitionGroupBlock.columnRawGroupBlocks
                .findLast { raw -> raw.columnBlock?.getNodeText()?.length == maxColumnNameLength }
        return maxColumnNameRaw?.isFirstColumnRaw == true
    }

    private fun createFixedSpacing(spaces: Int): Spacing = Spacing.createSpacing(spaces, spaces, 0, false, 0, 0)
}
