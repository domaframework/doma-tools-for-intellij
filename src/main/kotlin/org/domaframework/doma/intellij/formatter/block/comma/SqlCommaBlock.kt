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
package org.domaframework.doma.intellij.formatter.block.comma

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlFromGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlSecondKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlValuesParamGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlCommaBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlBlock(
        node,
        context.wrap,
        context.alignment,
        context.spacingBuilder,
        context.enableFormat,
        context.formatMode,
    ) {
    companion object {
        private val EXPECTED_TYPES =
            listOf(
                SqlInsertColumnGroupBlock::class,
                SqlInsertValueGroupBlock::class,
                SqlUpdateSetGroupBlock::class,
                SqlUpdateColumnGroupBlock::class,
                SqlUpdateValueGroupBlock::class,
                SqlFunctionParamBlock::class,
                SqlWithColumnGroupBlock::class,
                SqlKeywordGroupBlock::class,
                SqlElConditionLoopCommentBlock::class,
            )

        private val PARENT_INDENT_SYNC_TYPES =
            listOf(
                SqlUpdateColumnGroupBlock::class,
                SqlInsertColumnGroupBlock::class,
                SqlWithColumnGroupBlock::class,
                SqlFunctionParamBlock::class,
            )
    }

    override val indent =
        ElementIndent(
            IndentType.COMMA,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.COMMA
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlFromGroupBlock) {
            if (lastGroup.tableBlocks.isNotEmpty()) lastGroup.tableBlocks.add(this)
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlSubGroupBlock) {
                if (parent is SqlParallelListBlock) {
                    return 0
                }

                val parentIndentLen = parent.indent.groupIndentLen
                if (TypeUtil.isExpectedClassType(PARENT_INDENT_SYNC_TYPES, parent)) {
                    return parentIndentLen
                }

                // TODO Indent each comma in a value group so that it aligns with the position of the first value row.
                val parentIndentSingleSpaceTypes =
                    listOf(
                        SqlInsertValueGroupBlock::class,
                        SqlUpdateValueGroupBlock::class,
                    )
                if (TypeUtil.isExpectedClassType(parentIndentSingleSpaceTypes, parent)) {
                    return parentIndentLen.plus(1)
                }

                val notNewLineTypes =
                    listOf(
                        SqlValuesParamGroupBlock::class,
                        SqlConditionalExpressionGroupBlock::class,
                    )
                if (TypeUtil.isExpectedClassType(notNewLineTypes, parent)) return 0

                val grand = parent.parentBlock
                grand?.let { grand ->
                    val grandIndent = grand.indent.indentLen
                    val groupIndent = parentBlock?.indent?.groupIndentLen ?: 0

                    if (grand is SqlColumnRawGroupBlock) {
                        return groupIndent.plus(grandIndent)
                    }

                    return groupIndent.plus(grandIndent).minus(1)
                }
                return parentIndentLen.plus(1)
            } else {
                return when (parent) {
                    is SqlValuesGroupBlock -> parent.indent.indentLen
                    is SqlElConditionLoopCommentBlock -> {
                        val firstChild = parent.childBlocks.findLast { it is SqlFunctionParamBlock && it.endPatternBlock == null }
                        val parentIndent = firstChild?.indent ?: parent.indent
                        parentIndent.groupIndentLen.plus(1)
                    }
                    else -> {
                        // No indent after ORDER BY within function parameters
                        if (parent is SqlSecondKeywordBlock && parent.parentBlock is SqlFunctionParamBlock) {
                            0
                        } else {
                            parent.indent.groupIndentLen.plus(1)
                        }
                    }
                }
            }
        }
        return 1
    }

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(1)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        parentBlock?.let { parent ->
            if (parent is SqlConditionalExpressionGroupBlock) return false
            // Don't allow line breaks after ORDER BY within function parameters
            if (parent.parentBlock is SqlFunctionParamBlock) {
                return false
            }
            return TypeUtil.isExpectedClassType(EXPECTED_TYPES, parent)
        }
        return false
    }
}
