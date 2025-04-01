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
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.SqlBlockBuilder
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.SqlKeywordUtil
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElDotBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElSymbolBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlSubQueryGroupBlock
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
    open var parentBlock: SqlBlock? = null
    open val childBlocks = mutableListOf<SqlBlock>()

    open fun setParentGroupBlock(block: SqlBlock?) {
        parentBlock = block
        parentBlock?.childBlocks?.add(this)
    }

    open val indent: ElementIndent =
        ElementIndent(
            IndentType.FILE,
            0,
            0,
        )

    private val blockBuilder = SqlBlockBuilder()

    protected open val pendingCommentBlocks = mutableListOf<SqlBlock>()

    public override fun buildChildren(): MutableList<AbstractBlock> {
        if (isLeaf) return mutableListOf()

        var child = node.firstChildNode
        var prevNonWhiteSpaceNode: ASTNode? = null
        blockBuilder.addGroupTopNodeIndexHistory(Pair(0, this))
        while (child != null) {
            if (child !is PsiWhiteSpace) {
                val childBlock = getBlock(child)
                if (blocks.isNotEmpty() && blocks.last() is SqlWhitespaceBlock) {
                    val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
                    if (isSaveWhiteSpace(childBlock, child, lastGroup)) {
                        val whiteBlock = blocks.last() as SqlBlock
                        whiteBlock.parentBlock = lastGroup
                    } else {
                        blocks.removeLast()
                    }
                }
                prevNonWhiteSpaceNode = child
                updateSearchKeywordLevelHistory(childBlock, child)
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

        return blocks
    }

    private fun isSaveWhiteSpace(
        childBlock: SqlBlock,
        child: ASTNode,
        lastGroup: SqlBlock?,
    ): Boolean =
        (
            childBlock.indent.indentLevel.isNewLineGroup() &&
                !SqlKeywordUtil.isSetLineKeyword(
                    child.text,
                    lastGroup?.node?.text ?: "",
                )
        ) ||
            childBlock.node.elementType == SqlTypes.COMMA ||
            childBlock is SqlColumnDefinitionRawGroupBlock ||
            childBlock is SqlColumnDefinitionGroupBlock ||
            (
                childBlock is SqlRightPatternBlock &&
                    (
                        lastGroup is SqlColumnDefinitionGroupBlock ||
                            lastGroup is SqlColumnDefinitionRawGroupBlock
                    )
            )

    protected open fun updateSearchKeywordLevelHistory(
        childBlock: SqlBlock,
        child: ASTNode,
    ) {
        val lastGroupBlock = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
        val lastIndentLevel = lastGroupBlock?.indent?.indentLevel
        if (lastGroupBlock == null || lastIndentLevel == null) {
            setParentGroups(
                childBlock,
            ) { history ->
                return@setParentGroups null
            }
            return
        }

        when (childBlock) {
            is SqlKeywordGroupBlock -> {
                if (lastGroupBlock.indent.indentLevel == IndentType.SUB) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups lastGroupBlock
                    }
                } else if (lastIndentLevel == childBlock.indent.indentLevel) {
                    blockBuilder.removeLastGroupTopNodeIndexHistory()
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups lastGroupBlock.parentBlock
                    }
                } else if (lastIndentLevel < childBlock.indent.indentLevel) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history.last().second
                    }
                } else {
                    if (lastIndentLevel == IndentType.JOIN &&
                        SqlKeywordUtil.isSecondOptionKeyword(child.text)
                    ) {
                        // left,right < inner,outer < join
                        setParentGroups(
                            childBlock,
                        ) { history ->
                            return@setParentGroups history.last().second
                        }
                        return
                    }

                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history
                            .lastOrNull { it.second.indent.indentLevel < childBlock.indent.indentLevel }
                            ?.second
                    }
                }
            }

            is SqlColumnGroupBlock -> {
                when (lastIndentLevel) {
                    childBlock.indent.indentLevel -> {
                        blockBuilder.removeLastGroupTopNodeIndexHistory()
                        setParentGroups(
                            childBlock,
                        ) { history ->
                            return@setParentGroups lastGroupBlock.parentBlock
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

            is SqlInlineGroupBlock -> {
                // case-end
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }

            is SqlInlineSecondGroupBlock -> {
                if (childBlock.isEndCase) {
                    val inlineIndex =
                        blockBuilder.getGroupTopNodeIndexByIndentType(IndentType.INLINE)
                    if (inlineIndex >= 0) {
                        setParentGroups(
                            childBlock,
                        ) { history ->
                            return@setParentGroups history[inlineIndex].second
                        }
                        blockBuilder.clearSubListGroupTopNodeIndexHistory(inlineIndex)
                    }
                    return
                }
                if (lastIndentLevel == IndentType.INLINE_SECOND) {
                    blockBuilder.removeLastGroupTopNodeIndexHistory()
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups lastGroupBlock.parentBlock
                    }
                    return
                }
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }

            is SqlWordBlock, is SqlOtherBlock, is SqlLineCommentBlock, is SqlBlockCommentBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }

            is SqlSubQueryGroupBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }

            is SqlRightPatternBlock -> {
                val leftIndex = blockBuilder.getGroupTopNodeIndexByIndentType(IndentType.SUB)
                if (leftIndex >= 0) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history[leftIndex].second
                    }
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(leftIndex)
                }
                pendingCommentBlocks.clear()
            }

            is SqlElSymbolBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }

            is SqlColumnDefinitionRawGroupBlock -> {
                if (lastGroupBlock is SqlColumnDefinitionRawGroupBlock) {
                    blockBuilder.removeLastGroupTopNodeIndexHistory()
                }
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
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
        val parentGroup = getParentGroup(blockBuilder.getGroupTopNodeIndexHistory() as MutableList<Pair<Int, SqlBlock>>)
        childBlock.setParentGroupBlock(parentGroup)

        if ((
                childBlock is SqlNewGroupBlock &&
                    childBlock.indent.indentLevel.isNewLineGroup() &&
                    !SqlKeywordUtil.isSetLineKeyword(
                        childBlock.node.text,
                        parentGroup?.node?.text ?: "",
                    )
            ) ||
            childBlock is SqlSubGroupBlock ||
            childBlock is SqlInlineGroupBlock ||
            childBlock is SqlInlineSecondGroupBlock ||
            childBlock is SqlColumnDefinitionRawGroupBlock
        ) {
            blockBuilder.addGroupTopNodeIndexHistory(Pair(blocks.size - 1, childBlock))
        }
    }

    open fun getBlock(child: ASTNode): SqlBlock {
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
        return when (child.elementType) {
            SqlTypes.KEYWORD -> {
                return getKeywordBlock(child)
            }

            SqlTypes.LEFT_PAREN -> {
                if (lastGroup is SqlCreateKeywordGroupBlock && lastGroup.isCreateTable) {
                    SqlColumnDefinitionGroupBlock(child, wrap, alignment, spacingBuilder)
                } else if (lastGroup is SqlColumnDefinitionRawGroupBlock) {
                    SqlDataTypeParamBlock(child, wrap, alignment, spacingBuilder)
                } else {
                    SqlSubQueryGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            SqlTypes.OTHER ->
                return SqlOtherBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.DOT -> return SqlElDotBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.RIGHT_PAREN ->
                return SqlRightPatternBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.COMMA -> {
                return when (lastGroup) {
                    is SqlColumnDefinitionGroupBlock, is SqlColumnDefinitionRawGroupBlock ->
                        SqlColumnDefinitionRawGroupBlock(
                            child,
                            wrap,
                            alignment,
                            spacingBuilder,
                        )

                    is SqlColumnGroupBlock -> SqlColumnGroupBlock(child, wrap, alignment, spacingBuilder)

                    else -> SqlColumnGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            SqlTypes.WORD -> {
                when (lastGroup) {
                    is SqlKeywordGroupBlock -> {
                        when {
                            SqlKeywordUtil.isBeforeTableKeyword(lastGroup.node.text) ->
                                SqlTableBlock(
                                    child,
                                    wrap,
                                    alignment,
                                    spacingBuilder,
                                )

                            else -> SqlWordBlock(child, wrap, alignment, spacingBuilder)
                        }
                    }

                    is SqlColumnDefinitionGroupBlock -> {
                        SqlColumnDefinitionRawGroupBlock(
                            child,
                            wrap,
                            alignment,
                            spacingBuilder,
                        )
                    }

                    else -> SqlWordBlock(child, wrap, alignment, spacingBuilder)
                }
            }

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

    private fun getKeywordBlock(child: ASTNode): SqlBlock {
        // Because we haven't yet set the parent-child relationship of the block,
        // the parent group references groupTopNodeIndexHistory.
        val indentLevel = SqlKeywordUtil.getIndentType(child.text)
        val lastGroupBlock = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
        if (indentLevel.isNewLineGroup()) {
            if (child.text.lowercase() == "create") {
                return SqlCreateKeywordGroupBlock(child, wrap, alignment, spacingBuilder)
            }
            if (indentLevel == IndentType.JOIN) {
                return if (SqlKeywordUtil.isJoinKeyword(child.text)) {
                    SqlJoinGroupBlock(child, wrap, alignment, spacingBuilder)
                } else if (lastGroupBlock is SqlJoinGroupBlock) {
                    SqlKeywordBlock(child, IndentType.ATTACHED, wrap, alignment, spacingBuilder)
                } else {
                    SqlJoinGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }
            if (indentLevel == IndentType.INLINE_SECOND) {
                return SqlInlineSecondGroupBlock(child, wrap, alignment, spacingBuilder)
            }

            return SqlKeywordGroupBlock(child, indentLevel, wrap, alignment, spacingBuilder)
        }

        if (lastGroupBlock is SqlCreateKeywordGroupBlock) {
            lastGroupBlock.setCreateTableGroup(child.text)
            return SqlKeywordBlock(child, indentLevel, wrap, alignment, spacingBuilder)
        }

        if (indentLevel == IndentType.INLINE) {
            return SqlInlineGroupBlock(child, wrap, alignment, spacingBuilder)
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
        if (child2 is SqlNewGroupBlock) {
            when (child1) {
                is SqlWhitespaceBlock ->
                    SqlCustomSpacingBuilder()
                        .getSpacingWithWhiteSpace(
                            child1,
                            child2,
                        )?.let { return it }

                else ->
                    SqlCustomSpacingBuilder()
                        .getSpacingWithIndentLevel(child2)
                        ?.let { return it }
            }
        }

        if (child2 is SqlColumnDefinitionRawGroupBlock) {
            SqlCustomSpacingBuilder().getSpacingColumnDefinitionRaw(child2)?.let { return it }
        }

        if (child2 is SqlRightPatternBlock && child2.parentBlock is SqlColumnDefinitionGroupBlock) {
            SqlCustomSpacingBuilder().getSpacingColumnDefinitionRawEndRight(child2)?.let { return it }
        }

        if (child1 is SqlBlock && (child2 is SqlCommaBlock || child2 is SqlColumnGroupBlock)) {
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
                        is SqlKeywordGroupBlock -> Indent.getSpaceIndent(it.indent.indentLen)
                        else -> childIndent ?: Indent.getNoneIndent()
                    }
                return ChildAttributes(indent, null)
            }
        return ChildAttributes(Indent.getNoneIndent(), null)
    }

    override fun getChildIndent(): Indent? = Indent.getSpaceIndent(4)

    override fun isLeaf(): Boolean = myNode.firstChildNode == null

    data class ElementIndent(
        var indentLevel: IndentType,
        var indentLen: Int,
        var groupIndentLen: Int,
    )
}
