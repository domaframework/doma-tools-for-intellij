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
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInsertKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlCommaBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
) : SqlBlock(
        node,
        wrap,
        alignment,
        null,
        spacingBuilder,
    ) {
    override val indent =
        ElementIndent(
            IndentType.COMMA,
            0,
            0,
        )

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLevel = IndentType.COMMA
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(node.text.length)
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? = Indent.getSpaceIndent(indent.indentLen)

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlSubGroupBlock) {
                if (parent is SqlParallelListBlock) {
                    return 0
                }

                val parentIndentLen = parent.indent.groupIndentLen
                if (parent is SqlUpdateColumnGroupBlock || parent is SqlUpdateValueGroupBlock) {
                    return parentIndentLen
                }

                val grand = parent.parentBlock
                grand?.let { grand ->
                    if (grand is SqlCreateKeywordGroupBlock) {
                        val grandIndentLen = grand.indent.groupIndentLen
                        return grandIndentLen.plus(parentIndentLen).minus(1)
                    }
                    if (grand is SqlInsertKeywordGroupBlock) {
                        return parentIndentLen
                    }
                    if (grand is SqlColumnGroupBlock) {
                        val grandIndentLen = grand.indent.groupIndentLen
                        var prevTextLen = 1
                        parent.prevChildren?.dropLast(1)?.forEach { prev -> prevTextLen = prevTextLen.plus(prev.node.text.length) }
                        return grandIndentLen.plus(prevTextLen).plus(1)
                    }
                }
                return parentIndentLen
            } else {
                var prevLen = 0
                parent.childBlocks
                    .filter { it.node.elementType == SqlTypes.KEYWORD }
                    .forEach { prev ->
                        prevLen =
                            prevLen.plus(
                                prev.node.text.length
                                    .plus(1),
                            )
                    }
                return parent.indent.groupIndentLen
                    .plus(prevLen)
                    .plus(1)
            }
        }
        return 1
    }
}
