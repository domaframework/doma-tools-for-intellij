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

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

abstract class SqlSubGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlNewGroupBlock(
        node,
        context,
    ) {
    var isFirstLineComment = false
    var prevChildren: List<SqlBlock>? = emptyList<SqlBlock>()
    var endPatternBlock: SqlRightPatternBlock? = null

    override val indent =
        ElementIndent(
            IndentType.SUB,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        prevChildren = parentBlock?.childBlocks?.toList()
        indent.indentLevel = indent.indentLevel
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = parentBlock?.let { parent ->
            parent.indent.indentLen.plus(parent.getNodeText().length.plus(1))
        } ?: indent.indentLen.plus(getNodeText().length)
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlDoGroupBlock)?.doQueryBlock = this
    }

    override fun addChildBlock(childBlock: SqlBlock) {
        childBlocks.add(childBlock)
        if (!isFirstLineComment) {
            isFirstLineComment = childBlock is SqlCommentBlock
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getSpacing(
        p0: Block?,
        p1: Block,
    ): Spacing? = null

    override fun isLeaf(): Boolean = true

    open fun endGroup() {}
}
