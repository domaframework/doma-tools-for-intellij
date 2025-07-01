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
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.util.TypeUtil.isExpectedClassType
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElSymbolBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlDataTypeBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateColumnAssignmentSymbolBlock
import org.domaframework.doma.intellij.formatter.builder.SqlBlockBuilder
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.processor.SqlSetParentGroupProcessor
import org.domaframework.doma.intellij.formatter.util.CreateTableUtil
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlBlockUtil
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val customSpacingBuilder: SqlCustomSpacingBuilder?,
    internal val spacingBuilder: SpacingBuilder,
    private val enableFormat: Boolean,
    private val formatMode: FormattingMode,
) : AbstractBlock(
        node,
        wrap,
        alignment,
    ) {
    data class ElementIndent(
        var indentLevel: IndentType,
        /**
         * The number of indentation spaces for this element.
         * Returns `0` if there is no line break.
         */
        var indentLen: Int,
        /**
         * Indentation baseline applied to the group itself.
         * Even if the group does not start on a new line,
         * it determines and applies indentation to the group based on factors such as the number of preceding characters.
         */
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
    private val parentSetProcessor = SqlSetParentGroupProcessor(blockBuilder)
    protected val blockUtil = SqlBlockUtil(this, isEnableFormat(), formatMode)

    protected open val pendingCommentBlocks = mutableListOf<SqlBlock>()

    fun isEnableFormat(): Boolean = enableFormat

    open fun setParentGroupBlock(lastGroup: SqlBlock?) {
        parentBlock = lastGroup
        parentBlock?.addChildBlock(this)
    }

    open val isNeedWhiteSpace: Boolean = true

    open fun addChildBlock(childBlock: SqlBlock) {
        childBlocks.add(childBlock)
    }

    fun getNodeText() = node.text.lowercase()

    public override fun buildChildren(): MutableList<AbstractBlock> {
        if (isLeaf) return mutableListOf()

        var child = node.firstChildNode
        var prevNonWhiteSpaceNode: ASTNode? = null
        blockBuilder.addGroupTopNodeIndexHistory(this)
        while (child != null) {
            val lastBlock = blocks.lastOrNull()
            val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()
            if (child !is PsiWhiteSpace) {
                val childBlock = getBlock(child)
                updateWhiteSpaceInclude(lastBlock, childBlock, lastGroup)
                prevNonWhiteSpaceNode = child
                updateCommentParentAndIdent(childBlock)
                updateBlockParentAndLAddGroup(childBlock)
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

    /**
     * Sets the parent and indentation for the comment element based on the element that was registered earlier.
     */
    private fun updateCommentParentAndIdent(commentBlock: SqlBlock) {
        if (commentBlock !is SqlCommentBlock) return
        if (commentBlock is SqlElConditionLoopCommentBlock) {
            blockBuilder.addConditionOrLoopBlock(
                commentBlock,
            )
        } else {
            blockBuilder.addCommentBlock(commentBlock)
        }
    }

    /**
     * Determines whether to retain the preceding newline (space) as a formatting target block based on the currently checked element.
     */
    private fun updateWhiteSpaceInclude(
        lastBlock: AbstractBlock?,
        childBlock: SqlBlock,
        lastGroup: SqlBlock?,
    ) {
        if (blocks.isNotEmpty() && lastBlock is SqlWhitespaceBlock) {
            if (isSaveWhiteSpace(childBlock, lastGroup)) {
                val whiteBlock = lastBlock as SqlBlock
                whiteBlock.parentBlock = lastGroup
            } else {
                // Ignore space blocks for non-breaking elements
                blocks.removeLast()
            }
        }
    }

    /**
     * Determines whether to retain the space (newline) based on the last registered group or the class of the currently checked element.
     */
    private fun isSaveWhiteSpace(
        childBlock: SqlBlock,
        lastGroup: SqlBlock?,
    ): Boolean {
        val child = childBlock.node

        if (!childBlock.isNeedWhiteSpace) return false

        val expectedClassTypes =
            listOf(
                SqlElConditionLoopCommentBlock::class,
                SqlInsertColumnGroupBlock::class,
                SqlColumnDefinitionRawGroupBlock::class,
                SqlCreateTableColumnDefinitionGroupBlock::class,
                SqlUpdateColumnAssignmentSymbolBlock::class,
            )

        if (isExpectedClassType(expectedClassTypes, childBlock)) return true

        if (isNewLineSqlComment(child, childBlock)) return true

        return (
            isNewLineGroupBlockAfterRegistrationChild(childBlock, lastGroup) ||
                (childBlock is SqlRightPatternBlock && childBlock.isNewLine(lastGroup))
        )
    }

    /**
     * Retains the block only for comments that are broken down.
     */
    private fun isNewLineSqlComment(
        child: ASTNode,
        childBlock: SqlBlock,
    ): Boolean {
        val commentBlockType =
            listOf(
                SqlLineCommentBlock::class,
                SqlBlockCommentBlock::class,
            )
        val prevSpace = PsiTreeUtil.prevLeaf(child.psi)
        return isExpectedClassType(
            commentBlockType,
            childBlock,
        ) &&
            prevSpace?.text?.contains("\n") == true
    }

    /**
     * Determines whether a newline is required after registering itself as a child of the parent block.
     */
    private fun isNewLineGroupBlockAfterRegistrationChild(
        childBlock: SqlBlock,
        lastGroup: SqlBlock?,
    ): Boolean {
        fun isParallelListRawChild(): Boolean =
            childBlock is SqlCommaBlock &&
                (
                    lastGroup is SqlParallelListBlock ||
                        lastGroup?.parentBlock is SqlParallelListBlock
                )

        if (isParallelListRawChild()) return false

        if (parentSetProcessor.isNewGroupAndNotSetLineKeywords(childBlock, lastGroup)) {
            return if (lastGroup is SqlSubQueryGroupBlock) {
                val lastGroupChildren = lastGroup.childBlocks
                (lastGroupChildren.isNotEmpty() && lastGroupChildren.drop(1).isNotEmpty())
            } else {
                true
            }
        }
        return false
    }

    /**
     * Updates the parent block or registers itself as a new group block based on the class of the target block.
     */
    private fun updateBlockParentAndLAddGroup(childBlock: SqlBlock) {
        val lastGroupBlock = blockBuilder.getLastGroupTopNodeIndexHistory()
        val lastIndentLevel = lastGroupBlock?.indent?.indentLevel
        if (lastGroupBlock == null || lastIndentLevel == null) {
            parentSetProcessor.updateGroupBlockAddGroup(
                childBlock,
            )
            return
        }

        when (childBlock) {
            is SqlKeywordGroupBlock -> {
                parentSetProcessor.updateKeywordGroupBlockParentAndAddGroup(
                    lastGroupBlock,
                    lastIndentLevel,
                    childBlock,
                )
            }

            is SqlColumnDefinitionRawGroupBlock -> {
                parentSetProcessor.updateColumnDefinitionRawGroupBlockParentAndAddGroup(
                    lastGroupBlock,
                    lastIndentLevel,
                    childBlock,
                )
            }

            is SqlColumnRawGroupBlock -> {
                parentSetProcessor.updateColumnRawGroupBlockParentAndAddGroup(
                    lastGroupBlock,
                    childBlock,
                )
            }

            is SqlInlineGroupBlock -> {
                // case-end
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            is SqlInlineSecondGroupBlock -> {
                parentSetProcessor.updateInlineSecondGroupBlockParentAndAddGroup(
                    lastGroupBlock,
                    lastIndentLevel,
                    childBlock,
                )
            }

            is SqlColumnBlock -> {
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            is SqlElConditionLoopCommentBlock -> {
                parentSetProcessor.updateConditionLoopCommentBlockParent(
                    lastGroupBlock,
                    childBlock,
                )
            }

            is SqlWordBlock, is SqlOtherBlock, is SqlLineCommentBlock, is SqlBlockCommentBlock -> {
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            is SqlSubQueryGroupBlock -> {
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            is SqlRightPatternBlock -> {
                parentSetProcessor.updateSqlRightPatternBlockParent(
                    childBlock,
                )
            }

            is SqlElSymbolBlock -> {
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            is SqlDataTypeBlock -> {
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            is SqlCommaBlock -> {
                if (lastGroupBlock is SqlCommaBlock) {
                    blockBuilder.removeLastGroupTopNodeIndexHistory()
                }
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            else -> {
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }
        }
    }

    /**
     * Creates the indentation length for the block.
     */
    open fun createBlockIndentLen(): Int = 0

    /**
     * Creates a block for the given child AST node.
     */
    open fun getBlock(child: ASTNode): SqlBlock {
        val defaultFormatCtx =
            SqlBlockFormattingContext(
                wrap,
                alignment,
                spacingBuilder,
                isEnableFormat(),
                formatMode,
            )
        val lastGroup = blockBuilder.getLastGroupTopNodeIndexHistory()
        return when (child.elementType) {
            SqlTypes.KEYWORD -> {
                return blockUtil.getKeywordBlock(
                    child,
                    blockBuilder.getLastGroupTopNodeIndexHistory(),
                )
            }

            SqlTypes.DATATYPE -> {
                SqlDataTypeBlock(
                    child,
                    defaultFormatCtx,
                )
            }

            SqlTypes.LEFT_PAREN -> {
                return blockUtil.getSubGroupBlock(lastGroup, child)
            }

            SqlTypes.OTHER -> return if (lastGroup is SqlUpdateSetGroupBlock) {
                SqlUpdateColumnAssignmentSymbolBlock(child, defaultFormatCtx)
            } else {
                SqlOtherBlock(
                    child,
                    defaultFormatCtx,
                )
            }

            SqlTypes.RIGHT_PAREN -> return SqlRightPatternBlock(
                child,
                defaultFormatCtx,
            )

            SqlTypes.COMMA -> {
                return blockUtil.getCommaGroupBlock(lastGroup, child)
            }

            SqlTypes.WORD -> return blockUtil.getWordBlock(lastGroup, child)

            SqlTypes.BLOCK_COMMENT -> {
                return blockUtil.getBlockCommentBlock(child, createBlockCommentSpacingBuilder())
            }

            SqlTypes.LINE_COMMENT ->
                return SqlLineCommentBlock(
                    child,
                    defaultFormatCtx,
                )

            SqlTypes.PLUS, SqlTypes.MINUS, SqlTypes.ASTERISK, SqlTypes.SLASH ->
                return SqlElSymbolBlock(
                    child,
                    defaultFormatCtx,
                )

            SqlTypes.LE, SqlTypes.LT, SqlTypes.EL_EQ, SqlTypes.EL_NE, SqlTypes.GE, SqlTypes.GT ->
                return SqlElSymbolBlock(
                    child,
                    defaultFormatCtx,
                )

            SqlTypes.STRING, SqlTypes.NUMBER, SqlTypes.BOOLEAN ->
                return SqlLiteralBlock(
                    child,
                    defaultFormatCtx,
                )

            else ->
                SqlUnknownBlock(
                    child,
                    defaultFormatCtx,
                )
        }
    }

    /**
     * Creates a spacing builder for custom spacing rules.
     */
    protected open fun createSpacingBuilder(): SqlCustomSpacingBuilder = SqlCustomSpacingBuilder()

    /**
     * Creates a spacing builder specifically for block comments.
     */
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

    /**
     * Returns the indentation for the block.
     */
    override fun getIndent(): Indent? =
        if (isAdjustIndentOnEnter()) {
            null
        } else {
            Indent.getSpaceIndent(indent.indentLen)
        }

    /**
     * Determines whether to adjust the indentation on pressing Enter.
     */
    fun isAdjustIndentOnEnter(): Boolean = formatMode == FormattingMode.ADJUST_INDENT_ON_ENTER && !isEnableFormat()

    /**
     * Returns the spacing between two child blocks.
     */
    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? {
        if (isAdjustIndentOnEnter()) return null

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
                is SqlElBlockCommentBlock, is SqlWhitespaceBlock -> {
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
                is SqlBlockCommentBlock, is SqlLineCommentBlock, is SqlNewGroupBlock -> {
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

        // Create Table Column Definition Raw Group Block
        CreateTableUtil.getColumnDefinitionRawGroupSpacing(child1, child2)?.let { return it }

        when (child2) {
            is SqlColumnDefinitionRawGroupBlock ->
                SqlCustomSpacingBuilder()
                    .getSpacingColumnDefinitionRaw(
                        child2,
                    )?.let { return it }

            is SqlRightPatternBlock -> return SqlCustomSpacingBuilder().getSpacingRightPattern(
                child2,
            )

            is SqlColumnBlock ->
                SqlCustomSpacingBuilder()
                    .getSpacingColumnDefinition(child2)
                    ?.let { return it }
        }

        if (child1 is SqlBlock && (child2 is SqlCommaBlock || child2 is SqlColumnRawGroupBlock)) {
            SqlCustomSpacingBuilder().getSpacingWithIndentComma(child1, child2)?.let { return it }
        }

        val spacing: Spacing? = customSpacingBuilder?.getCustomSpacing(child1, child2)
        return spacing ?: spacingBuilder.getSpacing(this, child1, child2)
    }

    /**
     * Returns the child attributes for a new child at the specified index.
     */
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

    /**
     * Returns the child indentation for the block.
     */
    override fun getChildIndent(): Indent? =
        if (isEnableFormat()) {
            Indent.getSpaceIndent(4)
        } else {
            Indent.getSpaceIndent(0)
        }

    /**
     * Determines whether the block is a leaf node.
     */
    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
