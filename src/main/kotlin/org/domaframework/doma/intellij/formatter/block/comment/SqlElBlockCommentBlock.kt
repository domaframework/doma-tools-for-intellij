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
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlOperationBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElFieldAccessBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElFunctionCallBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElStaticFieldAccessBlock
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
    private val context: SqlBlockFormattingContext,
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
        val element = this.node.psi
        val contentElement = PsiTreeUtil.firstChild(element).nextSibling

        if (contentElement is SqlElIfDirective ||
            contentElement is SqlElForDirective ||
            contentElement is SqlElElseifDirective ||
            contentElement.elementType == SqlTypes.EL_ELSE ||
            contentElement.elementType == SqlTypes.EL_END
        ) {
            return SqlElCommentDirectiveType.CONDITION_LOOP
        }
        if (contentElement.elementType == SqlTypes.HASH) {
            return SqlElCommentDirectiveType.EMBEDDED
        }
        if (contentElement.elementType == SqlTypes.EL_EXPAND) {
            return SqlElCommentDirectiveType.EXPAND
        }
        if (contentElement.elementType == SqlTypes.EL_POPULATE) {
            return SqlElCommentDirectiveType.POPULATE
        }
        if (contentElement.elementType == SqlTypes.CARET) {
            return SqlElCommentDirectiveType.LITERAL
        }

        return SqlElCommentDirectiveType.NORMAL
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

            SqlTypes.BLOCK_COMMENT_START -> SqlCommentStartBlock(child, context)

            SqlTypes.BLOCK_COMMENT_END -> SqlCommentEndBlock(child, context)

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

            SqlTypes.BLOCK_COMMENT_CONTENT ->
                SqlBlockCommentBlock(child, createBlockCommentSpacingBuilder(), context)

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

    protected fun createBlockCommentSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.BLOCK_COMMENT_START,
                SqlTypes.BLOCK_COMMENT_CONTENT,
                Spacing.createSpacing(1, 1, 0, false, 0),
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
            return when (parent) {
                is SqlElConditionLoopCommentBlock -> parent.indent.groupIndentLen
                is SqlSubQueryGroupBlock -> {
                    if (parent.getChildBlocksDropLast().isEmpty()) {
                        if (isConditionLoopDirectiveRegisteredBeforeParent()) {
                            parent.indent.groupIndentLen
                        } else {
                            parent.indent.groupIndentLen
                        }
                    } else if (parent.isFirstLineComment) {
                        parent.indent.groupIndentLen.minus(2)
                    } else {
                        parent.indent.groupIndentLen
                    }
                }
                is SqlValuesGroupBlock -> parent.indent.indentLen
                else -> parent.indent.groupIndentLen
            }
        }
        return 0
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean =
        parentBlock?.let { parent ->
            isConditionLoopDirectiveRegisteredBeforeParent() ||
                (
                    (
                        parent is SqlWithQuerySubGroupBlock ||
                            parent is SqlValuesGroupBlock ||
                            parent is SqlElConditionLoopCommentBlock
                    ) &&
                        parent.childBlocks
                            .dropLast(1)
                            .none { it !is SqlElConditionLoopCommentBlock }
                )
        } == true
}
