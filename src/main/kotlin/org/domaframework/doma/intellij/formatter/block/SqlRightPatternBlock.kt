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

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInsertKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlUpdateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateValueGroupBlock

/**
 * Parent is always a subclass of a subgroup
 */
open class SqlRightPatternBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
    enableFormat: Boolean,
    formatMode: FormattingMode,
) : SqlBlock(
        node,
        wrap,
        alignment,
        null,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
    var preSpaceRight = false

    fun enableLastRight() {
        parentBlock?.let { parent ->
            // TODO:Customize indentation
            if (parent is SqlFunctionParamBlock) {
                preSpaceRight = false
                return
            }
            if (parent is SqlInsertColumnGroupBlock) {
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
                preSpaceRight = (
                    grand.indent.indentLevel <= IndentType.SECOND &&
                        grand.parentBlock !is SqlInsertKeywordGroupBlock
                ) ||
                    grand.indent.indentLevel == IndentType.JOIN
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

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLevel = IndentType.NONE
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen
        enableLastRight()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int =
        if (parentBlock is SqlUpdateColumnGroupBlock || parentBlock is SqlUpdateValueGroupBlock) {
            parentBlock?.indent?.indentLen ?: 1
        } else {
            parentBlock?.indent?.groupIndentLen ?: 1
        }

    override fun isLeaf(): Boolean = true

    fun isNewLine(lastGroup: SqlBlock?): Boolean =
        lastGroup is SqlColumnDefinitionGroupBlock ||
            lastGroup is SqlColumnDefinitionRawGroupBlock ||
            lastGroup?.parentBlock is SqlUpdateKeywordGroupBlock ||
            lastGroup?.parentBlock is SqlUpdateColumnGroupBlock ||
            lastGroup is SqlUpdateColumnGroupBlock ||
            lastGroup is SqlUpdateValueGroupBlock ||
            lastGroup?.parentBlock is SqlUpdateValueGroupBlock
}
