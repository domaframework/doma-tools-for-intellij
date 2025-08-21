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
package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

abstract class SqlCommentBlock(
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
    companion object {
        const val DEFAULT_INDENT = 0
        const val SINGLE_INDENT = 1
    }

    override val indent =
        ElementIndent(
            IndentType.NONE,
            DEFAULT_INDENT,
            DEFAULT_INDENT,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.NONE
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun isLeaf(): Boolean = true

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (shouldInheritParentIndent(parent)) {
                return parent.indent.indentLen
            }
        }
        return DEFAULT_INDENT
    }

    override fun createGroupIndentLen(): Int = indent.indentLen

    protected open fun shouldInheritParentIndent(parent: SqlBlock): Boolean =
        parent.parentBlock !is SqlSubGroupBlock || parent.parentBlock?.childBlocks?.size != 1

    fun hasLineBreakBefore(): Boolean = PsiTreeUtil.prevLeaf(node.psi)?.text?.contains(StringUtil.LINE_SEPARATE) == true

    protected fun buildChildBlocks(blockProvider: (ASTNode) -> SqlBlock): MutableList<AbstractBlock> {
        val blocks = mutableListOf<AbstractBlock>()
        var child = node.firstChildNode
        while (child != null) {
            if (child !is PsiWhiteSpace) {
                blocks.add(blockProvider(child))
            }
            child = child.treeNext
        }
        return blocks
    }
}
