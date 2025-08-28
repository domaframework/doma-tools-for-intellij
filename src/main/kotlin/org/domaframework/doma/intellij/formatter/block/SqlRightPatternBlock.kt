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
package org.domaframework.doma.intellij.formatter.block

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.common.util.TypeUtil.isExpectedClassType
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictExpressionSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTopQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlValuesParamGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * Parent is always a subclass of a subgroup
 */
open class SqlRightPatternBlock(
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
    enum class LineBreakAndSpacingType {
        NONE, // No line break and no spacing
        LINE_BREAK, // Line break only
        SPACING, // Spacing only
        LINE_BREAK_AND_SPACING, // Both line break and spacing
    }

    private var preSpaceRight = false

    fun isPreSpaceRight() = preSpaceRight

    var lineBreakAndSpacingType: LineBreakAndSpacingType = LineBreakAndSpacingType.NONE

    companion object {
        private val INDENT_EXPECTED_TYPES =
            listOf(
                SqlUpdateColumnGroupBlock::class,
                SqlUpdateValueGroupBlock::class,
                SqlCreateTableColumnDefinitionGroupBlock::class,
                SqlInsertValueGroupBlock::class,
            )

        val NOT_INDENT_EXPECTED_TYPES =
            listOf(
                SqlFunctionParamBlock::class,
                SqlInsertColumnGroupBlock::class,
                SqlWithQuerySubGroupBlock::class,
                SqlConflictExpressionSubGroupBlock::class,
            )

        val NEW_LINE_EXPECTED_TYPES =
            listOf(
                SqlUpdateColumnGroupBlock::class,
                SqlCreateTableColumnDefinitionGroupBlock::class,
                SqlColumnDefinitionRawGroupBlock::class,
                SqlUpdateSetGroupBlock::class,
                SqlWithQuerySubGroupBlock::class,
            )

        val NOT_NEW_LINE_EXPECTED_TYPES =
            listOf(
                SqlDataTypeParamBlock::class,
                SqlConditionalExpressionGroupBlock::class,
                SqlConflictExpressionSubGroupBlock::class,
                SqlFunctionParamBlock::class,
            )
    }

    /**
     * Configures whether to add a space to the right side when the group ends.
     */
    private fun enableLastRight() {
        parentBlock?.let { parent ->
            val isFirstChildQuery =
                parent.childBlocks.firstOrNull {
                    it !is SqlCommentBlock
                } is SqlTopQueryGroupBlock
            // Check if parent is in the notInsertSpaceClassList
            if (isExpectedClassType(NOT_INDENT_EXPECTED_TYPES, parent)) {
                preSpaceRight = false
                return
            }

            if (parent is SqlConditionalExpressionGroupBlock) {
                preSpaceRight = isFirstChildQuery
                return
            }

            if (isExpectedClassType(
                    INDENT_EXPECTED_TYPES,
                    parent,
                ) ||
                parent.childBlocks.any { it is SqlValuesGroupBlock }
            ) {
                preSpaceRight = true
                return
            }

            // Check if parent is SqlSubQueryGroupBlock
            if (parent is SqlSubQueryGroupBlock) {
                val prevKeywordBlock =
                    parent.childBlocks
                        .filter { it.node.startOffset < node.startOffset }
                        .find { it is SqlKeywordGroupBlock }

                preSpaceRight = prevKeywordBlock?.indent?.indentLevel == IndentType.TOP
                return
            }

            // Check grandparent for SqlKeywordGroupBlock
            parent.parentBlock?.let { grandParent ->
                preSpaceRight = grandParent.childBlocks.any { it is SqlKeywordGroupBlock }
                return
            }
        }

        // Default case
        preSpaceRight = parentBlock is SqlValuesParamGroupBlock
    }

    override val indent =
        ElementIndent(
            IndentType.NONE,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        enableLastRight()
        indent.indentLevel = IndentType.NONE
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlSubGroupBlock)?.endPatternBlock = this
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if ((
                    isExpectedClassType(NEW_LINE_EXPECTED_TYPES, parent) ||
                        parent.getChildBlocksDropLast().firstOrNull() is SqlValuesGroupBlock
                ) &&
                preSpaceRight
            ) {
                return parent.indent.indentLen
            }
            if (preSpaceRight) return 1
            return parent.indent.indentLen
        } ?: return 0
    }

    override fun isLeaf(): Boolean = true

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        parentBlock?.let { parent ->
            if (isExpectedClassType(NEW_LINE_EXPECTED_TYPES, parent) ||
                parent.childBlocks.any { it is SqlValuesGroupBlock }
            ) {
                lineBreakAndSpacingType =
                    if (preSpaceRight) {
                        LineBreakAndSpacingType.LINE_BREAK_AND_SPACING
                    } else {
                        LineBreakAndSpacingType.LINE_BREAK
                    }
                return true
            }

            if (parent is SqlSubGroupBlock) {
                val firstChild =
                    parent.getChildBlocksDropLast().firstOrNull()
                if (firstChild is SqlKeywordGroupBlock) {
                    //  For subgroups other than function parameters, if the first element is a keyword group, add a line break before the closing parenthesis except at the top level.
                    //  For subgroups created by WITHIN GROUP (), do not add a line break.
                    val lineBreak =
                        firstChild.indent.indentLevel != IndentType.TOP &&
                            !isExpectedClassType(NOT_NEW_LINE_EXPECTED_TYPES, parent)
                    lineBreakAndSpacingType =
                        if (lineBreak) {
                            if (preSpaceRight) {
                                LineBreakAndSpacingType.LINE_BREAK_AND_SPACING
                            } else {
                                LineBreakAndSpacingType.LINE_BREAK
                            }
                        } else {
                            if (preSpaceRight) {
                                LineBreakAndSpacingType.SPACING
                            } else {
                                LineBreakAndSpacingType.NONE
                            }
                        }

                    return lineBreak
                }
            }
        }
        lineBreakAndSpacingType =
            if (preSpaceRight) {
                LineBreakAndSpacingType.SPACING
            } else {
                LineBreakAndSpacingType.NONE
            }
        return false
    }
}
