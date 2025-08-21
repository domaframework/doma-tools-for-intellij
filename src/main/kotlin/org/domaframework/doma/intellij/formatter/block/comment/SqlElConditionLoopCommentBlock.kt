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
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.util.StringUtil
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
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElConditionLoopCommentBlock(
    node: ASTNode,
    private val context: SqlBlockFormattingContext,
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

    companion object {
        private const val DIRECTIVE_INDENT_STEP = 2
        private const val DEFAULT_INDENT_OFFSET = 1

        private val LINE_BREAK_PARENT_TYPES =
            listOf(
                SqlSubGroupBlock::class,
                SqlColumnRawGroupBlock::class,
                SqlElConditionLoopCommentBlock::class,
            )
    }

    var tempParentBlock: SqlBlock? = null
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
     * To ensure correct group block generation in the later sub-group block processing, the conditional/loop directive must remain registered as a child of the preceding keyword group.
     * If it is not retained, the resulting group block structure will be inaccurate.
     *
     */
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)

        childBlocks.forEach { child ->
            if (child is SqlElConditionLoopCommentBlock && child.conditionType.isStartDirective()) {
                // If the child is a condition loop directive, align its indentation with the parent directive
                child.indent.indentLen = indent.indentLen.plus(DIRECTIVE_INDENT_STEP)
            } else if (child is SqlLineCommentBlock) {
                if (PsiTreeUtil.prevLeaf(child.node.psi, false)?.text?.contains(StringUtil.LINE_SEPARATE) == true) {
                    child.indent.indentLen = indent.groupIndentLen
                } else {
                    child.indent.indentLen = 1
                }
            } else {
                child.indent.indentLen = indent.groupIndentLen
            }
        }
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlElConditionLoopCommentBlock && conditionType.isEnd()) {
            lastGroup.conditionEnd = this
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> {
        val blocks = mutableListOf<AbstractBlock>()
        var child = node.firstChildNode
        while (child != null) {
            if (child !is PsiWhiteSpace) {
                val block = getBlock(child)
                blocks.add(block)
            }
            child = child.treeNext
        }
        return blocks
    }

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

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        if (conditionType.isEnd() || conditionType.isElse()) {
            return true
        }
        if (TypeUtil.isExpectedClassType(LINE_BREAK_PARENT_TYPES, lastGroup)) {
            return lastGroup?.childBlocks?.dropLast(1)?.isNotEmpty() == true || lastGroup is SqlElConditionLoopCommentBlock
        }
        return lastGroup?.childBlocks?.firstOrNull() != this
    }

    /**
     * If the element directly under the directive is [SqlKeywordGroupBlock] or [SqlSubGroupBlock],
     * make it the parent and align the indentation with the group directly under it.
     *
     * If the element immediately below the directive is not [SqlKeywordGroupBlock] or [SqlSubGroupBlock],
     * align it to the previous group indent.
     */
    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (conditionType.isEnd() || conditionType.isElse()) {
                return parent.indent.indentLen
            }
            val openConditionLoopDirectiveCount = getOpenDirectiveCount(parent)
            when (parent) {
                is SqlSubGroupBlock -> return calculateSubGroupBlockIndent(parent, openConditionLoopDirectiveCount)

                is SqlElConditionLoopCommentBlock -> {
                    if (conditionType.isEnd()) {
                        parent.conditionEnd = this
                        conditionStart = parent
                        return parent.indent.indentLen
                    } else if (conditionType.isElse()) {
                        return parent.indent.indentLen
                    } else {
                        return parent.indent.indentLen.plus(DIRECTIVE_INDENT_STEP)
                    }
                }

                is SqlKeywordGroupBlock -> {
                    // At this point, it's not possible to determine whether the parent keyword group appears before or after this block based solely on the parent-child relationship.
                    // Therefore, determine the position directly using the text offset.
                    if (isBeforeParentBlock()) {
                        return parent.indent.indentLen + openConditionLoopDirectiveCount * DIRECTIVE_INDENT_STEP
                    }
                    getLastBlockHasConditionLoopDirective()?.let { lastBlock ->
                        if (lastBlock.conditionEnd != null) {
                            return lastBlock.indent.indentLen
                        }
                    }
                    return parent.indent.groupIndentLen +
                        openConditionLoopDirectiveCount * DIRECTIVE_INDENT_STEP +
                        if (parent !is SqlWithQueryGroupBlock) 1 else 0
                }
                else -> return parent.indent.indentLen.plus(openConditionLoopDirectiveCount * DIRECTIVE_INDENT_STEP)
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
    private fun getOpenDirectiveCount(parent: SqlBlock): Int {
        val conditionLoopDirectives: List<SqlElConditionLoopCommentBlock> =
            parent
                .childBlocks
                .filterIsInstance<SqlElConditionLoopCommentBlock>()
                .filter { it.conditionEnd == null }
        val startDirectives =
            conditionLoopDirectives.count { it.conditionType.isStartDirective() }
        val endDirectives = conditionLoopDirectives.count { it.conditionType.isEnd() }
        val diffCount = startDirectives.minus(endDirectives)
        return if (diffCount > 0) diffCount.minus(1) else 0
    }

    /**
     * Determine if this conditional loop directive block is positioned before its parent block.
     */
    fun isBeforeParentBlock(): Boolean {
        parentBlock?.let { parent ->
            return parent.node.startOffset > node.startOffset
        }
        return false
    }

    fun checkConditionLoopDirectiveParentBlock(block: SqlBlock): Boolean = isBeforeParentBlock() && parentBlock == block

    private fun calculateSubGroupBlockIndent(
        parent: SqlSubGroupBlock,
        openDirectiveCount: Int,
    ): Int {
        val parentGroupIndentLen = parent.indent.groupIndentLen
        val grand = parent.parentBlock

        grand?.let { grandParent ->
            when (grandParent) {
                is SqlCreateKeywordGroupBlock -> {
                    val grandIndentLen = grandParent.indent.groupIndentLen
                    return grandIndentLen.plus(parentGroupIndentLen).minus(DEFAULT_INDENT_OFFSET)
                }
                is SqlInsertQueryGroupBlock -> return parentGroupIndentLen
                is SqlColumnRawGroupBlock -> {
                    val grandIndentLen = grandParent.indent.groupIndentLen
                    val prevTextLen = calculatePreviousTextLength(parent)
                    return grandIndentLen.plus(prevTextLen)
                }
            }
        }

        return if (shouldNotIndent(parent)) {
            parentGroupIndentLen.plus(openDirectiveCount * DIRECTIVE_INDENT_STEP)
        } else {
            parentGroupIndentLen.plus(openDirectiveCount * DIRECTIVE_INDENT_STEP).plus(DEFAULT_INDENT_OFFSET)
        }
    }

    private fun calculatePreviousTextLength(parent: SqlSubGroupBlock): Int {
        var prevTextLen = DEFAULT_INDENT_OFFSET
        parent.prevChildren?.dropLast(1)?.forEach { prev ->
            prevTextLen = prevTextLen.plus(prev.getNodeText().length)
        }
        return prevTextLen
    }

    private fun shouldNotIndent(parent: SqlSubGroupBlock): Boolean =
        TypeUtil.isExpectedClassType(SqlRightPatternBlock.NOT_INDENT_EXPECTED_TYPES, parent) ||
            parent is SqlWithCommonTableGroupBlock
}
