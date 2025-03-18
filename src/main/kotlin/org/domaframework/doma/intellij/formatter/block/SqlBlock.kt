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
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.block.group.SqlFromGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlSelectGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlWhereGroupBlock
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
    val blocks = mutableListOf<AbstractBlock>()

    var indentCount = 0
    protected var blockSkip = false
    protected var endBlock = false

    open val indentLevel = 0
    protected open var searchKeywordLevel = 0

    protected open val searchKeywordLevelHistory = mutableListOf<Int>()
    private val pendingCommentBlocks = mutableListOf<SqlBlock>()

    public override fun buildChildren(): MutableList<AbstractBlock> {
        var child = node.firstChildNode
        var nonWhiteSpaceChild: SqlBlock? = null
        searchKeywordLevelHistory.add(0)
        while (child != null && !endBlock) {
            if (child is PsiWhiteSpace && !blockSkip) {
                blocks.add(
                    SqlWhitespaceBlock(
                        child,
                        wrap,
                        alignment,
                        nonWhiteSpaceChild,
                        spacingBuilder,
                    ),
                )
            } else {
                val childBlock = getBlock(child)
                if (blocks.isNotEmpty()) {
                    (blocks.last() as? SqlWhitespaceBlock)?.nextNode = childBlock
                }
                updateSearchKeywordLevelHistory(childBlock, child)
                nonWhiteSpaceChild = childBlock
            }
            child = child.treeNext
        }
        blocks.addAll(pendingCommentBlocks)

        if (!isLeaf) {
            println("=========Build Block: Top")
            println("Blocks Size: ${blocks.size}")
            println("Blocks: ${blocks.map { it.node.text }}")
            println("=========Build Block: END")
        }
        return blocks
    }

    protected open fun updateSearchKeywordLevelHistory(
        childBlock: SqlBlock,
        child: ASTNode,
    ) {
        if (child.elementType == SqlTypes.RIGHT_PAREN) {
            blockSkip = false
            val leftIndex = searchKeywordLevelHistory.indexOfLast { it == 3 }
            if (leftIndex >= 0) {
                searchKeywordLevelHistory
                    .subList(
                        leftIndex,
                        searchKeywordLevelHistory.size,
                    ).clear()
                println("hit RIGHT_PAREN")
            }
            pendingCommentBlocks.clear()
            return
        }
        val childIndentLevel = childBlock.indentLevel
        val lastIndentLevel = searchKeywordLevelHistory.last()
        when (childBlock) {
            is SqlGroupBlock -> {
                println("Hit Group Blocks: ${child.text} $childIndentLevel")
                // 最後のブロックより同等以上のブロックが来た場合
                if (lastIndentLevel >= childIndentLevel) {
                    when (lastIndentLevel) {
                        3 -> {
                            searchKeywordLevelHistory.add(childIndentLevel)
                        }

                        else -> {
                            blockSkip = true
                            if (lastIndentLevel == childIndentLevel) {
                                if (!searchKeywordLevelHistory.contains(3)) {
                                    blocks.addAll(pendingCommentBlocks)
                                    pendingCommentBlocks.clear()
                                    println("Top Add Node: ${child.text} : $lastIndentLevel $childIndentLevel")
                                    blocks.add(childBlock)
                                }
                            } else {
                                // lastIndentLevel > childIndentLevel
                                searchKeywordLevelHistory.removeLast()
                                searchKeywordLevelHistory.add(childIndentLevel)
                                blocks.addAll(pendingCommentBlocks)
                                pendingCommentBlocks.clear()
                                blocks.add(childBlock)
                            }
                        }
                    }
                } else {
                    // 最後の要素より下位のブロックが来た場合
                    searchKeywordLevelHistory.add(childIndentLevel)
                    if (!blockSkip &&
                        childIndentLevel <= indentLevel + 1
                    ) {
                        blocks.add(childBlock)
                        println("Top Add Node: ${child.text} : $lastIndentLevel $childIndentLevel")
                        blockSkip = true
                    }
                }
                println("Top Blocks: $searchKeywordLevelHistory")
                pendingCommentBlocks.clear()
            }

            is SqlBlockCommentBlock, is SqlLineCommentBlock -> {
                pendingCommentBlocks.add(childBlock)
            }
        }
    }

    open fun getBlock(child: ASTNode): SqlBlock {
        return when (child.elementType) {
            SqlTypes.KEYWORD -> {
                return getKeywordBlock(child)
            }

            SqlTypes.LEFT_PAREN -> SqlSubGroupBlock(child, wrap, alignment, this, spacingBuilder)

            SqlTypes.OTHER ->
                return SqlOtherBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.COMMA, SqlTypes.DOT, SqlTypes.RIGHT_PAREN ->
                return SqlElSymbolBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.WORD ->
                return SqlWordBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.BLOCK_COMMENT ->
                return SqlElBlockCommentBlock(
                    child,
                    wrap,
                    alignment,
                    createBlockCommentSpacingBuilder(),
                    spacingBuilder,
                )

            SqlTypes.LINE_COMMENT ->
                return SqlLineCommentBlock(child, wrap, alignment, spacingBuilder)

            else -> SqlUnknownBlock(child, wrap, alignment, spacingBuilder)
        }
    }

    fun getKeywordBlock(child: ASTNode): SqlBlock {
        val lowercaseText = child.text.lowercase()
        return when (lowercaseText) {
            "select" -> {
                val block =
                    SqlSelectGroupBlock(
                        child,
                        wrap,
                        alignment,
                        this,
                        spacingBuilder,
                    )
                return block
            }

            "from" -> {
                val block =
                    SqlFromGroupBlock(
                        child,
                        wrap,
                        alignment,
                        this,
                        spacingBuilder,
                    )
                return block
            }

            "where" -> {
                val block =
                    SqlWhereGroupBlock(
                        child,
                        wrap,
                        alignment,
                        this,
                        spacingBuilder,
                    )
                return block
            }

            else -> {
                return SqlKeywordBlock(child, wrap, alignment, spacingBuilder)
            }
        }
    }

    protected open fun createSpacingBuilder(): SqlCustomSpacingBuilder = SqlCustomSpacingBuilder()

    protected fun createBlockCommentSpacingBuilder(): SqlCustomSpacingBuilder =
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
        val prevChild = findPrevNonWhiteSpace(child1)
        val nextChild = findNextNonWhiteSpace(child2)
        val spacing: Spacing? = customSpacingBuilder?.getSpacing(this, prevChild, nextChild)
        return spacing ?: spacingBuilder.getSpacing(this, prevChild, nextChild)
    }

    protected fun findPrevNonWhiteSpace(block: Block?): Block? {
        var current: Block? = block
        while (current != null && current is SqlWhitespaceBlock) {
            current = current.prevNode
        }
        return current
    }

    protected fun findNextNonWhiteSpace(block: Block?): Block? {
        var current: Block? = block
        while (current != null && current is SqlWhitespaceBlock) {
            current = current.nextNode
        }
        return current
    }

    override fun isLeaf(): Boolean = false
}
