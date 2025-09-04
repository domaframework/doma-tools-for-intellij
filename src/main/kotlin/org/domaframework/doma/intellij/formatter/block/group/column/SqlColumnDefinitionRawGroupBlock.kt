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
package org.domaframework.doma.intellij.formatter.block.group.column

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.other.SqlEscapeBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Column definition group block in the column list group attached to Create Table
 * The parent must be SqlCreateTableColumnDefinitionGroupBlock
 */
open class SqlColumnDefinitionRawGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlRawGroupBlock(
        node,
        context,
    ) {
    companion object {
        private const val DEFAULT_OFFSET = 0
        private const val ESCAPE_CHARS_LENGTH = 2
        private const val FIRST_COLUMN_INDENT = 1
    }

    val isFirstColumnRaw = node.elementType != SqlTypes.COMMA
    open var columnBlock: SqlBlock? = if (isFirstColumnRaw) this else null

    fun getColumnNameLength(): Int {
        val columnNameLength = columnBlock?.getNodeText()?.length ?: 0
        val hasEscapeCharacters = columnBlock?.prevBlocks?.firstOrNull() is SqlEscapeBlock
        return if (hasEscapeCharacters) {
            columnNameLength + ESCAPE_CHARS_LENGTH
        } else {
            columnNameLength
        }
    }

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.groupIndentLen = indent.indentLen
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    /**
     * Calculate indent length for column definition.
     * First column has an indent of 1, others use default offset.
     */
    private fun calculateIndentLength(): Int = if (isFirstColumnRaw) FIRST_COLUMN_INDENT else DEFAULT_OFFSET

    override fun createBlockIndentLen(): Int = calculateIndentLength()

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true
}
