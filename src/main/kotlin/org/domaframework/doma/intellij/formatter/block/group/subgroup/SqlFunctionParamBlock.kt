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
package org.domaframework.doma.intellij.formatter.block.group.subgroup

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlFunctionGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlFunctionParamBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    override val indent =
        ElementIndent(
            IndentType.PARAM,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.PARAM
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlFunctionGroupBlock)?.parameterGroupBlock = this
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->

            if (parent is SqlElConditionLoopCommentBlock) return parent.indent.groupIndentLen

            if (parent !is SqlSubGroupBlock) {
                return if (parent is SqlFunctionGroupBlock) {
                    parent.indent.groupIndentLen
                } else {
                    parent.indent.groupIndentLen.plus(1)
                }
            }

            val children =
                prevChildren?.dropLast(1)?.filter {
                    it !is SqlDefaultCommentBlock
                    it.node != SqlTypes.DOT
                }
            children?.let { prevList ->
                return prevList
                    .sumOf { it.getNodeText().length.plus(1) }
                    .plus(parent.indent.groupIndentLen)
                    .plus(getNodeText().length)
                    .plus(1)
            }
        }
        return 0
    }

    override fun createGroupIndentLen(): Int {
        val parentFunctionName = parentBlock as? SqlFunctionGroupBlock
        parentFunctionName?.let { parent ->
            return parent.indent.groupIndentLen
                .plus(getNodeText().length)
        }

        val prevChildrenDropLast =
            prevChildren?.dropLast(1)?.filter {
                it !is SqlDefaultCommentBlock &&
                    it.node.elementType != SqlTypes.DOT
            }
                ?: emptyList()
        val prevLength =
            prevChildrenDropLast
                .sumOf { it.getNodeText().length }
                .plus(getNodeText().length)
        return prevLength.plus(prevChildrenDropLast.count()).plus(1)
    }
}
