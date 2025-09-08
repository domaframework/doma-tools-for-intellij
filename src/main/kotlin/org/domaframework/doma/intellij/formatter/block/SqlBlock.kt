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

    // Maintain the conditional loop directive block associated with itself.
    var conditionLoopDirective: SqlElConditionLoopCommentBlock? = null

    // A flag that exists within a conditional loop directive but is not associated with the directive
    // (representing the top element of multiple lines).
    var multipleInlineDirective = false

    open var parentBlock: SqlBlock? = null
    open val childBlocks = mutableListOf<SqlBlock>()
    open var prevBlocks = emptyList<SqlBlock>()
    open val offset = 0

    companion object {
        private const val DEFAULT_INDENT_SIZE = 4
        private const val DEFAULT_TEXT_LENGTH_INCREMENT = 1
        private val EXCLUDED_FROM_TEXT_LENGTH = setOf(SqlTypes.DOT, SqlTypes.RIGHT_PAREN)
        private val SPACING_ONE = Spacing.createSpacing(1, 1, 0, true, 0)
        private val SPACING_ZERO = Spacing.createSpacing(0, 0, 0, true, 0)
        private val SPACING_ONE_NO_KEEP = Spacing.createSpacing(1, 1, 0, false, 0)
    }

    fun getChildrenTextLen(): Int = childBlocks.sumOf { child -> calculateChildTextLength(child) }

    private fun calculateChildTextLength(child: SqlBlock): Int {
        val nonCommentChildren = child.childBlocks.filterNot { it is SqlDefaultCommentBlock }

        // True only on the first loop iteration when the current element is the first child.
        // If the subgroup is empty, return the length of “)”;
        // otherwise DEFAULT_TEXT_LENGTH_INCREMENT already adds a space, so “)” needs no extra length.
        return when {
            nonCommentChildren.isNotEmpty() -> child.getChildrenTextLen() + child.getNodeText().length
            isExcludedFromTextLength(child) -> if (childBlocks.firstOrNull() == child) child.getNodeText().length else 0
            else -> child.getNodeText().length + DEFAULT_TEXT_LENGTH_INCREMENT
        }
    }

    private fun isExcludedFromTextLength(block: SqlBlock): Boolean = block.node.elementType in EXCLUDED_FROM_TEXT_LENGTH

    fun getChildBlocksDropLast(
        dropIndex: Int = 1,
        skipCommentBlock: Boolean = true,
        skipConditionLoopCommentBlock: Boolean = true,
    ): List<SqlBlock> {
        var children = childBlocks.dropLast(dropIndex)
        if (skipCommentBlock) {
            children = children.filter { it !is SqlDefaultCommentBlock }
        }
        if (skipConditionLoopCommentBlock) {
            children = children.filter { it !is SqlElConditionLoopCommentBlock }
        }
        return children
    }

    open val indent: ElementIndent =
        ElementIndent(
            IndentType.FILE,
            0,
            0,
        )

    /**
     * Calculate indentation and line breaks based on the parent block and conditional loop directives with no dependency target set.
     * @param lastGroup The last group block
     */
    open fun setParentGroupBlock(lastGroup: SqlBlock?) {
        parentBlock = lastGroup
        setPrevBlocks()
        parentBlock?.addChildBlock(this)
        setParentPropertyBlock(lastGroup)
        setIndentLen()
    }

    fun setPrevBlocks(parent: SqlBlock? = parentBlock) {
        parent?.let { p ->
            // Retrieve the first conditional loop directive and the closing tag of the last conditional loop directive.
            val firstConditionStart = p.childBlocks.firstOrNull { it.conditionLoopDirective != null }?.conditionLoopDirective
            val lastConditionEnd =
                p.childBlocks
                    .lastOrNull {
                        it.conditionLoopDirective != null &&
                            it.conditionLoopDirective?.conditionEnd != null
                    }?.conditionLoopDirective
                    ?.conditionEnd
            var openDirective: SqlElConditionLoopCommentBlock? = null

            val filterBlockInlineOpenDirectives =
                if (firstConditionStart != null && lastConditionEnd != null) {
                    p.childBlocks.filterNot {
                        it.node.startOffset in
                            (firstConditionStart.node.startOffset until lastConditionEnd.node.startOffset)
                    }
                } else if (firstConditionStart != null) {
                    openDirective = firstConditionStart
                    p.childBlocks.filter { it.node.startOffset >= firstConditionStart.node.startOffset }
                } else {
                    p.childBlocks
                }

            prevBlocks = filterBlockInlineOpenDirectives.filter { it != this }
        }
    }

    /**
     *  Indentation calculation
     *
     * * When `multipleInlineDirective` is **true**: sibling blocks whose parent is **outside** the conditional loop directive.
     * * When `multipleInlineDirective` is **false**: sibling blocks whose parent is **inside** the conditional loop directive, or the block body that the conditional loop directive depends on.
     *
     */
    protected fun setIndentLen(baseDirective: SqlElConditionLoopCommentBlock? = conditionLoopDirective): Int {
        indent.indentLen =
            if (multipleInlineDirective) {
                baseDirective?.indent?.indentLen ?: indent.indentLen
            } else {
                if (baseDirective?.getDependsOnBlock() == this) {
                    baseDirective.indent.indentLen // The block body that the conditional loop directive depends on.
                } else {
                    createBlockIndentLen() // Sibling blocks whose parent is within a conditional loop directive.
                }
            }
        return indent.indentLen
    }

    fun createBlockIndentLenDirective(
        parent: SqlBlock?,
        dependDirective: SqlElConditionLoopCommentBlock?,
    ) {
        val dependDirectiveOnBlock = dependDirective?.getDependsOnBlock()
        if (dependDirectiveOnBlock == null) {
            conditionLoopDirective = dependDirective
            conditionLoopDirective?.setDependsOnBlock(this)
            conditionLoopDirective?.createBlockIndentLenFromDependOn(this)
        }
        setPrevBlocks(parent)
        // Check its own parent block and the parent of the block that `notDependDirective` depends on,
        // and adjust the indentation as needed so that it fits within the conditional loop directive block.
        val directiveDependent = conditionLoopDirective?.getDependsOnBlock()
        if (directiveDependent == null || parent == directiveDependent.parentBlock) {
            // Search among sibling blocks for those associated with a conditional loop directive.
            // Even if a sibling block is associated with a conditional loop directive,
            // there are cases where the indentation is aligned with the parent rather than the conditional loop directive.
            val inlineDirectiveParentBlock =
                parent?.let { p -> p.node.startOffset >= (dependDirective?.node?.startOffset ?: 0) } == true
            multipleInlineDirective = dependDirective?.getDependsOnBlock() != this && !inlineDirectiveParentBlock
            setIndentLen(dependDirective)
        }
    }

    /**
     * Trace back from the associated directive and recalculate the indentation of the nested directives.
     */
    fun recalculateDirectiveIndent() {
        conditionLoopDirective?.let { directive ->
            directive.recalculateIndentLen(createBlockIndentLen())
            indent.indentLen = directive.indent.indentLen
            indent.groupIndentLen = createGroupIndentLen()
        }
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

    /**
     * Block-specific line break determination.
     */
    open fun isSaveSpace(lastGroup: SqlBlock?): Boolean =
        when (lastGroup) {
            is SqlNewGroupBlock -> shouldSaveSpaceForNewGroup(lastGroup)
            else -> false
        }

    private fun shouldSaveSpaceForNewGroup(parent: SqlNewGroupBlock): Boolean {
        val prevWord = prevBlocks.lastOrNull { it !is SqlCommentBlock }

        if (isNonBreakingKeywordCombination(parent, prevWord)) {
            return false
        }

        return hasConditionLoopAround(parent)
    }

    private fun isNonBreakingKeywordCombination(
        parent: SqlNewGroupBlock,
        prevWord: SqlBlock?,
    ): Boolean {
        val currentText = getNodeText()
        val parentText = parent.getNodeText()
        val prevText = prevWord?.getNodeText() ?: ""

        return SqlKeywordUtil.isSetLineKeyword(currentText, parentText) ||
            SqlKeywordUtil.isSetLineKeyword(currentText, prevText)
    }

    private fun hasConditionLoopAround(parent: SqlNewGroupBlock): Boolean = isFollowedByConditionLoop() || isPrecededByConditionLoop(parent)

    private fun isFollowedByConditionLoop(): Boolean = childBlocks.lastOrNull() is SqlElConditionLoopCommentBlock

    private fun isPrecededByConditionLoop(parent: SqlNewGroupBlock): Boolean {
        val lastPrevBlock = prevBlocks.lastOrNull()
        return lastPrevBlock is SqlElConditionLoopCommentBlock &&
            lastPrevBlock.node.psi.startOffset > parent.node.psi.startOffset
    }

    /**
     * Set the indentation for line breaks caused by conditional loop directives based on the parent block and the conditional loop directive.
     * @return The number of spaces to use for indentation
     */
    open fun createBlockIndentLen(): Int = 0

    /**
     * Calculate the indentation to apply to its own child blocks.
     * @return The number of spaces to use for child block indentation
     */
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
    protected open fun createBlockDirectiveCommentSpacingBuilder(): SqlCustomSpacingBuilder {
        val builder = SqlCustomSpacingBuilder()

        // Types that need spacing after BLOCK_COMMENT_START
        val typesNeedingSpaceAfterStart =
            listOf(
                SqlTypes.EL_ID_EXPR,
                SqlTypes.EL_PRIMARY_EXPR,
                SqlTypes.EL_STRING,
                SqlTypes.EL_NUMBER,
                SqlTypes.BOOLEAN,
                SqlTypes.EL_NULL,
                SqlTypes.EL_FIELD_ACCESS_EXPR,
                SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR,
            )

        // Types that need spacing before BLOCK_COMMENT_END
        val typesNeedingSpaceBeforeEnd =
            listOf(
                SqlTypes.EL_ID_EXPR,
                SqlTypes.EL_PRIMARY_EXPR,
                SqlTypes.STRING,
                SqlTypes.EL_NUMBER,
                SqlTypes.EL_NULL,
                SqlTypes.BOOLEAN,
                SqlTypes.EL_FIELD_ACCESS_EXPR,
                SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR,
            )

        typesNeedingSpaceAfterStart.forEach { type ->
            builder.withSpacing(SqlTypes.BLOCK_COMMENT_START, type, SPACING_ONE)
        }

        builder.withSpacing(SqlTypes.BLOCK_COMMENT_START, SqlTypes.HASH, SPACING_ZERO)
        builder.withSpacing(SqlTypes.BLOCK_COMMENT_START, SqlTypes.CARET, SPACING_ZERO)

        typesNeedingSpaceAfterStart.forEach { type ->
            builder.withSpacing(SqlTypes.HASH, type, SPACING_ONE)
        }

        typesNeedingSpaceAfterStart.forEach { type ->
            builder.withSpacing(SqlTypes.CARET, type, SPACING_ONE)
        }

        builder.withSpacing(SqlTypes.BLOCK_COMMENT_CONTENT, SqlTypes.BLOCK_COMMENT_END, SPACING_ZERO)
        builder.withSpacing(SqlTypes.EL_FIELD_ACCESS_EXPR, SqlTypes.OTHER, SPACING_ONE_NO_KEEP)
        builder.withSpacing(SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR, SqlTypes.OTHER, SPACING_ONE_NO_KEEP)

        typesNeedingSpaceBeforeEnd.forEach { type ->
            builder.withSpacing(type, SqlTypes.BLOCK_COMMENT_END, SPACING_ONE)
        }

        return builder
    }

    protected fun calculatePrevBlocksLength(
        children: List<SqlBlock>,
        parent: SqlBlock,
    ): Int {
        var prevBlock: SqlBlock? = null
        val prevChildren =
            children
                .filter { it !is SqlDefaultCommentBlock }

        val prevSumLength =
            prevChildren.sumOf { prev ->
                val sum =
                    prev
                        .getChildrenTextLen()
                        .plus(
                            if (prev.node.elementType == SqlTypes.DOT ||
                                prev.node.elementType == SqlTypes.RIGHT_PAREN
                            ) {
                                0
                            } else if (prev.isOperationSymbol() && prevBlock?.isOperationSymbol() == true) {
                                // When operators appear consecutively, the first symbol includes the text length for the last space.
                                // Subsequent symbols add only their own symbol length.
                                prev.getNodeText().length
                            } else {
                                prev.getNodeText().length.plus(1)
                            },
                        )
                prevBlock = prev
                return@sumOf sum
            }
        return prevSumLength.plus(parent.indent.groupIndentLen)
    }

    fun isOperationSymbol(): Boolean =
        node.elementType in
            listOf(
                SqlTypes.PLUS,
                SqlTypes.MINUS,
                SqlTypes.ASTERISK,
                SqlTypes.AT_SIGN,
                SqlTypes.SLASH,
                SqlTypes.HASH,
                SqlTypes.LE,
                SqlTypes.LT,
                SqlTypes.EL_EQ,
                SqlTypes.EL_NE,
                SqlTypes.GE,
                SqlTypes.GT,
                SqlTypes.OTHER,
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

    /**
     * Determines whether the block is a leaf node.
     *
     * @return true if this block has no child nodes, false otherwise
     */
    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
