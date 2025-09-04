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
package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.insert.SqlInsertQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlArrayListGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.builder.SqlBlockBuilder
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElConditionLoopCommentBlock(
    node: ASTNode,
    override val context: SqlBlockFormattingContext,
    override val customSpacingBuilder: SqlCustomSpacingBuilder?,
) : SqlElBlockCommentBlock(
        node,
        context,
        customSpacingBuilder,
    ) {
    enum class SqlConditionLoopCommentBlockType {
        CONDITION,
        ELSE,
        LOOP,
        END,
        UNKNOWN,
        ;

        fun isEnd(): Boolean = this == END

        fun isStartDirective(): Boolean = this == CONDITION || this == LOOP

        fun isElse(): Boolean = this == ELSE
    }

    // Temporary storage for making the condition/loop directive one level above the parent depending on the child block
    var nestParentBlock: SqlElConditionLoopCommentBlock? = null

    fun setParentSelfNestBlock() {
        if (parentBlock == null) {
            setParentGroupBlock(nestParentBlock)
        }
    }

    // Hold dependency block separately from parent
    private var dependsOnBlock: SqlBlock? = null

    fun getDependsOnBlock(): SqlBlock? = dependsOnBlock

    companion object {
        const val DIRECTIVE_INDENT_STEP = 2
        private const val DEFAULT_INDENT_OFFSET = 1

        private val LINE_BREAK_PARENT_TYPES =
            listOf(
                SqlSubGroupBlock::class,
                SqlColumnRawGroupBlock::class,
            )
    }

    val conditionType: SqlConditionLoopCommentBlockType = initConditionOrLoopType(node)
    var conditionStart: SqlElConditionLoopCommentBlock? = null
    var conditionEnd: SqlElConditionLoopCommentBlock? = null

    private fun initConditionOrLoopType(node: ASTNode): SqlConditionLoopCommentBlockType {
        val psi = node.psi
        if (psi is SqlCustomElCommentExpr && psi.isConditionOrLoopDirective()) {
            if (PsiTreeUtil.getChildOfType(psi, SqlElForDirective::class.java) != null) {
                return SqlConditionLoopCommentBlockType.LOOP
            }
            val directiveElement = psi.findElementAt(2)
            if (PsiTreeUtil.getChildOfType(psi, SqlElIfDirective::class.java) != null) {
                return SqlConditionLoopCommentBlockType.CONDITION
            }
            if (directiveElement?.elementType == SqlTypes.EL_ELSE ||
                directiveElement?.elementType == SqlTypes.EL_ELSEIF
            ) {
                return SqlConditionLoopCommentBlockType.ELSE
            }
            if (directiveElement?.elementType == SqlTypes.EL_END) {
                return SqlConditionLoopCommentBlockType.END
            }
        }
        return SqlConditionLoopCommentBlockType.UNKNOWN
    }

    /**
     * Initially, set a **provisional indentation level** for conditional directives.
     *
     * If the next element is a **[SqlKeywordGroupBlock]** or **[SqlSubGroupBlock]**, align the directive’s indentation to that keyword’s indentation.
     * Otherwise, use the **provisional indentation element as the base** to align the indentation of child elements under the directive.
     *
     * @note
     * When a keyword group appears immediately before a conditional or loop directive, and a non-group block appears immediately after, the directive can end up being registered as a child of both.
     *
     * To ensure correct group block generation in the later subgroup block processing, the conditional/loop directive must remain registered as a child of the preceding keyword group.
     * If it is not retained, the resulting group block structure will be inaccurate.
     *
     */
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        // When resetting the parent of the parent condition/loop directive, check that the parent is not yet set
        if (parentBlock == null) {
            parentBlock = lastGroup
            if (!conditionType.isStartDirective()) {
                (parentBlock as? SqlElConditionLoopCommentBlock)?.conditionEnd = this
                createBlockIndentLenFromDependOn(null)
            } else if (parentBlock !is SqlElConditionLoopCommentBlock) {
                indent.indentLen = initIndentConditionLoopDirective()
            }
        }
        indent.groupIndentLen = indent.indentLen
    }

    fun setParentGroupBlock(
        lastGroup: SqlBlock?,
        builder: SqlBlockBuilder?,
    ) {
        // Basically no parent except for nested structures
        parentBlock = lastGroup
        // Determine your own indent according to the parent's indent. Calculate considering directive nesting
        indent.indentLen = calculateIndentLen(builder)
        indent.groupIndentLen = indent.indentLen
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlElConditionLoopCommentBlock && !conditionType.isStartDirective()) {
            lastGroup.conditionEnd = this
        }
    }

    fun setDependsOnBlock(block: SqlBlock?) {
        if (dependsOnBlock != null) return
        dependsOnBlock = block
    }

    /**
     * Calculate indent when dependency is determined
     * Called later because dependency indent needs to be calculated first
     * If both parent and dependency exist, prioritize parent's indent
     * Skip if the passed block is not your own dependency block, just recalculate
     */
    fun createBlockIndentLenFromDependOn(hitBlock: SqlBlock?) {
        if (hitBlock != null && hitBlock != dependsOnBlock) return
        indent.indentLen = calculateIndentLenFromDependOn()
        indent.groupIndentLen = indent.indentLen
    }

    private fun calculateIndentLenFromDependOn(): Int {
        parentBlock?.let { parent ->
            val parentGroupIndent = parent.indent.groupIndentLen
            return when (parentBlock) {
                is SqlElConditionLoopCommentBlock -> {
                    if (conditionType.isStartDirective()) {
                        parentGroupIndent.plus(DIRECTIVE_INDENT_STEP)
                    } else {
                        parentGroupIndent
                    }
                }

                is SqlSubGroupBlock -> {
                    parentGroupIndent
                }

                else -> parentGroupIndent.plus(1)
            }
        }
        return dependsOnBlock?.indent?.indentLen ?: 0
    }

    /**
     * Recalculate nested indents
     * Count the number of levels and return to the caller
     */
    fun recalculateIndentLen(baseBlockIndent: Int): Int {
        if (parentBlock is SqlSubGroupBlock) {
            return 1
        }
        val nestParent = parentBlock as? SqlElConditionLoopCommentBlock
        if (nestParent != null) {
            val nestLevel = nestParent.recalculateIndentLen(baseBlockIndent)
            indent.indentLen = nestParent.indent.indentLen.plus(DIRECTIVE_INDENT_STEP)
            indent.groupIndentLen = indent.indentLen
            return nestLevel.plus(1)
        }
        parentBlock = null
        indent.indentLen = baseBlockIndent
        indent.groupIndentLen = indent.indentLen
        return 1
    }

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? =
        customSpacingBuilder?.getCustomSpacing(child1, child2) ?: spacingBuilder.getSpacing(
            this,
            child1,
            child2,
        )

    override fun isLeaf(): Boolean = false

    override fun buildChildren(): MutableList<AbstractBlock> = buildChildBlocks { getBlock(it) }

    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.EL_IF_DIRECTIVE, SqlTypes.EL_ELSEIF_DIRECTIVE, SqlTypes.EL_FOR_DIRECTIVE ->
                SqlElBlockCommentBlock(
                    child,
                    context,
                    createBlockDirectiveCommentSpacingBuilder(),
                )

            else -> SqlUnknownBlock(child, context)
        }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        if (!conditionType.isStartDirective()) {
            return true
        }

        // Line break if parent is a condition/loop directive
        if (parentBlock is SqlElConditionLoopCommentBlock) return true

        // If `lastGroup` is a comma group or a subgroup,
        // do not insert a line break when there is no preceding child (i.e., when this node comes first).
        val isExpectedParentOfDependentType = TypeUtil.isExpectedClassType(LINE_BREAK_PARENT_TYPES, lastGroup)
        if (isExpectedParentOfDependentType) return lastGroup?.childBlocks?.isNotEmpty() == true
        return true
    }

    /**
     * When directly containing a conditional directive, determine your own indentation as the nest top.
     */
    private fun initIndentConditionLoopDirective(): Int =
        parentBlock?.let { parent ->
            val openConditionLoopDirectiveCount = getOpenDirectiveCount(null)
            when (parent) {
                is SqlSubGroupBlock -> calculateSubGroupBlockIndent(parent, openConditionLoopDirectiveCount)
                is SqlKeywordGroupBlock -> calculateKeywordGroupBlockIndent(parent, openConditionLoopDirectiveCount)

                else -> 0
            }
        } ?: 0

    private fun calculateKeywordGroupBlockIndent(
        parent: SqlKeywordGroupBlock,
        openConditionLoopDirectiveCount: Int,
    ): Int {
        val withQuerySpace = (parent as? SqlWithQueryGroupBlock)?.let { 0 } ?: 1
        return parent.indent.groupIndentLen +
            openConditionLoopDirectiveCount * DIRECTIVE_INDENT_STEP +
            withQuerySpace
    }

    /**
     * If the element directly under the directive is [SqlKeywordGroupBlock] or [SqlSubGroupBlock],
     * make it the parent and align the indentation with the group directly under it.
     *
     * If the element immediately below the directive is not [SqlKeywordGroupBlock] or [SqlSubGroupBlock],
     * align it to the previous group indent.
     */
    fun createBlockIndentLen(builder: SqlBlockBuilder?) {
        indent.indentLen = calculateIndentLen(builder)
    }

    private fun calculateIndentLen(builder: SqlBlockBuilder?): Int {
        parentBlock?.let { parent ->
            if (conditionType.isEnd() || conditionType.isElse()) {
                return parent.indent.indentLen
            }
            // Once a parent-child relationship with a conditional directive is established,
            // the top-level indentation is already calculated.
            val openConditionLoopDirectiveCount = getOpenDirectiveCount(builder)
            when (parent) {
                is SqlSubGroupBlock -> return calculateSubGroupBlockIndent(parent, openConditionLoopDirectiveCount)

                is SqlElConditionLoopCommentBlock -> {
                    if (!conditionType.isStartDirective()) {
                        parent.conditionEnd = this
                        conditionStart = parent
                        return parent.indent.indentLen
                    } else if (conditionType.isElse()) {
                        return parent.indent.indentLen
                    } else {
                        return parent.indent.indentLen + DIRECTIVE_INDENT_STEP
                    }
                }

                is SqlKeywordGroupBlock -> return calculateKeywordGroupBlockIndent(parent, openConditionLoopDirectiveCount)
                else -> return parent.indent.indentLen + openConditionLoopDirectiveCount * DIRECTIVE_INDENT_STEP
            }
        }
        return 0
    }

    override fun createGroupIndentLen(): Int = indent.indentLen

    /**
     * Count the number of [SqlElConditionLoopCommentBlock] within the same parent block.
     * Since the current directive is included in the count,
     * **subtract 1 at the end** to exclude itself.
     */
    private fun getOpenDirectiveCount(builder: SqlBlockBuilder?): Int {
        if (parentBlock !is SqlElConditionLoopCommentBlock) return 0
        val conditionLoopDirectives: List<SqlElConditionLoopCommentBlock> =
            builder?.getNotClosedConditionOrLoopBlock() ?: emptyList()
        return conditionLoopDirectives.size
    }

    /**
     * Indentation calculation when the immediately preceding group is a subgroup block.
     */
    private fun calculateSubGroupBlockIndent(
        parent: SqlSubGroupBlock,
        openDirectiveCount: Int = 0,
    ): Int {
        val parentGroupIndentLen = parent.indent.groupIndentLen
        val grand = parent.parentBlock

        if (parent is SqlArrayListGroupBlock) {
            return parent.indent.groupIndentLen
        }

        // Set the indentation determined by the parent of the subgroup block.
        grand?.let { grandParent ->
            when (grandParent) {
                is SqlCreateKeywordGroupBlock -> {
                    val grandIndentLen = grandParent.indent.groupIndentLen
                    return grandIndentLen + parentGroupIndentLen - DEFAULT_INDENT_OFFSET
                }
                is SqlInsertQueryGroupBlock -> return parentGroupIndentLen
                is SqlColumnRawGroupBlock -> {
                    val grandIndentLen = grandParent.indent.groupIndentLen
                    val prevTextLen = calculatePreviousTextLength(parent)
                    return grandIndentLen + prevTextLen
                }
            }
        }

        return if (shouldNotIndent(parent)) {
            parentGroupIndentLen + openDirectiveCount * DIRECTIVE_INDENT_STEP
        } else {
            parentGroupIndentLen + openDirectiveCount * DIRECTIVE_INDENT_STEP + DEFAULT_INDENT_OFFSET
        }
    }

    private fun calculatePreviousTextLength(parent: SqlSubGroupBlock): Int {
        var prevTextLen = DEFAULT_INDENT_OFFSET
        parent.prevChildren?.dropLast(1)?.forEach { prev ->
            prevTextLen += prev.getNodeText().length
        }
        return prevTextLen
    }

    private fun shouldNotIndent(parent: SqlSubGroupBlock): Boolean =
        TypeUtil.isExpectedClassType(SqlRightPatternBlock.NOT_INDENT_EXPECTED_TYPES, parent) ||
            parent is SqlWithCommonTableGroupBlock ||
            parent is SqlConditionalExpressionGroupBlock
}
