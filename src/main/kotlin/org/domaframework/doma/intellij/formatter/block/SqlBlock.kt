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
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val customSpacingBuilder: SqlCustomSpacingBuilder?,
    internal val spacingBuilder: SpacingBuilder,
) : AbstractBlock(
        node,
        wrap,
        alignment,
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

    open fun getBlock(child: ASTNode): AbstractBlock {
        val lowercaseText = child.text.lowercase()
        return when (child.elementType) {
            SqlTypes.KEYWORD -> {
                if (listOf(
                        "select",
                        "update",
                        "set",
                        "insert",
                        "from",
                        "where",
                        "with",
                        "and",
                        "or",
                        "left",
                        "inner",
                        "right",
                        "cross",
                        "inner",
                    ).contains(lowercaseText)
                ) {
                    SqlTopKeywordBlock(child, wrap, alignment, 1, spacingBuilder)
                } else {
                    if (lowercaseText == "join") {
                        getJoinTopKeyword(child)
                    } else {
                        SqlTopKeywordBlock(child, wrap, alignment, 1, spacingBuilder)
                    }
                }
            }

            SqlTypes.OTHER ->
                SqlOtherBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.WORD ->
                SqlWordBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.BLOCK_COMMENT ->
                SqlElBlockCommentBlock(child, wrap, alignment, createBlockCommentSpacingBuilder(), spacingBuilder)

            else -> SqlBlock(child, wrap, alignment, null, spacingBuilder)
        }
    }

    private fun getJoinTopKeyword(child: ASTNode): SqlKeywordBlock =
        if (PsiTreeUtil
                .skipSiblingsBackward(
                    child.psi,
                    PsiWhiteSpace::class.java,
                )?.elementType == SqlTypes.KEYWORD
        ) {
            SqlKeywordBlock(child, wrap, alignment, spacingBuilder)
        } else {
            SqlTopKeywordBlock(child, wrap, alignment, 1, spacingBuilder)
        }

    protected open fun createSpacingBuilder(): SqlCustomSpacingBuilder = SqlCustomSpacingBuilder()

    private fun createBlockCommentSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.BLOCK_COMMENT_CONTENT,
                Spacing.createSpacing(0, 0, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_CONTENT,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(0, 0, 0, true, 0),
            ).withSpacing(
                SqlTypes.EL_FIELD_ACCESS_EXPR,
                SqlTypes.OTHER,
                Spacing.createSpacing(1, 1, 0, false, 0),
            ).withSpacing(
                SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR,
                SqlTypes.OTHER,
                Spacing.createSpacing(1, 1, 0, false, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_CONTENT,
                SqlTypes.BLOCK_COMMENT_START,
                Spacing.createSpacing(0, 0, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_CONTENT,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(0, 0, 0, true, 0),
            )

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? {
        val spacing: Spacing? = customSpacingBuilder?.getSpacing(this, child1, child2)
        return spacing ?: spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun isLeaf(): Boolean = false
}
