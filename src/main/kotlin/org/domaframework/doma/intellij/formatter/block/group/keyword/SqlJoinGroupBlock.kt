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
package org.domaframework.doma.intellij.formatter.block.group.keyword

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlJoinGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(
        node,
        IndentType.JOIN,
        context,
    ) {
    override val indent =
        ElementIndent(
            IndentType.JOIN,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        parentBlock = lastGroup
        parentBlock?.childBlocks?.add(this)
        indent.indentLevel = IndentType.JOIN
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        return parentBlock?.let { parent ->
            if (parent is SqlElConditionLoopCommentBlock) {
                return parent.indent.groupIndentLen
            }
            return parent.indent.groupIndentLen.plus(1)
        } ?: 1
    }

    override fun createGroupIndentLen(): Int =
        indent.indentLen
            .plus(
                topKeywordBlocks
                    .drop(1)
                    .filter { it !is SqlLateralGroupBlock }
                    .sumOf { it.getNodeText().length.plus(1) },
            ).plus(getNodeText().length)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true
}
