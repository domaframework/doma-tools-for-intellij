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
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictExpressionSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
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
        null,
        context.spacingBuilder,
        context.enableFormat,
        context.formatMode,
    ) {
    var preSpaceRight = false

    /**
     * Configures whether to add a space to the right side when the group ends.
     */
    private fun enableLastRight() {
        parentBlock?.let { parent ->
            // Check if parent is in the notInsertSpaceClassList
            val notInsertSpaceClassList =
                listOf(
                    SqlFunctionParamBlock::class,
                    SqlInsertColumnGroupBlock::class,
                    SqlWithQuerySubGroupBlock::class,
                    SqlConflictExpressionSubGroupBlock::class,
                )
            if (isExpectedClassType(notInsertSpaceClassList, parent)) {
                preSpaceRight = false
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
        preSpaceRight = false
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
        if (preSpaceRight) return 1

        parentBlock?.let { parent ->
            if (parent is SqlWithQuerySubGroupBlock) return 0
            val exceptionalTypes =
                listOf(
                    SqlUpdateColumnGroupBlock::class,
                    SqlUpdateValueGroupBlock::class,
                    SqlCreateTableColumnDefinitionGroupBlock::class,
                )
            if (isExpectedClassType(exceptionalTypes, parent)) return parent.indent.indentLen
            return parent.indent.indentLen
        } ?: return 0
    }

    override fun isLeaf(): Boolean = true

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        if (preSpaceRight) return false

        parentBlock?.let { parent ->
            val exceptionalTypes =
                listOf(
                    SqlCreateTableColumnDefinitionGroupBlock::class,
                    SqlColumnDefinitionRawGroupBlock::class,
                    SqlUpdateSetGroupBlock::class,
                    SqlUpdateColumnGroupBlock::class,
                    SqlUpdateValueGroupBlock::class,
                    SqlWithQuerySubGroupBlock::class,
                )

            val excludeTypes =
                listOf(
                    SqlDataTypeParamBlock::class,
                    SqlConditionalExpressionGroupBlock::class,
                    SqlConflictExpressionSubGroupBlock::class,
                    SqlFunctionParamBlock::class,
                )

            if ((
                    isExpectedClassType(exceptionalTypes, parent) ||
                        isExpectedClassType(exceptionalTypes, parent.parentBlock)
                ) &&
                !isExpectedClassType(excludeTypes, parent)
            ) {
                return true
            }

            if (parent is SqlSubGroupBlock) {
                val firstChild =
                    parent.getChildBlocksDropLast(skipCommentBlock = true).firstOrNull()
                if (firstChild is SqlKeywordGroupBlock) {
                    return firstChild.indent.indentLevel != IndentType.TOP
                }
            }
        }
        return false
    }
}
