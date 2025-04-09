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
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlUpdateKeywordGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * In an UPDATE statement using the row value constructor,
 * a group representing the value list
 */
class SqlUpdateValueGroupBlock(
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

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlUpdateKeywordGroupBlock) {
                val keywords =
                    parent.childBlocks
                        .dropLast(1)
                        .takeWhile { parent.node.elementType == SqlTypes.KEYWORD }
                return parent.indent.indentLen
                    .plus(parent.node.text.length)
                    .plus(3)
            }
            // TODO:Customize indentation
            return 2
        } ?: return 2
    }

    private fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlUpdateKeywordGroupBlock) {
                val parentGroupIndent = parent.indent.groupIndentLen
                return parentGroupIndent.plus(4)
            }
        } ?: return 2
        return 2
    }
}
