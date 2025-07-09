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
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictClauseBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
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
    fun enableLastRight() {
        parentBlock?.let { parent ->
            // TODO:Customize spacing
            val notInsertSpaceClassList =
                listOf(
                    SqlFunctionParamBlock::class,
                    SqlInsertColumnGroupBlock::class,
                    SqlWithQuerySubGroupBlock::class,
                )
            if (isExpectedClassType(notInsertSpaceClassList, parent)) {
                preSpaceRight = false
                return
            }

            if (parent.parentBlock is SqlConflictClauseBlock) {
                preSpaceRight = false
                return
            }

            if (parent is SqlSubQueryGroupBlock) {
                val prevKeywordBlock =
                    parent.childBlocks
                        .filter { it.node.startOffset < node.startOffset }
                        .find { it is SqlKeywordGroupBlock && it.indent.indentLevel == IndentType.TOP }
                if (prevKeywordBlock != null) {
                    preSpaceRight = true
                    return
                }
            }

            parent.parentBlock?.let { grand ->
                preSpaceRight = grand.childBlocks.find { it is SqlKeywordGroupBlock } != null
                return
            }
        }
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
        indent.indentLevel = IndentType.NONE
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen
        enableLastRight()
        (lastGroup as? SqlSubGroupBlock)?.endPatternBlock = this
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlWithQuerySubGroupBlock) return 0
            val exceptionalTypes =
                listOf(
                    SqlUpdateColumnGroupBlock::class,
                    SqlUpdateValueGroupBlock::class,
                    SqlCreateTableColumnDefinitionGroupBlock::class,
                )
            if (isExpectedClassType(exceptionalTypes, parent)) return parent.indent.indentLen
            return parent.indent.groupIndentLen
        } ?: return 0
    }

    override fun isLeaf(): Boolean = true

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        val exceptionalTypes =
            listOf(
                SqlCreateTableColumnDefinitionGroupBlock::class,
                SqlColumnDefinitionRawGroupBlock::class,
                SqlUpdateColumnGroupBlock::class,
                SqlUpdateValueGroupBlock::class,
                SqlWithQuerySubGroupBlock::class,
            )
        if (isExpectedClassType(exceptionalTypes, parentBlock)) return true

        val parentExceptionalTypes =
            listOf(
                SqlUpdateSetGroupBlock::class,
                SqlUpdateColumnGroupBlock::class,
                SqlUpdateValueGroupBlock::class,
            )
        return isExpectedClassType(parentExceptionalTypes, parentBlock?.parentBlock)
    }
}
