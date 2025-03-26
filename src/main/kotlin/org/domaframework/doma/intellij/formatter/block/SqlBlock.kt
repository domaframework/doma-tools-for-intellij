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
import com.intellij.formatting.ChildAttributes
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.psi.SqlTypes

enum class IndentType(
    level: Int,
) {
    FILE(0),
    TOP(1),
    SECOND(2),
    SUB(3),
    COMMA(4),

    // SUB_QUERY(5),
    OPTION(6),
}

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
    open var parentBlock: SqlBlock? = null

    open fun setParentGroupBlock(block: SqlBlock?) {
        parentBlock = block
    }

    open val indentLevel = IndentType.FILE
    open var indentLen = 0

    private val groupTopNodeIndexHistory = mutableListOf<Pair<Int, SqlBlock>>()

    protected open val pendingCommentBlocks = mutableListOf<SqlBlock>()

    public override fun buildChildren(): MutableList<AbstractBlock> {
        if (isLeaf) return mutableListOf()

        var child = node.firstChildNode
        var settingWhiteSpace = false
        var prevNonWhiteSpaceNode: ASTNode? = null
        groupTopNodeIndexHistory.add(Pair(0, this))
        while (child != null) {
            settingWhiteSpace = false
            if (child !is PsiWhiteSpace) {
                val childBlock = getBlock(child)
                if (blocks.isNotEmpty() && blocks.last() is SqlWhitespaceBlock) {
                    if ((childBlock is SqlKeywordBlock && childBlock.indentLevel < IndentType.SUB) ||
                        childBlock is SqlCommaBlock
                    ) {
                        val whiteBlock = blocks.last() as SqlBlock
                        whiteBlock.parentBlock =
                            groupTopNodeIndexHistory.lastOrNull()?.second
                        settingWhiteSpace = true
                    } else {
                        blocks.removeLast()
                    }
                }
                prevNonWhiteSpaceNode = child
                updateSearchKeywordLevelHistory(childBlock, child, parentBlock)
                blocks.add(childBlock)
            } else {
                blocks.add(
                    SqlWhitespaceBlock(
                        child,
                        parentBlock,
                        wrap,
                        alignment,
                        spacingBuilder,
                    ),
                )
            }
            child = child.treeNext
        }
        blocks.addAll(pendingCommentBlocks)

        println("=========Build Block: Top")
        println("Blocks Size: ${blocks.size}")
        println("Blocks: ${blocks.map { "Parent: \"${(it as? SqlBlock)?.parentBlock?.node?.text}\" -->  \"${it.node.text}\"\n" }}")
        println("=========Build Block: END")

        return blocks
    }

    protected open fun updateSearchKeywordLevelHistory(
        childBlock: SqlBlock,
        child: ASTNode,
        parentKeywordBlock: SqlBlock?,
    ) {
        val lastIndentLevel = groupTopNodeIndexHistory.last().second.indentLevel
        val latestGroupTopBlock = groupTopNodeIndexHistory.last().second
        when (childBlock) {
            is SqlKeywordBlock -> {
                println("Keyword: ${child.text}")
                if (latestGroupTopBlock.indentLevel == IndentType.SUB) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups latestGroupTopBlock
                    }
                } else if (lastIndentLevel == childBlock.indentLevel) {
                    groupTopNodeIndexHistory.removeLast()
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups latestGroupTopBlock.parentBlock
                    }
                } else if (lastIndentLevel < childBlock.indentLevel) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history.last().second
                    }
                } else {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history.lastOrNull { it.second.indentLevel < childBlock.indentLevel }?.second
                    }
                }
            }
            is SqlWordBlock, is SqlOtherBlock, is SqlLineCommentBlock, is SqlBlockCommentBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }
            is SqlElSymbolBlock -> {
                when (child.elementType) {
                    SqlTypes.LEFT_PAREN -> {
                        setParentGroups(
                            childBlock,
                        ) { history ->
                            return@setParentGroups history.last().second
                        }
                    }

                    SqlTypes.RIGHT_PAREN -> {
                        val leftIndex =
                            groupTopNodeIndexHistory.indexOfLast { it.second.indentLevel == IndentType.SUB }
                        if (leftIndex >= 0) {
                            setParentGroups(
                                childBlock,
                            ) { history ->
                                return@setParentGroups history[leftIndex].second
                            }
                            groupTopNodeIndexHistory
                                .subList(
                                    leftIndex,
                                    groupTopNodeIndexHistory.size,
                                ).clear()
                            println("hit RIGHT_PAREN: parent ${parentKeywordBlock?.node?.text}")
                        }
                        pendingCommentBlocks.clear()
                    }

                    else -> {
                        setParentGroups(
                            childBlock,
                        ) { history ->
                            return@setParentGroups history.last().second
                        }
                    }
                }
            }
            else -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }
        }
    }

    private fun setParentGroups(
        childBlock: SqlBlock,
        getParentGroup: (MutableList<Pair<Int, SqlBlock>>) -> SqlBlock?,
    ) {
        val parentGroup = getParentGroup(groupTopNodeIndexHistory)
        childBlock.setParentGroupBlock(parentGroup)

        if (childBlock is SqlKeywordBlock) {
            groupTopNodeIndexHistory.add(Pair(blocks.size - 1, childBlock))
        }
    }

    open fun getBlock(child: ASTNode): SqlBlock {
        return when (child.elementType) {
            SqlTypes.KEYWORD -> {
                return getKeywordBlock(child)
            }

            SqlTypes.LEFT_PAREN -> SqlKeywordBlock(child, IndentType.SUB, wrap, alignment, spacingBuilder)
            SqlTypes.RIGHT_PAREN -> SqlElSymbolBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.OTHER ->
                return SqlOtherBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.DOT, SqlTypes.RIGHT_PAREN ->
                return SqlElSymbolBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.COMMA ->
                return SqlCommaBlock(child, wrap, alignment, spacingBuilder)
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

    private fun getKeywordBlock(child: ASTNode): SqlKeywordBlock {
        val lower = child.text.lowercase()
        val indentLevel =
            when (lower) {
                "select" -> IndentType.TOP
                "from", "where" -> IndentType.SECOND
                "(" -> IndentType.SUB
                else -> IndentType.OPTION
            }
        return SqlKeywordBlock(child, indentLevel, wrap, alignment, spacingBuilder)
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
        if (child1 is SqlWhitespaceBlock && child2 !is SqlKeywordBlock && child2 !is SqlCommaBlock) {
            return Spacing.createSpacing(0, 0, 0, false, 0, 0)
        }

        if (child2 is SqlKeywordBlock) {
            when (child1) {
                is SqlWhitespaceBlock ->
                    customSpacingBuilder
                        ?.getSpacingWithWhiteSpace(
                            child1,
                            child2,
                        )?.let { return it }

                else ->
                    SqlCustomSpacingBuilder()
                        .getSpacingWithIndentLevel(child2)
                        ?.let { return it }
            }
        }

        if (child1 is SqlBlock && child2 is SqlCommaBlock) {
            SqlCustomSpacingBuilder().getSpacingWithIndentComma(child1, child2)?.let { return it }
        }

        val spacing: Spacing? = customSpacingBuilder?.getSpacing(child1, child2)
        return spacing ?: spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        blocks
            .getOrNull(newChildIndex)
            ?.let {
                val indent =
                    when (it) {
                        is SqlKeywordBlock -> Indent.getSpaceIndent(it.indentLen)
                        else -> childIndent ?: Indent.getNoneIndent()
                    }
                return ChildAttributes(indent, null)
            }
        return ChildAttributes(Indent.getNoneIndent(), null)
    }

    override fun getChildIndent(): Indent? = Indent.getSpaceIndent(4)

    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
