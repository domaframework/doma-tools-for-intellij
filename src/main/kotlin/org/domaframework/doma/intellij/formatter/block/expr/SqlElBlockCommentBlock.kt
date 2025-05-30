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
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.IndentType
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlOperationBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlElBlockCommentBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    open val customSpacingBuilder: SqlCustomSpacingBuilder?,
    spacingBuilder: SpacingBuilder,
    enableFormat: Boolean,
    private val formatMode: FormattingMode,
) : SqlCommentBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
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
                SqlOperationBlock(child, wrap, alignment, spacingBuilder, isEnableFormat(), formatMode)

            SqlTypes.EL_FIELD_ACCESS_EXPR ->
                SqlElFieldAccessBlock(
                    child,
                    wrap,
                    alignment,
                    createFieldAccessSpacingBuilder(),
                    spacingBuilder,
                    isEnableFormat(),
                    formatMode,
                )

            SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR ->
                SqlElStaticFieldAccessBlock(
                    child,
                    wrap,
                    alignment,
                    createStaticFieldSpacingBuilder(),
                    spacingBuilder,
                    isEnableFormat(),
                    formatMode,
                )

            SqlTypes.EL_FUNCTION_CALL_EXPR ->
                SqlElFunctionCallBlock(
                    child,
                    wrap,
                    alignment,
                    createSpacingBuilder(),
                    spacingBuilder,
                    isEnableFormat(),
                    formatMode,
                )

            SqlTypes.BLOCK_COMMENT_CONTENT ->
                SqlBlockCommentBlock(child, wrap, alignment, spacingBuilder, isEnableFormat(), formatMode)

            else -> SqlUnknownBlock(child, wrap, alignment, spacingBuilder, isEnableFormat(), formatMode)
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
