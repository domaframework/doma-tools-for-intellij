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
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElDotBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlArrayListGroupBlock
import org.domaframework.doma.intellij.formatter.block.other.SqlOtherBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlArrayWordBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlWordBlock(
        node,
        context,
    ) {
    var arrayParams: SqlArrayListGroupBlock? = null

    override val indent =
        ElementIndent(
            IndentType.NONE,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int =
        when (val parent = parentBlock) {
            is SqlElConditionLoopCommentBlock -> parent.indent.groupIndentLen
            else -> 1
        }

    override fun createGroupIndentLen(): Int =
        parentBlock
            ?.getChildBlocksDropLast()
            ?.sumOf {
                when (it) {
                    is SqlOtherBlock, is SqlElDotBlock -> it.getNodeText().length
                    else -> it.getNodeText().length.plus(1)
                }
            }?.plus(parentBlock?.indent?.groupIndentLen?.plus(1) ?: 1)
            ?.plus(getNodeText().length.plus(1))
            ?: getNodeText().length.plus(1)
}
