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
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlTableModifySecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlSelectQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * Group blocks when generating columns with subqueries.
 * Represents a group consisting of a single column row.
 * For Example:
 * SELECT id     -- "id" is [SqlColumnRawGroupBlock](isFirstColumnGroup = true)
 *        , name -- "," is [SqlColumnRawGroupBlock]
 */
class SqlColumnRawGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlRawGroupBlock(
        node,
        context,
    ) {
    private val offset = 1

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.COLUMN
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen =
            if (isFirstColumnGroup) indent.indentLen else indent.indentLen.plus(1)
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        when (lastGroup) {
            is SqlSelectQueryGroupBlock -> lastGroup.selectionColumns.add(this)
            is SqlUpdateColumnGroupBlock -> lastGroup.columnRawGroupBlocks.add(this)
        }
        (lastGroup as? SqlSelectQueryGroupBlock)?.selectionColumns?.add(this)
    }

    override fun createBlockIndentLen(): Int =
        when (parentBlock) {
            is SqlWithQueryGroupBlock -> {
                parentBlock
                    ?.childBlocks
                    ?.dropLast(1)
                    ?.lastOrNull()
                    ?.indent
                    ?.indentLen ?: offset
            }

            is SqlTableModifySecondGroupBlock -> {
                parentBlock?.let { parent ->
                    val grand = parent.parentBlock
                    if (grand is SqlElConditionLoopCommentBlock ||
                        parent.childBlocks.firstOrNull() is SqlElConditionLoopCommentBlock
                    ) {
                        parent.indent.indentLen.plus(2)
                    } else {
                        parent.indent.indentLen
                    }
                } ?: offset
            }

            else ->
                parentBlock?.indent?.groupIndentLen?.plus(1) ?: offset
        }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = !isFirstColumnGroup
}
