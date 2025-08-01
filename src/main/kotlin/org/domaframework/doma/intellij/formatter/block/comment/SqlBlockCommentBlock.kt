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

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlBlockCommentBlock(
    node: ASTNode,
    private val customSpacingBuilder: SqlCustomSpacingBuilder,
    private val context: SqlBlockFormattingContext,
) : SqlDefaultCommentBlock(
        node,
        context,
    ) {
    override fun buildChildren(): MutableList<AbstractBlock> {
        val blocks = mutableListOf<AbstractBlock>()
        var child = node.firstChildNode
        while (child != null) {
            if (child !is PsiWhiteSpace) {
                val block = getBlock(child)
                blocks.add(block)
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getBlock(child: ASTNode): SqlBlock {
        val elementType = child.elementType
        return when (elementType) {
            SqlTypes.BLOCK_COMMENT_START -> SqlCommentStartBlock(child, context)
            SqlTypes.BLOCK_COMMENT_END -> SqlCommentEndBlock(child, context)
            SqlTypes.BLOCK_COMMENT_CONTENT -> SqlCommentContentBlock(child, context)
            else -> SqlUnknownBlock(child, context)
        }
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean =
        PsiTreeUtil.prevLeaf(node.psi)?.text?.contains(StringUtil.LINE_SEPARATE) == true

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? =
        customSpacingBuilder.getCustomSpacing(child1, child2)
            ?: SqlCustomSpacingBuilder.normalSpacing
}
