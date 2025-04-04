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
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlOperationBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.psi.SqlElElseifDirective
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlTypes

enum class SqlDirectiveType {
    IF,
    ELSEIF,
    ELSE,
    FOR,
    END,
    Variable,
}

class SqlElBlockCommentBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    val customSpacingBuilder: SqlCustomSpacingBuilder?,
    spacingBuilder: SpacingBuilder,
) : SqlCommentBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
    ) {
    var isConditionLoopBlock = getConditionOrLoopBlock(node)

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
    }

    override fun buildChildren(): MutableList<AbstractBlock> {
        val blocks = mutableListOf<AbstractBlock>()
        var child = node.firstChildNode
        while (child != null) {
            if (child !is PsiWhiteSpace) {
                val block = getBlock(child)
                blocks.add(block)
                if (!isConditionLoopBlock &&
                    (
                        child.elementType == SqlTypes.EL_IF_DIRECTIVE ||
                            child.elementType == SqlTypes.EL_FOR_DIRECTIVE ||
                            child.elementType == SqlTypes.EL_ELSEIF_DIRECTIVE ||
                            child.elementType == SqlTypes.EL_ELSE ||
                            child.elementType == SqlTypes.EL_END
                    )
                ) {
                    isConditionLoopBlock = true
                }
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

    private fun getConditionOrLoopBlock(node: ASTNode): Boolean {
        val directiveType =
            when {
                PsiTreeUtil.getChildOfType(node.psi, SqlElIfDirective::class.java) != null -> {
                    SqlDirectiveType.IF
                }

                PsiTreeUtil.getChildOfType(node.psi, SqlElElseifDirective::class.java) != null -> {
                    SqlDirectiveType.ELSEIF
                }

                PsiTreeUtil.getChildOfType(node.psi, SqlElForDirective::class.java) != null -> {
                    SqlDirectiveType.FOR
                }

                PsiTreeUtil.getChildOfType(node.psi, SqlElElseifDirective::class.java) != null -> {
                    SqlDirectiveType.ELSE
                }

                PsiTreeUtil.getChildOfType(node.psi, SqlElForDirective::class.java) != null -> {
                    SqlDirectiveType.END
                }

                else -> {
                    val children =
                        PsiTreeUtil
                            .getChildrenOfType(node.psi, PsiElement::class.java)
                            ?.firstOrNull { it.elementType == SqlTypes.EL_ELSE || it.elementType == SqlTypes.EL_END }
                    children?.let {
                        when (it.elementType) {
                            SqlTypes.EL_ELSE -> SqlDirectiveType.ELSE
                            SqlTypes.EL_END -> SqlDirectiveType.END
                            else -> SqlDirectiveType.Variable
                        }
                    } ?: SqlDirectiveType.Variable
                }
            }

        return directiveType != SqlDirectiveType.Variable
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
        parentBlock?.let {
            if (it is SqlSubQueryGroupBlock) {
                if (it.childBlocks.dropLast(1).isEmpty()) {
                    return 1
                }
                if (it.isFirstLineComment) {
                    return it.indent.groupIndentLen.minus(2)
                }
            }
        }
        return 1
    }
}
