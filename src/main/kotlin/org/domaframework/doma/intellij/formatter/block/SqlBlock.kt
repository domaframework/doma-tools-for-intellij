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
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.formatter.CreateQueryType
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.SqlBlockBuilder
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.SqlKeywordUtil
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElSymbolBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInsertKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlUpdateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlViewGroupBlock
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
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

    protected open val pendingCommentBlocks = mutableListOf<SqlBlock>()

    protected fun isEnableFormat(): Boolean = enableFormat

    open fun setParentGroupBlock(block: SqlBlock?) {
        parentBlock = block
        parentBlock?.addChildBlock(this)
    }

    open fun addChildBlock(childBlock: SqlBlock) {
        childBlocks.add(childBlock)
    }

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
                updateSearchKeywordLevelHistory(childBlock, child)
                setRightSpace(childBlock)
                blocks.add(childBlock)
                if (childBlock is SqlCommentBlock) {
                    blockBuilder.addCommentBlock(childBlock)
                }
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
            childBlock.node.elementType == SqlTypes.COMMA ||
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

    private fun setRightSpace(currentBlock: SqlBlock?) {
        val rightBlock = currentBlock as? SqlRightPatternBlock
        rightBlock?.enableLastRight()
    }

    private fun isNewGroup(childBlock: SqlBlock): Boolean {
        val isNewGroupType = childBlock.indent.indentLevel.isNewLineGroup()
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
        val lastKeywordText =
            if (lastGroup?.indent?.indentLevel == IndentType.JOIN) {
                lastGroup.node.text
            } else {
                lastGroup
                    ?.childBlocks
                    ?.lastOrNull { it.node.elementType == SqlTypes.KEYWORD }
                    ?.node
                    ?.text ?: lastGroup?.node?.text ?: ""
            }

        val isSetLineGroup =
            SqlKeywordUtil.isSetLineKeyword(
                childBlock.node.text,
                lastKeywordText,
            )

        return isNewGroupType && !isSetLineGroup
    }

    private fun isNewLineGroupBlock(
        childBlock: SqlBlock,
        child: ASTNode,
        lastGroup: SqlBlock?,
    ): Boolean {
        val isNewGroupType = childBlock.indent.indentLevel.isNewLineGroup()
        val lastKeywordText =
            if (lastGroup?.indent?.indentLevel == IndentType.JOIN) {
                lastGroup.node.text
            } else {
                lastGroup
                    ?.childBlocks
                    ?.lastOrNull { it.node.elementType == SqlTypes.KEYWORD }
                    ?.node
                    ?.text ?: lastGroup?.node?.text ?: ""
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

            is SqlColumnBlock -> {
                setParentGroups(
                    childBlock,
                ) { history ->
                    val parentGroupBlock = history.last().second
                    if (parentGroupBlock is SqlColumnDefinitionRawGroupBlock &&
                        parentGroupBlock.columnName != ","
                    ) {
                        parentGroupBlock.columnName = childBlock.node.text
                        val columnDefinition = parentGroupBlock.parentBlock as? SqlColumnDefinitionGroupBlock
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
                val paramIndex = blockBuilder.getGroupTopNodeIndexByIndentType(IndentType.PARAM)
                if (paramIndex >= 0) {
                    setParentGroups(
                        childBlock,
                    ) { history ->
                        return@setParentGroups history[paramIndex].second
                    }
                    blockBuilder.clearSubListGroupTopNodeIndexHistory(paramIndex)
                    return
                }

                val leftIndex = blockBuilder.getGroupTopNodeIndexByIndentType(IndentType.SUB)
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
        childBlock.setParentGroupBlock(parentGroup)
        if (isNewGroup(childBlock) ||
            childBlock is SqlSubGroupBlock ||
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
                return getKeywordBlock(child)
            }

            SqlTypes.DATATYPE -> SqlDataTypeBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.LEFT_PAREN -> {
                return getSubGroupBlock(lastGroup, child)
            }

            SqlTypes.OTHER -> return SqlOtherBlock(child, wrap, alignment, spacingBuilder, blockBuilder.getLastGroup())
            SqlTypes.RIGHT_PAREN -> return SqlRightPatternBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.COMMA -> {
                return when (lastGroup) {
                    is SqlColumnDefinitionGroupBlock, is SqlColumnDefinitionRawGroupBlock ->
                        SqlColumnDefinitionRawGroupBlock(
                            child,
                            wrap,
                            alignment,
                            spacingBuilder,
                        )

                    is SqlColumnGroupBlock, is SqlKeywordGroupBlock -> {
                        if (lastGroup.indent.indentLevel == IndentType.SECOND) {
                            SqlCommaBlock(child, wrap, alignment, spacingBuilder)
                        } else {
                            SqlColumnGroupBlock(child, wrap, alignment, spacingBuilder)
                        }
                    }

                    else -> SqlCommaBlock(child, wrap, alignment, spacingBuilder)
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
                        lastGroup.alignmentColumnName = child.text
                        SqlColumnDefinitionRawGroupBlock(
                            child,
                            wrap,
                            alignment,
                            spacingBuilder,
                        )
                    }

                    is SqlColumnDefinitionRawGroupBlock -> {
                        if (lastGroup.childBlocks.isEmpty()) {
                            lastGroup.columnName = child.text
                            SqlColumnBlock(
                                child,
                                wrap,
                                alignment,
                                spacingBuilder,
                            )
                        } else {
                            SqlWordBlock(child, wrap, alignment, spacingBuilder)
                        }
                    }

                    else -> SqlWordBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            SqlTypes.BLOCK_COMMENT -> {
                if (PsiTreeUtil.getChildOfType(child.psi, PsiComment::class.java) != null) {
                    return SqlBlockCommentBlock(
                        child,
                        wrap,
                        alignment,
                        spacingBuilder,
                    )
                }
                if (child.psi is SqlCustomElCommentExpr &&
                    (child.psi as SqlCustomElCommentExpr).isConditionOrLoopDirective()
                ) {
                    return SqlElConditionLoopCommentBlock(
                        child,
                        wrap,
                        alignment,
                        createBlockCommentSpacingBuilder(),
                        spacingBuilder,
                    )
                }
                return SqlElBlockCommentBlock(
                    child,
                    wrap,
                    alignment,
                    createBlockCommentSpacingBuilder(),
                    spacingBuilder,
                )
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

    private fun getSubGroupBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
    ): SqlBlock {
        if (child.treePrev.elementType == SqlTypes.WORD) {
            return SqlFunctionParamBlock(child, wrap, alignment, spacingBuilder)
        }

        when (lastGroup) {
            is SqlCreateKeywordGroupBlock -> {
                return if (lastGroup.createType == CreateQueryType.TABLE) {
                    SqlColumnDefinitionGroupBlock(
                        child,
                        wrap,
                        alignment,
                        spacingBuilder,
                    )
                } else {
                    SqlSubQueryGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            is SqlColumnDefinitionRawGroupBlock ->
                return SqlDataTypeParamBlock(child, wrap, alignment, spacingBuilder)

            is SqlInsertKeywordGroupBlock ->
                return SqlInsertColumnGroupBlock(child, wrap, alignment, spacingBuilder)

            is SqlUpdateKeywordGroupBlock -> {
                return if (lastGroup.childBlocks.firstOrNull { it is SqlUpdateColumnGroupBlock } == null) {
                    SqlUpdateColumnGroupBlock(child, wrap, alignment, spacingBuilder)
                } else if (lastGroup.childBlocks.lastOrNull { it is SqlUpdateColumnGroupBlock } != null) {
                    SqlUpdateValueGroupBlock(child, wrap, alignment, spacingBuilder)
                } else {
                    SqlSubQueryGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            else ->
                return SqlSubQueryGroupBlock(child, wrap, alignment, spacingBuilder)
        }
    }

    private fun getKeywordBlock(child: ASTNode): SqlBlock {
        // Because we haven't yet set the parent-child relationship of the block,
        // the parent group references groupTopNodeIndexHistory.
        val indentLevel = SqlKeywordUtil.getIndentType(child.text)
        val lastGroupBlock = blockBuilder.getLastGroupTopNodeIndexHistory()?.second
        val keywordText = child.text.lowercase()
        if (indentLevel.isNewLineGroup()) {
            when (indentLevel) {
                IndentType.JOIN -> {
                    return if (SqlKeywordUtil.isJoinKeyword(child.text)) {
                        SqlJoinGroupBlock(child, wrap, alignment, spacingBuilder)
                    } else if (lastGroupBlock is SqlJoinGroupBlock) {
                        SqlKeywordBlock(child, IndentType.ATTACHED, wrap, alignment, spacingBuilder)
                    } else {
                        SqlJoinGroupBlock(child, wrap, alignment, spacingBuilder)
                    }
                }

                IndentType.INLINE_SECOND -> {
                    return SqlInlineSecondGroupBlock(child, wrap, alignment, spacingBuilder)
                }

                IndentType.TOP -> {
                    if (keywordText == "create") {
                        return SqlCreateKeywordGroupBlock(child, wrap, alignment, spacingBuilder)
                    }
                    if (keywordText == "insert") {
                        return SqlInsertKeywordGroupBlock(child, wrap, alignment, spacingBuilder)
                    }

                    return SqlKeywordGroupBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                }

                IndentType.SECOND -> {
                    return if (keywordText == "set") {
                        SqlUpdateKeywordGroupBlock(child, wrap, alignment, spacingBuilder)
                    } else {
                        SqlKeywordGroupBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                    }
                }

                else -> {
                    return SqlKeywordGroupBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                }
            }
        }

        when (indentLevel) {
            IndentType.INLINE -> {
                if (!SqlKeywordUtil.isSetLineKeyword(
                        child.text,
                        lastGroupBlock?.node?.text ?: "",
                    )
                ) {
                    return SqlInlineGroupBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            IndentType.ATTACHED -> {
                if (lastGroupBlock is SqlCreateKeywordGroupBlock) {
                    lastGroupBlock.setCreateQueryType(child.text)
                    return SqlKeywordBlock(child, indentLevel, wrap, alignment, spacingBuilder)
                }
            }

            IndentType.OPTIONS -> {
                if (child.text.lowercase() == "as") {
                    val parentCreateBlock =
                        lastGroupBlock as? SqlCreateKeywordGroupBlock
                            ?: lastGroupBlock?.parentBlock as? SqlCreateKeywordGroupBlock
                    if (parentCreateBlock != null && parentCreateBlock.createType == CreateQueryType.VIEW) {
                        return SqlViewGroupBlock(child, wrap, alignment, spacingBuilder)
                    }
                }
            }

            else -> return SqlKeywordBlock(child, indentLevel, wrap, alignment, spacingBuilder)
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
        if (!isEnableFormat()) return null
        // The end of a line comment element is a newline, so just add a space for the indent.
        if (child1 is SqlLineCommentBlock) {
            if (child2 is SqlBlock) {
                return Spacing.createSpacing(child2.indent.indentLen, child2.indent.indentLen, 0, false, 0)
            }
        }

        // Do not leave a space after the comment block of the bind variable
        if (child1 is SqlElBlockCommentBlock && child2 !is SqlCommentBlock) {
            return Spacing.createSpacing(0, 0, 0, false, 0)
        }

        if (child2 is SqlElBlockCommentBlock) {
            when (child1) {
                is SqlElBlockCommentBlock -> {
                    val indentLen = child2.indent.indentLen
                    return Spacing.createSpacing(indentLen, indentLen, 1, false, 0)
                }

                is SqlWhitespaceBlock -> {
                    val indentLen = child2.indent.indentLen
                    return Spacing.createSpacing(indentLen, indentLen, 0, false, 0)
                }

                else -> return SqlCustomSpacingBuilder.normalSpacing
            }
        }

        if (child1 is SqlFunctionParamBlock) {
            return SqlCustomSpacingBuilder.nonSpacing
        }

        if (child2 is SqlOtherBlock) {
            val indentLen = child2.indent.indentLen
            return Spacing.createSpacing(indentLen, indentLen, 0, false, 0)
        }

        if (child1 is SqlWhitespaceBlock) {
            when (child2) {
                is SqlBlockCommentBlock, is SqlLineCommentBlock -> {
                    val indentLen = child2.indent.indentLen
                    return Spacing.createSpacing(indentLen, indentLen, 0, false, 0)
                }

                is SqlNewGroupBlock -> {
                    return SqlCustomSpacingBuilder()
                        .getSpacing(
                            child2,
                        )?.let { return it }
                }
            }
        }

        if (child2 is SqlNewGroupBlock) {
            when (child2) {
                is SqlSubQueryGroupBlock -> {
                    if (child1 is SqlNewGroupBlock) {
                        return SqlCustomSpacingBuilder.normalSpacing
                    } else {
                        // Remove spaces for parameter subgroups such as functions
                        SqlCustomSpacingBuilder.nonSpacing
                    }
                }

                else -> {
                    SqlCustomSpacingBuilder.normalSpacing
                }
            }
        }

        if (child2 is SqlColumnDefinitionRawGroupBlock) {
            SqlCustomSpacingBuilder().getSpacingColumnDefinitionRaw(child2)?.let { return it }
        }

        if (child2 is SqlRightPatternBlock) {
            return when {
                child2.parentBlock is SqlColumnDefinitionGroupBlock ||
                    child2.parentBlock is SqlUpdateColumnGroupBlock ||
                    child2.parentBlock is SqlUpdateValueGroupBlock -> {
                    val indentLen = child2.indent.indentLen
                    return Spacing.createSpacing(indentLen, indentLen, 0, false, 0, 0)
                }

                child2.parentBlock is SqlDataTypeParamBlock -> SqlCustomSpacingBuilder.nonSpacing

                child2.preSpaceRight -> SqlCustomSpacingBuilder.normalSpacing
                else -> SqlCustomSpacingBuilder.nonSpacing
            }
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
        if (!isEnableFormat()) {
            Indent.getSpaceIndent(4)
        } else {
            Indent.getSpaceIndent(0)
        }

    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
