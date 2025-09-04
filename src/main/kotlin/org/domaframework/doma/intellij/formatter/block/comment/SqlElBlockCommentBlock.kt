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
import com.intellij.psi.PsiElement
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlOperationBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElFieldAccessBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElFunctionCallBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElStaticFieldAccessBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlElElseifDirective
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIfDirective
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlElBlockCommentBlock(
    node: ASTNode,
    protected open val context: SqlBlockFormattingContext,
    open val customSpacingBuilder: SqlCustomSpacingBuilder?,
) : SqlCommentBlock(node, context) {
    enum class SqlElCommentDirectiveType {
        CONDITION_LOOP,
        EXPAND,
        POPULATE,
        LITERAL,
        EMBEDDED,
        NORMAL,
    }

    val directiveType: SqlElCommentDirectiveType = initDirectiveType()

    private fun initDirectiveType(): SqlElCommentDirectiveType {
        val element = node.psi
        val contentElement = PsiTreeUtil.firstChild(element).nextSibling

        return when {
            isConditionOrLoopDirective(contentElement) -> SqlElCommentDirectiveType.CONDITION_LOOP
            contentElement.elementType == SqlTypes.HASH -> SqlElCommentDirectiveType.EMBEDDED
            contentElement.elementType == SqlTypes.EL_EXPAND -> SqlElCommentDirectiveType.EXPAND
            contentElement.elementType == SqlTypes.EL_POPULATE -> SqlElCommentDirectiveType.POPULATE
            contentElement.elementType == SqlTypes.CARET -> SqlElCommentDirectiveType.LITERAL
            else -> SqlElCommentDirectiveType.NORMAL
        }
    }

    private fun isConditionOrLoopDirective(contentElement: PsiElement?): Boolean =
        contentElement is SqlElIfDirective ||
            contentElement is SqlElForDirective ||
            contentElement is SqlElElseifDirective ||
            contentElement.elementType == SqlTypes.EL_ELSE ||
            contentElement.elementType == SqlTypes.EL_END

    override fun buildChildren(): MutableList<AbstractBlock> = buildChildBlocks { getBlock(it) }

    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.GE, SqlTypes.LE, SqlTypes.GT, SqlTypes.LT, SqlTypes.EL_EQ, SqlTypes.EL_NE,
            SqlTypes.PLUS, SqlTypes.MINUS, SqlTypes.ASTERISK, SqlTypes.SLASH, SqlTypes.AT_SIGN,
            ->
                SqlOperationBlock(child, context)

            SqlTypes.EL_EQ_EXPR, SqlTypes.EL_NE_EXPR, SqlTypes.EL_GE_EXPR, SqlTypes.EL_GT_EXPR,
            SqlTypes.EL_LE_EXPR, SqlTypes.EL_LT_EXPR, SqlTypes.EL_AND_EXPR, SqlTypes.EL_OR_EXPR,
            SqlTypes.EL_NOT_EXPR, SqlTypes.EL_ADD_EXPR, SqlTypes.EL_SUBTRACT_EXPR,
            SqlTypes.EL_MULTIPLY_EXPR, SqlTypes.EL_DIVIDE_EXPR, SqlTypes.EL_MOD_EXPR,
            ->
                SqlElBlockCommentBlock(child, context, createBlockDirectiveCommentSpacingBuilder())

            SqlTypes.EL_FIELD_ACCESS_EXPR -> SqlElFieldAccessBlock(child, context)
            SqlTypes.BLOCK_COMMENT_START -> SqlCommentStartBlock(child, context)
            SqlTypes.BLOCK_COMMENT_END -> SqlCommentEndBlock(child, context)
            SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR -> SqlElStaticFieldAccessBlock(child, context)
            SqlTypes.EL_FUNCTION_CALL_EXPR -> SqlElFunctionCallBlock(child, context)
            SqlTypes.BLOCK_COMMENT_CONTENT -> SqlBlockCommentBlock(child, context)
            else -> SqlUnknownBlock(child, context)
        }

    protected fun createBlockCommentSpacingBuilder(): SqlCustomSpacingBuilder {
        val noSpacing = Spacing.createSpacing(0, 0, 0, false, 0)
        val singleSpace = Spacing.createSpacing(1, 1, 0, false, 0)

        return SqlCustomSpacingBuilder()
            .withSpacing(SqlTypes.BLOCK_COMMENT_START, SqlTypes.BLOCK_COMMENT_CONTENT, singleSpace)
            .withSpacing(SqlTypes.BLOCK_COMMENT_START, SqlTypes.EL_PARSER_LEVEL_COMMENT, noSpacing)
            .withSpacing(SqlTypes.EL_PARSER_LEVEL_COMMENT, SqlTypes.BLOCK_COMMENT_CONTENT, noSpacing)
    }

    override fun createBlockDirectiveCommentSpacingBuilder(): SqlCustomSpacingBuilder = createBlockCommentSpacingBuilder()

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? =
        customSpacingBuilder?.getCustomSpacing(child1, child2)
            ?: spacingBuilder.getSpacing(this, child1, child2)

    override fun isLeaf(): Boolean = false

    override fun createBlockIndentLen(): Int =
        parentBlock?.let { parent ->
            when (parent) {
                is SqlSubQueryGroupBlock -> calculateSubQueryIndent(parent)
                is SqlValuesGroupBlock -> parent.indent.indentLen
                is SqlKeywordGroupBlock -> parent.indent.groupIndentLen + 1
                else -> parent.indent.groupIndentLen
            }
        } ?: 0

    private fun calculateSubQueryIndent(parent: SqlSubQueryGroupBlock): Int =
        when {
            parent.getChildBlocksDropLast().isEmpty() -> parent.indent.groupIndentLen
            parent.isFirstLineComment -> parent.indent.groupIndentLen - 2
            else -> parent.indent.groupIndentLen
        }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean =
        parentBlock?.let { parent ->
            isParentWithOnlyConditionLoopBlocks(parent)
        } == true

    private fun isParentWithOnlyConditionLoopBlocks(parent: SqlBlock): Boolean =
        (parent is SqlWithQuerySubGroupBlock || parent is SqlValuesGroupBlock || parent is SqlElConditionLoopCommentBlock) &&
            parent.childBlocks.dropLast(1).none { it !is SqlElConditionLoopCommentBlock }
}
