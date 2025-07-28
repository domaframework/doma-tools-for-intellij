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
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElSymbolBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateColumnAssignmentSymbolBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateSetGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.update.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlDataTypeParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.other.SqlEscapeBlock
import org.domaframework.doma.intellij.formatter.block.other.SqlOtherBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlAliasBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlFunctionGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlTableBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlWordBlock
import org.domaframework.doma.intellij.formatter.builder.SqlBlockBuilder
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.handler.CreateClauseHandler
import org.domaframework.doma.intellij.formatter.processor.SqlSetParentGroupProcessor
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlBlockGenerator
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlFileBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val customSpacingBuilder: SqlCustomSpacingBuilder?,
    override val spacingBuilder: SpacingBuilder,
    enableFormat: Boolean,
    private val formatMode: FormattingMode,
) : SqlBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
    override val indent =
        ElementIndent(
            IndentType.FILE,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(null)
    }

    private val blocks = mutableListOf<AbstractBlock>()

    private val blockBuilder = SqlBlockBuilder()
    private val parentSetProcessor = SqlSetParentGroupProcessor(blockBuilder)
    private val blockUtil = SqlBlockGenerator(this, isEnableFormat(), formatMode)

    private val pendingCommentBlocks = mutableListOf<SqlBlock>()

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
                prevNonWhiteSpaceNode = child
                updateCommentParentAndIdent(childBlock)
                updateBlockParentAndLAddGroup(childBlock)
                updateWhiteSpaceInclude(lastBlock, childBlock, lastGroup)
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
     * Creates a block for the given child AST node.
     */
    override fun getBlock(child: ASTNode): SqlBlock {
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

            SqlTypes.OTHER -> {
                return if (lastGroup is SqlUpdateSetGroupBlock &&
                    lastGroup.columnDefinitionGroupBlock != null
                ) {
                    SqlUpdateColumnAssignmentSymbolBlock(
                        child,
                        defaultFormatCtx,
                    )
                } else {
                    val escapeStrings = listOf("\"", "`", "[", "]")
                    if (escapeStrings.contains(child.text)) {
                        SqlEscapeBlock(
                            child,
                            defaultFormatCtx,
                        )
                    } else {
                        SqlOtherBlock(
                            child,
                            defaultFormatCtx,
                        )
                    }
                }
            }

            SqlTypes.RIGHT_PAREN -> return SqlRightPatternBlock(
                child,
                defaultFormatCtx,
            )

            SqlTypes.COMMA -> {
                return if (lastGroup is SqlWithQueryGroupBlock) {
                    SqlWithCommonTableGroupBlock(child, defaultFormatCtx)
                } else {
                    blockUtil.getCommaGroupBlock(lastGroup, child)
                }
            }

            SqlTypes.FUNCTION_NAME ->
                return SqlFunctionGroupBlock(child, defaultFormatCtx)

            SqlTypes.WORD -> {
                return if (lastGroup is SqlWithQueryGroupBlock) {
                    SqlWithCommonTableGroupBlock(child, defaultFormatCtx)
                } else {
                    blockUtil.getWordBlock(lastGroup, child)
                }
            }

            SqlTypes.BLOCK_COMMENT -> {
                return if (lastGroup is SqlWithCommonTableGroupBlock) {
                    SqlWithCommonTableGroupBlock(child, defaultFormatCtx)
                } else {
                    blockUtil.getBlockCommentBlock(child, createBlockCommentSpacingBuilder())
                }
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
     * Sets the parent and indentation for the comment element based on the element that was registered earlier.
     */
    private fun updateCommentParentAndIdent(commentBlock: SqlBlock) {
        if (commentBlock !is SqlCommentBlock) return
        if (commentBlock is SqlElConditionLoopCommentBlock) {
            blockBuilder.addConditionOrLoopBlock(
                commentBlock,
            )
        } else {
            (commentBlock as? SqlDefaultCommentBlock)?.let {
                blockBuilder.addCommentBlock(
                    commentBlock,
                )
            }
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
    ): Boolean = childBlock.isSaveSpace(lastGroup)

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

        if (childBlock is SqlDefaultCommentBlock) return

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

            is SqlWordBlock, is SqlOtherBlock -> {
                parentSetProcessor.updateGroupBlockParentAndAddGroup(
                    childBlock,
                )
            }

            is SqlSubGroupBlock -> {
                parentSetProcessor.updateSubGroupBlockParent(
                    lastGroupBlock,
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
     * Creates a spacing builder specifically for block comments.
     */
    private fun createBlockCommentSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.BLOCK_COMMENT_CONTENT,
                Spacing.createSpacing(0, 0, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.EL_ID_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.EL_PRIMARY_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.EL_STRING,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.EL_NUMBER,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.BOOLEAN,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.EL_NULL,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.EL_FIELD_ACCESS_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.HASH,
                Spacing.createSpacing(0, 0, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.EL_ID_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.EL_PRIMARY_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.EL_STRING,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.EL_NUMBER,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.BOOLEAN,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.EL_NULL,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.EL_FIELD_ACCESS_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.HASH,
                SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.CARET,
                Spacing.createSpacing(0, 0, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.EL_ID_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.EL_PRIMARY_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.EL_STRING,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.EL_NUMBER,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.BOOLEAN,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.EL_NULL,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.EL_FIELD_ACCESS_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.CARET,
                SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR,
                Spacing.createSpacing(1, 1, 0, true, 0),
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
                SqlTypes.EL_ID_EXPR,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.EL_PRIMARY_EXPR,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.STRING,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.EL_NUMBER,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.EL_NULL,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BOOLEAN,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.EL_FIELD_ACCESS_EXPR,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(1, 1, 0, true, 0),
            ).withSpacing(
                SqlTypes.BLOCK_COMMENT_CONTENT,
                SqlTypes.BLOCK_COMMENT_END,
                Spacing.createSpacing(0, 0, 0, true, 0),
            )

    /**
     * Returns the spacing between two child blocks.
     */
    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? {
        if (isAdjustIndentOnEnter()) return null
        val childBlock1: SqlBlock? = child1 as? SqlBlock
        val childBlock2: SqlBlock = child2 as SqlBlock

        // The end of a line comment element is a newline, so just add a space for the indent.
        if (childBlock1 is SqlLineCommentBlock) {
            return SqlCustomSpacingBuilder().getSpacing(childBlock2)
        }

        if (childBlock1 is SqlWhitespaceBlock && childBlock2.parentBlock is SqlElConditionLoopCommentBlock) {
            val child1 = childBlock2.parentBlock as SqlElConditionLoopCommentBlock
            SqlCustomSpacingBuilder()
                .getSpacingElDirectiveComment(child1, childBlock2)
                ?.let { return it }
        }

        if (childBlock1 is SqlElBlockCommentBlock && childBlock2 !is SqlRightPatternBlock) {
            SqlCustomSpacingBuilder()
                .getSpacingElDirectiveComment(childBlock1, childBlock2)
                ?.let { return it }
        }

        if (childBlock2 is SqlRightPatternBlock) {
            return SqlCustomSpacingBuilder().getSpacingRightPattern(
                childBlock2,
            )
        }

        if (childBlock2 is SqlWithColumnGroupBlock) {
            return SqlCustomSpacingBuilder.normalSpacing
        }

        if (childBlock1 is SqlSubGroupBlock) {
            if (childBlock2 is SqlSubGroupBlock) {
                return SqlCustomSpacingBuilder.nonSpacing
            }
            if (childBlock1 is SqlInsertValueGroupBlock ||
                childBlock1 is SqlUpdateValueGroupBlock
            ) {
                return SqlCustomSpacingBuilder.normalSpacing
            }
        }

        // Do not leave a space after the comment block of the bind variable
        if (childBlock1 is SqlElBlockCommentBlock && childBlock1 !is SqlElConditionLoopCommentBlock && childBlock2 !is SqlCommentBlock) {
            return SqlCustomSpacingBuilder.nonSpacing
        }

        if (childBlock2 is SqlElBlockCommentBlock) {
            if (TypeUtil.isExpectedClassType(
                    SqlRightPatternBlock.NOT_INDENT_EXPECTED_TYPES,
                    childBlock1,
                )
            ) {
                return SqlCustomSpacingBuilder.nonSpacing
            }
            return when (childBlock1) {
                is SqlWhitespaceBlock -> {
                    SqlCustomSpacingBuilder().getSpacing(childBlock2)
                }

                else -> SqlCustomSpacingBuilder.normalSpacing
            }
        }

        if (childBlock1?.node?.elementType == SqlTypes.DOT ||
            childBlock2.node.elementType == SqlTypes.DOT
        ) {
            return SqlCustomSpacingBuilder.nonSpacing
        }

        if (childBlock1 is SqlEscapeBlock) {
            return if (!childBlock1.isEndEscape) {
                SqlCustomSpacingBuilder.nonSpacing
            } else {
                SqlCustomSpacingBuilder.normalSpacing
            }
        }

        if (childBlock2 is SqlEscapeBlock) {
            if (childBlock2.isEndEscape) {
                return SqlCustomSpacingBuilder.nonSpacing
            }
            return SqlCustomSpacingBuilder().getSpacing(childBlock2)
        }

        if (childBlock2 is SqlOtherBlock) {
            return SqlCustomSpacingBuilder().getSpacing(childBlock2)
        }

        if (childBlock2 is SqlNewGroupBlock) {
            if (childBlock1 is SqlSubGroupBlock && childBlock2.indent.indentLevel == IndentType.ATTACHED) {
                return SqlCustomSpacingBuilder.nonSpacing
            }
            when (childBlock2) {
                is SqlSubQueryGroupBlock -> {
                    if (childBlock1 is SqlNewGroupBlock) {
                        return SqlCustomSpacingBuilder.normalSpacing
                    }
                }

                is SqlDataTypeParamBlock, is SqlFunctionParamBlock -> return SqlCustomSpacingBuilder.nonSpacing
            }
        }

        // Create Table Column Definition Raw Group Block
        CreateClauseHandler
            .getColumnDefinitionRawGroupSpacing(childBlock1, childBlock2)
            ?.let { return it }

        when (childBlock2) {
            is SqlColumnDefinitionRawGroupBlock ->
                SqlCustomSpacingBuilder()
                    .getSpacingColumnDefinitionRaw(
                        childBlock2,
                    )?.let { return it }

            is SqlColumnBlock ->
                SqlCustomSpacingBuilder()
                    .getSpacingColumnDefinition(childBlock2)
                    ?.let { return it }
        }

        if (childBlock1 is SqlBlock && (childBlock2 is SqlCommaBlock || childBlock2 is SqlColumnRawGroupBlock)) {
            SqlCustomSpacingBuilder()
                .getSpacingWithIndentComma(childBlock1, childBlock2)
                ?.let { return it }
        }

        // First apply spacing logic for blocks under specific conditions,
        // then execute the general spacing logic for post-line-break blocks at the end.
        if (childBlock1 is SqlWhitespaceBlock) {
            return when (childBlock2) {
                is SqlDefaultCommentBlock, is SqlNewGroupBlock -> {
                    SqlCustomSpacingBuilder().getSpacing(childBlock2)
                }

                else -> SqlCustomSpacingBuilder().getSpacing(childBlock2)
            }
        }

        if (childBlock1 is SqlTableBlock || childBlock1 is SqlAliasBlock) {
            return SqlCustomSpacingBuilder.normalSpacing
        }

        val spacing: Spacing? = customSpacingBuilder?.getCustomSpacing(childBlock1, childBlock2)
        return spacing ?: spacingBuilder.getSpacing(this, childBlock1, childBlock2)
    }

    override fun isLeaf(): Boolean = false

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
}
