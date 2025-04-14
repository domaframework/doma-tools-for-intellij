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
package org.domaframework.doma.intellij.formatter.block.expr

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.SqlOperationBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlInsertKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElConditionLoopCommentBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    override val customSpacingBuilder: SqlCustomSpacingBuilder?,
    spacingBuilder: SpacingBuilder,
) : SqlElBlockCommentBlock(
        node,
        wrap,
        alignment,
        customSpacingBuilder,
        spacingBuilder,
    ) {
    enum class SqlConditionLoopCommentBlockType {
        CONDITION,
        LOOP,
        END,
        UNKNOWN,
        ;

        fun isEnd(): Boolean = this == END

        fun isInvalid(): Boolean = this == UNKNOWN
    }

    val conditionType: SqlConditionLoopCommentBlockType = initConditionOrLoopType(node)

    private fun initConditionOrLoopType(node: ASTNode): SqlConditionLoopCommentBlockType {
        val psi = node.psi
        if (psi is SqlCustomElCommentExpr && psi.isConditionOrLoopDirective()) {
            if (PsiTreeUtil.getChildOfType(psi, SqlElForDirective::class.java) != null) {
                return SqlConditionLoopCommentBlockType.LOOP
            }
            if (PsiTreeUtil.getChildOfType(psi, SqlElIfDirective::class.java) != null ||
                psi.findElementAt(2)?.elementType == SqlTypes.EL_ELSE
            ) {
                return SqlConditionLoopCommentBlockType.CONDITION
            }
            if (psi.findElementAt(2)?.elementType == SqlTypes.EL_END) {
                return SqlConditionLoopCommentBlockType.END
            }
        }
        return SqlConditionLoopCommentBlockType.UNKNOWN
    }

    override val indent =
        ElementIndent(
            IndentType.NONE,
            0,
            0,
        )

    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLevel = IndentType.NONE
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = 0
        println("setParentGroupBlock:${block?.getNodeText()}")
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
                SqlOperationBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.EL_FIELD_ACCESS_EXPR ->
                SqlElFieldAccessBlock(
                    child,
                    wrap,
                    alignment,
                    createFieldAccessSpacingBuilder(),
                    spacingBuilder,
                )

            SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR ->
                SqlElStaticFieldAccessBlock(
                    child,
                    wrap,
                    alignment,
                    createStaticFieldSpacingBuilder(),
                    spacingBuilder,
                )

            SqlTypes.EL_FUNCTION_CALL_EXPR ->
                SqlElFunctionCallBlock(
                    child,
                    wrap,
                    alignment,
                    createSpacingBuilder(),
                    spacingBuilder,
                )

            SqlTypes.BLOCK_COMMENT_CONTENT ->
                SqlBlockCommentBlock(child, wrap, alignment, spacingBuilder)

            else -> SqlUnknownBlock(child, wrap, alignment, spacingBuilder)
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

    private fun createStaticFieldSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.AT_SIGN,
                SqlTypes.EL_CLASS,
                Spacing.createSpacing(0, 0, 0, false, 0),
            ).withSpacing(
                SqlTypes.AT_SIGN,
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

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            when (parent) {
                is SqlSubGroupBlock -> {
                    val parentGroupIndentLen = parent.indent.groupIndentLen
                    val grand = parent.parentBlock
                    grand?.let { grand ->
                        if (grand is SqlCreateKeywordGroupBlock) {
                            val grandIndentLen = grand.indent.groupIndentLen
                            return grandIndentLen.plus(parentGroupIndentLen).minus(1)
                        }
                        if (grand is SqlInsertKeywordGroupBlock) {
                            return parentGroupIndentLen
                        }
                        if (grand is SqlColumnGroupBlock) {
                            val grandIndentLen = grand.indent.groupIndentLen
                            var prevTextLen = 1
                            parent.prevChildren?.dropLast(1)?.forEach { prev ->
                                prevTextLen = prevTextLen.plus(prev.getNodeText().length)
                            }
                            return grandIndentLen.plus(prevTextLen).plus(1)
                        }
                    }
                    return parentGroupIndentLen
                }

                is SqlKeywordGroupBlock, is SqlCommaBlock -> {
                    return parent.indent.indentLen
                }

                // Parent of End Block is SqlElConditionLoopCommentBlock
                is SqlElConditionLoopCommentBlock -> {
                    return parent.indent.indentLen
                }
            }
        }
        return 1
    }
}
