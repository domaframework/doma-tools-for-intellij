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
import org.domaframework.doma.intellij.formatter.block.SqlOperationBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElFieldAccessBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElFunctionCallBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElStaticFieldAccessBlock
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
                child.indent.indentLen = indent.indentLen.plus(2)
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
            SqlTypes.GE, SqlTypes.LE, SqlTypes.GT, SqlTypes.LT, SqlTypes.EL_EQ, SqlTypes.EL_NE,
            SqlTypes.PLUS, SqlTypes.MINUS, SqlTypes.ASTERISK, SqlTypes.SLASH, SqlTypes.AT_SIGN,
            ->
                SqlOperationBlock(child, context)

            SqlTypes.EL_FIELD_ACCESS_EXPR ->
                SqlElFieldAccessBlock(
                    child,
                    context,
                    createFieldAccessSpacingBuilder(),
                )

            SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR ->
                SqlElStaticFieldAccessBlock(
                    child,
                    context,
                )

            SqlTypes.EL_FUNCTION_CALL_EXPR ->
                SqlElFunctionCallBlock(
                    child,
                    context,
                )

            else -> SqlUnknownBlock(child, context)
        }

    private fun createFieldAccessSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.EL_PRIMARY_EXPR,
                SqlTypes.DOT,
                Spacing.createSpacing(0, 0, 0, false, 0),
            ).withSpacing(
                SqlTypes.DOT,
                SqlTypes.EL_IDENTIFIER,
                Spacing.createSpacing(0, 0, 0, false, 0),
            ).withSpacing(
                SqlTypes.EL_IDENTIFIER,
                SqlTypes.DOT,
                Spacing.createSpacing(0, 0, 0, false, 0),
            ).withSpacing(
                SqlTypes.EL_IDENTIFIER,
                SqlTypes.EL_PARAMETERS,
                Spacing.createSpacing(0, 0, 0, false, 0),
            )

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
                is SqlSubGroupBlock -> {
                    val parentGroupIndentLen = parent.indent.groupIndentLen
                    val grand = parent.parentBlock
                    grand?.let { grand ->
                        if (grand is SqlCreateKeywordGroupBlock) {
                            val grandIndentLen = grand.indent.groupIndentLen
                            return grandIndentLen.plus(parentGroupIndentLen).minus(1)
                        }
                        if (grand is SqlInsertQueryGroupBlock) {
                            return parentGroupIndentLen
                        }
                        if (grand is SqlColumnRawGroupBlock) {
                            val grandIndentLen = grand.indent.groupIndentLen
                            var prevTextLen = 1
                            parent.prevChildren?.dropLast(1)?.forEach { prev ->
                                prevTextLen = prevTextLen.plus(prev.getNodeText().length)
                            }
                            return grandIndentLen.plus(prevTextLen)
                        }
                    }
                    return if (TypeUtil.isExpectedClassType(
                            SqlRightPatternBlock.NOT_INDENT_EXPECTED_TYPES,
                            parent,
                        ) ||
                        parent is SqlWithCommonTableGroupBlock
                    ) {
                        parentGroupIndentLen.plus(openConditionLoopDirectiveCount * 2)
                    } else {
                        parentGroupIndentLen.plus(openConditionLoopDirectiveCount * 2).plus(1)
                    }
                }

                is SqlElConditionLoopCommentBlock -> {
                    if (conditionType.isEnd()) {
                        parent.conditionEnd = this
                        conditionStart = parent
                        return parent.indent.indentLen
                    } else if (conditionType.isElse()) {
                        return parent.indent.indentLen
                    } else {
                        return parent.indent.indentLen.plus(2)
                    }
                }

                is SqlKeywordGroupBlock -> {
                    // At this point, it's not possible to determine whether the parent keyword group appears before or after this block based solely on the parent-child relationship.
                    // Therefore, determine the position directly using the text offset.
                    return if (parent.node.startOffset <
                        node.startOffset
                    ) {
                        // The child branch applies in cases where a conditional directive is included as a child of this block.
                        val questOffset = if (parent is SqlWithQueryGroupBlock) 0 else 1
                        parent.indent.groupIndentLen
                            .plus(openConditionLoopDirectiveCount * 2)
                            .plus(questOffset)
                    } else {
                        parent.indent.indentLen.plus(openConditionLoopDirectiveCount * 2)
                    }
                }
                else -> return parent.indent.indentLen.plus(openConditionLoopDirectiveCount * 2)
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
}
