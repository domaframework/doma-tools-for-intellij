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

import com.intellij.formatting.Alignment
import com.intellij.formatting.Indent
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlSubQueryGroupBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
) : SqlSubGroupBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
    ) {
    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? = Indent.getSpaceIndent(indent.indentLen)

    override fun createBlockIndentLen(): Int = 1

    private fun createGroupIndentLen(): Int {
        parentBlock?.let {
            if (it is SqlJoinGroupBlock) {
                var parentLen = 0
                val keywords =
                    it.childBlocks.dropLast(1).takeWhile { it.node.elementType == SqlTypes.KEYWORD }
                keywords.forEach { keyword ->
                    parentLen = parentLen.plus(keyword.getNodeText().length).plus(1)
                }
                return it.indent.indentLen
                    .plus(it.getNodeText().length)
                    .plus(2)
                    .plus(parentLen)
            } else {
                var parentLen = 0
                if (prevChildren?.findLast { it.indent.indentLevel == IndentType.COMMA } == null) {
                    prevChildren
                        ?.dropLast(1)
                        ?.forEach { prev ->
                            parentLen = parentLen.plus(prev.getNodeText().length).plus(1)
                        }
                }
                return it.indent.groupIndentLen
                    .plus(parentLen)
                    .plus(2)
            }
            return it.indent.groupIndentLen
                .plus(2)
        } ?: return 1
    }
}
