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
package org.domaframework.doma.intellij.formatter.block.word

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlWordBlock(
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
    override val indent =
        ElementIndent(
            IndentType.NONE,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.groupIndentLen = indent.indentLen
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlWithCommonTableGroupBlock) {
            lastGroup.commonTableNameBlock = this
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            when (parent) {
                // Calculate indentation assuming function parameters or within a list.
                is SqlSubQueryGroupBlock -> {
                    val parentIndentLen = parent.indent.groupIndentLen
                    val grand = parent.parentBlock
                    if (grand != null && grand.getNodeText().lowercase() == "create") {
                        val grandIndentLen = grand.indent.groupIndentLen
                        return grandIndentLen.plus(parentIndentLen).plus(1)
                    }
                    return parentIndentLen.plus(1)
                }

                else -> {
                    return parent.indent.groupIndentLen.plus(1)
                }
            }
        }
        return 1
    }
}
