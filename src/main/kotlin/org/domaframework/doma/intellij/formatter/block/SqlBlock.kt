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
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Indent
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil
import org.domaframework.doma.intellij.psi.SqlTypes
import org.jetbrains.kotlin.psi.psiUtil.startOffset

open class SqlBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    internal open val spacingBuilder: SpacingBuilder,
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
         *
         * Returns `0` if there is no line break.
         */
        var indentLen: Int,
        /**
         * Indentation baseline applied to the group itself.
         *
         * Even if the group does not start on a new line,
         * it determines and applies indentation to the group based on factors such as the number of preceding characters.
         */
        var groupIndentLen: Int,
    )

    open var parentBlock: SqlBlock? = null
    open val childBlocks = mutableListOf<SqlBlock>()
    open var prevBlocks = emptyList<SqlBlock>()

    fun getChildrenTextLen(): Int = childBlocks.sumOf { child -> calculateChildTextLength(child) }

    private fun calculateChildTextLength(child: SqlBlock): Int {
        val nonCommentChildren = child.childBlocks.filterNot { it is SqlDefaultCommentBlock }

        if (nonCommentChildren.isNotEmpty()) {
            return child.getChildrenTextLen() + child.getNodeText().length
        }
        if (isExcludedFromTextLength(child)) {
            return 0
        }
        return child.getNodeText().length + 1
    }

    private fun isExcludedFromTextLength(block: SqlBlock): Boolean = block.node.elementType in setOf(SqlTypes.DOT, SqlTypes.RIGHT_PAREN)

    /**
     * Checks if a conditional loop directive is registered before the parent block.
     *
     * @note
     * If the next element after a conditional directive is not a conditional directive block,
     * the directive becomes a child of the next element block.
     * Therefore, if the first element in [childBlocks] is a conditional directive,
     * it can be determined that—syntactically—the conditional directive was placed immediately before the current block.
     */
    protected fun isConditionLoopDirectiveRegisteredBeforeParent(): Boolean {
        val firstPrevBlock = (prevBlocks.lastOrNull() as? SqlElConditionLoopCommentBlock)
        parentBlock?.let { parent ->
            return firstPrevBlock != null &&
                firstPrevBlock.conditionEnd != null &&
                firstPrevBlock.node.startOffset > parent.node.startOffset
        }
        return false
    }

    /**
     * Determines if this is the element immediately after a conditional loop directive.
     *
     * @note
     * The parent conditional loop directive becomes a child of the element immediately after the conditional loop directive.
     * In the following case, "%if" is a child of "status", and the following "=" and "'pending'" are children of "%if".
     * Therefore, set the condition to break line only when the parent of the conditional loop directive is a group block.
     *
     * @example
     * ```sql
     * WHERE
     *      /*%if status == "pending" */
     *      status = 'pending'
     * ```
     */
    protected fun isElementAfterConditionLoopDirective(): Boolean =
        (parentBlock as? SqlElConditionLoopCommentBlock)?.let { parent ->
            parent.childBlocks.firstOrNull() == this &&
                (parent.parentBlock is SqlNewGroupBlock || parent.parentBlock is SqlElConditionLoopCommentBlock)
        } == true

    protected fun isFirstChildConditionLoopDirective(): Boolean = childBlocks.firstOrNull() is SqlElConditionLoopCommentBlock

    fun getChildBlocksDropLast(
        dropIndex: Int = 1,
        skipCommentBlock: Boolean = true,
    ): List<SqlBlock> {
        val children = childBlocks.dropLast(dropIndex)
        if (skipCommentBlock) {
            return children.filter { it !is SqlDefaultCommentBlock }
        }
        return children
    }

    open val indent: ElementIndent =
        ElementIndent(
            IndentType.FILE,
            0,
            0,
        )

    open fun setParentGroupBlock(lastGroup: SqlBlock?) {
        parentBlock = lastGroup
        prevBlocks = parentBlock?.childBlocks?.toList() ?: emptyList()
        parentBlock?.addChildBlock(this)
        setParentPropertyBlock(lastGroup)
    }

    open fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        // This method can be overridden to set additional properties on the parent block if needed.
    }

    open fun addChildBlock(childBlock: SqlBlock) {
        if (!childBlocks.contains(childBlock)) {
            childBlocks.add(childBlock)
        }
    }

    fun getNodeText() = node.text.lowercase()

    fun isEnableFormat(): Boolean = enableFormat

    open fun isSaveSpace(lastGroup: SqlBlock?): Boolean =
        when (lastGroup) {
            is SqlNewGroupBlock -> shouldSaveSpaceForNewGroup(lastGroup)
            else -> {
                shouldSaveSpaceForConditionLoop()
            }
        } == true

    private fun shouldSaveSpaceForConditionLoop(): Boolean =
        isConditionLoopDirectiveRegisteredBeforeParent() ||
            isElementAfterConditionLoopDirective() ||
            isFirstChildConditionLoopDirective()

    private fun shouldSaveSpaceForNewGroup(parent: SqlNewGroupBlock): Boolean {
        val prevWord = prevBlocks.lastOrNull { it !is SqlCommentBlock }

        if (isNonBreakingKeywordCombination(parent, prevWord)) {
            return false
        }

        return isFollowedByConditionLoop() || isPrecededByConditionLoop(parent)
    }

    private fun isNonBreakingKeywordCombination(
        parent: SqlNewGroupBlock,
        prevWord: SqlBlock?,
    ): Boolean =
        SqlKeywordUtil.isSetLineKeyword(getNodeText(), parent.getNodeText()) ||
            SqlKeywordUtil.isSetLineKeyword(getNodeText(), prevWord?.getNodeText() ?: "")

    private fun isFollowedByConditionLoop(): Boolean = childBlocks.lastOrNull() is SqlElConditionLoopCommentBlock

    private fun isPrecededByConditionLoop(parent: SqlNewGroupBlock): Boolean {
        val lastPrevBlock = prevBlocks.lastOrNull()
        return lastPrevBlock is SqlElConditionLoopCommentBlock &&
            lastPrevBlock.node.psi.startOffset > parent.node.psi.startOffset
    }

    /**
     * Creates the indentation length for the block.
     *
     * @return The number of spaces to use for indentation
     */
    open fun createBlockIndentLen(): Int = 0

    open fun createGroupIndentLen(): Int = 0

    open fun getBlock(child: ASTNode): SqlBlock = this

    /**
     * Creates a spacing builder for custom spacing rules.
     *
     * @return A new instance of SqlCustomSpacingBuilder
     */
    protected open fun createSpacingBuilder(): SqlCustomSpacingBuilder = SqlCustomSpacingBuilder()

    override fun buildChildren(): List<Block?>? = emptyList()

    /**
     * Determines whether to adjust the indentation on pressing Enter.
     *
     * @return true if indentation should be adjusted on Enter, false otherwise
     */
    fun isAdjustIndentOnEnter(): Boolean = formatMode == FormattingMode.ADJUST_INDENT_ON_ENTER && !isEnableFormat()

    /**
     * Returns the indentation for the block.
     *
     * @return The indent to apply to this block, or null if no indentation should be applied
     */
    override fun getIndent(): Indent? =
        if (isAdjustIndentOnEnter()) {
            null
        } else {
            Indent.getSpaceIndent(indent.indentLen)
        }

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? = null

    /**
     * Creates a spacing builder specifically for directive block comments.
     */
    protected fun createBlockDirectiveCommentSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
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
            )

    /**
     * Returns the child indentation for the block.
     *
     * @return The indent to apply to child blocks
     */
    override fun getChildIndent(): Indent? =
        if (isEnableFormat()) {
            Indent.getSpaceIndent(DEFAULT_INDENT_SIZE)
        } else {
            Indent.getSpaceIndent(0)
        }

    companion object {
        private const val DEFAULT_INDENT_SIZE = 4
    }

    /**
     * Determines whether the block is a leaf node.
     *
     * @return true if this block has no child nodes, false otherwise
     */
    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
