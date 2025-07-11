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
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlFromGroupBlock
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
        null,
        context.spacingBuilder,
        context.enableFormat,
        context.formatMode,
    ) {
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
        indent.groupIndentLen = indent.indentLen.plus(getNodeText().length)
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

                val parentIndentSyncBlockTypes =
                    listOf(
                        SqlUpdateColumnGroupBlock::class,
                        SqlInsertColumnGroupBlock::class,
                        SqlWithColumnGroupBlock::class,
                    )
                val parentIndentLen = parent.indent.groupIndentLen
                if (TypeUtil.isExpectedClassType(parentIndentSyncBlockTypes, parent)) {
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

                if (parent is SqlValuesParamGroupBlock) return 0

                val grand = parent.parentBlock
                grand?.let { grand ->
                    if (grand is SqlCreateKeywordGroupBlock) {
                        val grandIndentLen = grand.indent.groupIndentLen
                        return grandIndentLen.plus(parentIndentLen).minus(1)
                    }

                    val grandIndent = grand.indent.indentLen
                    val groupIndent = parentBlock?.indent?.groupIndentLen ?: 0

                    if (grand is SqlColumnRawGroupBlock) {
                        return groupIndent.plus(grandIndent)
                    }
                    return groupIndent.plus(grandIndent).minus(1)
                }
                return parentIndentLen.plus(1)
            } else {
                if (parent is SqlValuesGroupBlock) return parent.indent.indentLen
                return parent.indent.groupIndentLen.plus(1)
            }
        }
        return 1
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        val excludeTypes =
            listOf(
                SqlConditionalExpressionGroupBlock::class,
            )
        if (TypeUtil.isExpectedClassType(excludeTypes, parentBlock)) return false

        val expectedTypes =
            listOf(
                SqlInsertColumnGroupBlock::class,
                SqlInsertValueGroupBlock::class,
                SqlUpdateSetGroupBlock::class,
                SqlUpdateColumnGroupBlock::class,
                SqlUpdateValueGroupBlock::class,
                SqlFunctionParamBlock::class,
                SqlWithColumnGroupBlock::class,
                SqlKeywordGroupBlock::class,
            )
        return TypeUtil.isExpectedClassType(expectedTypes, parentBlock)
    }
}
