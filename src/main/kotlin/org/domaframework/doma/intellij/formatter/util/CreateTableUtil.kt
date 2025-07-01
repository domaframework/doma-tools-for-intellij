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
package org.domaframework.doma.intellij.formatter.util

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlWhitespaceBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

object CreateTableUtil {
    fun getColumnDefinitionGroup(
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
    ): Spacing? {
        // TODO Customize indentation
        val offset = 5

        // Top Column Definition Group Block
        if (child1 is SqlWhitespaceBlock && child2 is SqlCreateTableColumnDefinitionRawGroupBlock) {
            val columnDefinitionGroupBlock =
                child2.parentBlock as? SqlCreateTableColumnDefinitionGroupBlock ?: return null

            if (child2.node.elementType == SqlTypes.COMMA) {
                // If the child2 is a comma, it is not a column definition raw group block.
                return Spacing.createSpacing(offset, offset, 0, false, 0, 0)
            }

            val maxColumnName = columnDefinitionGroupBlock.getMaxColumnNameLength()
            val diffColumnNameLen =
                maxColumnName.minus(child2.columnBlock?.getNodeText()?.length ?: 0)
            // If the longest column name is not in the top row, add two spaces for the "," to match the row with a comma.
            var indentLen = offset.plus(diffColumnNameLen)
            val maxColumnNameRaw =
                columnDefinitionGroupBlock.columnRawGroupBlocks
                    .findLast { raw -> raw.columnBlock?.getNodeText()?.length == maxColumnName }
            if (maxColumnNameRaw?.isFirstColumnRaw != true) {
                indentLen = indentLen.plus(2)
            }

            return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
        }

        if (child1 is SqlCreateTableColumnDefinitionRawGroupBlock && child2 is SqlColumnBlock) {
            val columnDefinitionGroupBlock =
                child1.parentBlock as? SqlCreateTableColumnDefinitionGroupBlock ?: return null

            val maxColumnName = columnDefinitionGroupBlock.getMaxColumnNameLength()
            val diffColumnNameLen = maxColumnName.minus(child2.getNodeText().length)
            var indentLen = diffColumnNameLen.plus(1)
            return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
        }
        return null
    }
}
