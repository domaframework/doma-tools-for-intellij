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
import org.domaframework.doma.intellij.formatter.SqlBlockUtil
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.SqlKeywordUtil
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElSymbolBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlViewGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val customSpacingBuilder: SqlCustomSpacingBuilder?,
    internal val spacingBuilder: SpacingBuilder,
    private val enableFormat: Boolean = false,
) : AbstractBlock(
        node,
        wrap,
        alignment,
    ) {
    data class ElementIndent(
        var indentLevel: IndentType,
        var indentLen: Int,
        var groupIndentLen: Int,
    )

    val blocks = mutableListOf<AbstractBlock>()
    open var parentBlock: SqlBlock? = null
    open val childBlocks = mutableListOf<SqlBlock>()
    open val indent: ElementIndent =
        ElementIndent(
            IndentType.FILE,
            0,
            0,
        )

    private val blockBuilder = SqlBlockBuilder()
    protected val blockUtil = SqlBlockUtil(this)

    protected open val pendingCommentBlocks = mutableListOf<SqlBlock>()

    protected fun isEnableFormat(): Boolean = enableFormat

    open fun setParentGroupBlock(block: SqlBlock?) {
        parentBlock = block
        parentBlock?.addChildBlock(this)
    }

    open fun addChildBlock(childBlock: SqlBlock) {
        childBlocks.add(childBlock)
    }

    fun getNodeText() = node.text.lowercase()

    public override fun buildChildren(): MutableList<AbstractBlock> {
        if (isLeaf || !isEnableFormat()) return mutableListOf()

        var child = node.firstChildNode
        var prevNonWhiteSpaceNode: ASTNode? = null
        blockBuilder.addGroupTopNodeIndexHistory(Pair(0, this))
        while (child != null) {
            val lastBlock = blocks.lastOrNull()
            val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
            if (child !is PsiWhiteSpace) {
                val childBlock = getBlock(child)
                if (blocks.isNotEmpty() && lastBlock is SqlWhitespaceBlock) {
                    if (isSaveWhiteSpace(childBlock, child, lastGroup)) {
                        val whiteBlock = lastBlock as SqlBlock
                        whiteBlock.parentBlock = lastGroup
                    } else {
                        // Ignore space blocks for non-breaking elements
                        blocks.removeLast()
                    }
                }
                prevNonWhiteSpaceNode = child
                if (childBlock is SqlCommentBlock) {
                    when (childBlock) {
                        is SqlElConditionLoopCommentBlock ->
                            blockBuilder.addConditionOrLoopBlock(
                                childBlock,
                            )

                        else -> blockBuilder.addCommentBlock(childBlock)
                    }
                }
                updateSearchKeywordLevelHistory(childBlock, child)
                blocks.add(childBlock)
            } else {
                if (lastBlock !is SqlLineCommentBlock) {
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
        isNewLineGroupBlock(childBlock, child, lastGroup) ||
            childBlock is SqlInsertColumnGroupBlock ||
            childBlock is SqlColumnDefinitionRawGroupBlock ||
            childBlock is SqlColumnDefinitionGroupBlock ||
            (childBlock is SqlOtherBlock && childBlock.isUpdateColumnSubstitutions) ||
            (childBlock is SqlRightPatternBlock && childBlock.isNewLine(lastGroup)) ||
            (
                (
                    childBlock is SqlLineCommentBlock ||
                        childBlock is SqlBlockCommentBlock
                ) &&
                    child.treePrev.text.contains("\n")
            ) ||
            (childBlock is SqlElConditionLoopCommentBlock)

    private fun isNewGroup(childBlock: SqlBlock): Boolean {
        val isNewGroupType = childBlock.indent.indentLevel.isNewLineGroup()
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
        val lastKeywordText =
            if (lastGroup?.indent?.indentLevel == IndentType.JOIN) {
                lastGroup.getNodeText()
            } else {
                getLastGroupKeywordText(lastGroup)
            }

        val isSetLineGroup =
            SqlKeywordUtil.isSetLineKeyword(
                childBlock.getNodeText(),
                lastKeywordText,
            )

        return isNewGroupType && !isSetLineGroup
    }

    private fun isNewLineGroupBlock(
        childBlock: SqlBlock,
        child: ASTNode,
        lastGroup: SqlBlock?,
    ): Boolean {
        if (childBlock is SqlCommaBlock &&
            (
                lastGroup is SqlParallelListBlock ||
                    lastGroup?.parentBlock is SqlParallelListBlock
            )
        ) {
            return false
        }

        val isNewGroupType = childBlock.indent.indentLevel.isNewLineGroup()
        val lastKeywordText =
            if (lastGroup?.indent?.indentLevel == IndentType.JOIN) {
                lastGroup.getNodeText()
            } else {
                getLastGroupKeywordText(lastGroup)
            }

        val isSetLineGroup =
            SqlKeywordUtil.isSetLineKeyword(
                child.text,
                lastKeywordText,
            )
        if (isNewGroupType && !isSetLineGroup) {
            if (lastGroup is SqlSubQueryGroupBlock) {
                return (lastGroup.childBlocks.size > 1)
            }
            return true
        }
        return false
    }

    /**
     * Searches for a keyword element in the most recent group block and returns its text.
     * If not found, returns the text of the group block itself.
     */
    private fun getLastGroupKeywordText(lastGroup: SqlBlock?): String =
        lastGroup
            ?.childBlocks
            ?.lastOrNull { it.node.elementType == SqlTypes.KEYWORD }
            ?.node
            ?.text ?: lastGroup?.getNodeText() ?: ""

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
                    // The AND following an OR will be a child of OR unless surrounded by a subgroup
                    if (childBlock.getNodeText() == "and" && lastGroupBlock.getNodeText() == "or") {
                        setParentGroups(
                            childBlock,
                        ) { history ->
                            return@setParentGroups lastGroupBlock
                        }
                    } else {
                        if (childBlock.getNodeText() == "or" &&
                            lastGroupBlock.getNodeText() == "and" &&
                            lastGroupBlock.parentBlock?.getNodeText() == "or"
                        ) {
                            val orParentIndex =
                                blockBuilder.getGroupTopNodeIndex { block ->
                                    block is SqlKeywordGroupBlock && block.getNodeText() == "or"
                                }
                            blockBuilder.clearSubListGroupTopNodeIndexHistory(orParentIndex)
                            setParentGroups(
                                childBlock,
                            ) { history ->
                                return@setParentGroups history.lastOrNull()?.second
                            }
                        } else {
                            blockBuilder.removeLastGroupTopNodeIndexHistory()
                            setParentGroups(
                                childBlock,
                            ) { history ->
                                return@setParentGroups lastGroupBlock.parentBlock
                            }
                        }
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
                        blockBuilder.getGroupTopNodeIndex { block ->
                            block.indent.indentLevel == IndentType.INLINE
                        }
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

            is SqlColumnBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    val parentGroupBlock = history.last().second
                    if (parentGroupBlock is SqlColumnDefinitionRawGroupBlock &&
                        parentGroupBlock.columnName != ","
                    ) {
                        parentGroupBlock.columnName = childBlock.getNodeText()
                        val columnDefinition =
                            parentGroupBlock.parentBlock as? SqlColumnDefinitionGroupBlock
                        if (columnDefinition != null && columnDefinition.alignmentColumnName.length < parentGroupBlock.columnName.length) {
                            columnDefinition.alignmentColumnName = parentGroupBlock.columnName
                        }
                    }
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

            is SqlElConditionLoopCommentBlock -> {
                if (lastGroupBlock is SqlCommaBlock || lastGroupBlock is SqlElConditionLoopCommentBlock) {
                    blockBuilder.removeLastGroupTopNodeIndexHistory()
                }
                setParentGroups(
                    childBlock,
                ) { history ->
                    if (childBlock.conditionType.isEnd()) {
                        val lastConditionLoopCommentBlock = blockBuilder.getConditionOrLoopBlocksLast()
                        blockBuilder.removeConditionOrLoopBlockLast()
                        return@setParentGroups lastConditionLoopCommentBlock
                    }
                    return@setParentGroups null
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
                val paramIndex =
                    blockBuilder.getGroupTopNodeIndex { block ->
                        block.indent.indentLevel == IndentType.PARAM
                    }
                if (paramIndex >= 0) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history[paramIndex].second
                    }
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(paramIndex)
                    return
                }

                val leftIndex =
                    blockBuilder.getGroupTopNodeIndex { block ->
                        block.indent.indentLevel == IndentType.SUB
                    }
                if (leftIndex >= 0) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history[leftIndex].second
                    }
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(leftIndex)
                    return
                }
            }

            is SqlElSymbolBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }

            is SqlDataTypeBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    return@setParentGroups history.last().second
                }
            }

            is SqlCommaBlock -> {
                if (lastGroupBlock is SqlCommaBlock) {
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
        val parentGroup =
            getParentGroup(blockBuilder.getGroupTopNodeIndexHistory() as MutableList<Pair<Int, SqlBlock>>)

        // // The parent block for SqlElConditionLoopCommentBlock will be set later
        if (childBlock !is SqlElConditionLoopCommentBlock ||
            childBlock.conditionType.isEnd()
        ) {
            childBlock.setParentGroupBlock(parentGroup)
        }

        if (isNewGroup(childBlock) ||
            (childBlock is SqlSubGroupBlock) ||
            childBlock is SqlViewGroupBlock ||
            childBlock is SqlInlineGroupBlock ||
            childBlock is SqlInlineSecondGroupBlock ||
            childBlock is SqlColumnDefinitionRawGroupBlock
        ) {
            blockBuilder.addGroupTopNodeIndexHistory(Pair(blocks.size - 1, childBlock))
            // Set parent-child relationship and indent for preceding comment at beginning of block group
            blockBuilder.updateCommentBlockIndent(childBlock)
        }
    }

    open fun createBlockIndentLen(): Int = 0

    open fun getBlock(child: ASTNode): SqlBlock {
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
        return when (child.elementType) {
            SqlTypes.KEYWORD -> {
                return blockUtil.getKeywordBlock(
                    child,
                    blockBuilder.getLastGroupTopNodeIndexHistory()?.second,
                )
            }

            SqlTypes.DATATYPE -> SqlDataTypeBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.LEFT_PAREN -> {
                return blockUtil.getSubGroupBlock(lastGroup, child)
            }

            SqlTypes.OTHER -> return SqlOtherBlock(
                child,
                wrap,
                alignment,
                spacingBuilder,
                blockBuilder.getLastGroup(),
            )

            SqlTypes.RIGHT_PAREN -> return SqlRightPatternBlock(
                child,
                wrap,
                alignment,
                spacingBuilder,
            )

            SqlTypes.COMMA -> {
                return blockUtil.getCommaGroupBlock(lastGroup, child)
            }

            SqlTypes.WORD -> return blockUtil.getWordBlock(lastGroup, child)

            SqlTypes.BLOCK_COMMENT -> {
                return blockUtil.getBlockCommentBlock(child, createBlockCommentSpacingBuilder())
            }

            SqlTypes.LINE_COMMENT ->
                return SqlLineCommentBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.PLUS, SqlTypes.MINUS, SqlTypes.ASTERISK, SqlTypes.SLASH ->
                return SqlElSymbolBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.LE, SqlTypes.LT, SqlTypes.EL_EQ, SqlTypes.EL_NE, SqlTypes.GE, SqlTypes.GT ->
                return SqlElSymbolBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.STRING, SqlTypes.NUMBER, SqlTypes.BOOLEAN ->
                return SqlLiteralBlock(child, wrap, alignment, spacingBuilder)

            else -> SqlUnknownBlock(child, wrap, alignment, spacingBuilder)
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
        if (!isEnableFormat()) return null

        // The end of a line comment element is a newline, so just add a space for the indent.
        if (child1 is SqlLineCommentBlock && child2 is SqlBlock) {
            return SqlCustomSpacingBuilder().getSpacing(child2)
        }

        // Do not leave a space after the comment block of the bind variable
        if (child1 is SqlElBlockCommentBlock && child1 !is SqlElConditionLoopCommentBlock && child2 !is SqlCommentBlock) {
            return SqlCustomSpacingBuilder.nonSpacing
        }

        if (child2 is SqlElBlockCommentBlock) {
            return when (child1) {
                is SqlElBlockCommentBlock -> {
                    SqlCustomSpacingBuilder().getSpacing(child2)
                }

                is SqlWhitespaceBlock -> {
                    SqlCustomSpacingBuilder().getSpacing(child2)
                }

                else -> SqlCustomSpacingBuilder.normalSpacing
            }
        }

        if (child1 is SqlFunctionParamBlock) {
            return SqlCustomSpacingBuilder.nonSpacing
        }

        if (child2 is SqlOtherBlock) {
            return SqlCustomSpacingBuilder().getSpacing(child2)
        }

        if (child1 is SqlWhitespaceBlock) {
            when (child2) {
                is SqlBlockCommentBlock, is SqlLineCommentBlock -> {
                    return SqlCustomSpacingBuilder().getSpacing(child2)
                }

                is SqlNewGroupBlock -> {
                    return SqlCustomSpacingBuilder().getSpacing(child2)
                }
            }
        }

        if (child2 is SqlNewGroupBlock) {
            when (child2) {
                is SqlSubQueryGroupBlock -> {
                    if (child1 is SqlNewGroupBlock) {
                        return SqlCustomSpacingBuilder.normalSpacing
                    }
                }

                is SqlDataTypeParamBlock, is SqlFunctionParamBlock -> return SqlCustomSpacingBuilder.nonSpacing

                else -> return SqlCustomSpacingBuilder.normalSpacing
            }
        }

        if (child2 is SqlColumnDefinitionRawGroupBlock) {
            SqlCustomSpacingBuilder().getSpacingColumnDefinitionRaw(child2)?.let { return it }
        }

        if (child2 is SqlRightPatternBlock) {
            return SqlCustomSpacingBuilder().getSpacingRightPattern(child2)
        }

        if (child1 is SqlBlock && (child2 is SqlCommaBlock || child2 is SqlColumnGroupBlock)) {
            SqlCustomSpacingBuilder().getSpacingWithIndentComma(child1, child2)?.let { return it }
        }

        if (child2 is SqlDataTypeParamBlock) {
            return SqlCustomSpacingBuilder.nonSpacing
        }

        if (child2 is SqlColumnBlock) {
            SqlCustomSpacingBuilder().getSpacingColumnDefinition(child2)?.let { return it }
        }

        val spacing: Spacing? = customSpacingBuilder?.getCustomSpacing(child1, child2)
        return spacing ?: spacingBuilder.getSpacing(this, child1, child2)
    }

    override fun getChildAttributes(newChildIndex: Int): ChildAttributes {
        if (!isEnableFormat()) return ChildAttributes(Indent.getNoneIndent(), null)

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

    override fun getChildIndent(): Indent? =
        if (isEnableFormat()) {
            Indent.getSpaceIndent(4)
        } else {
            Indent.getSpaceIndent(0)
        }

    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
