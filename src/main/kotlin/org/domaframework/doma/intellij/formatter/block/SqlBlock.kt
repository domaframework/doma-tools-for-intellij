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
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlDefaultCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil
import org.domaframework.doma.intellij.psi.SqlTypes

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
         * Returns `0` if there is no line break.
         */
        var indentLen: Int,
        /**
         * Indentation baseline applied to the group itself.
         * Even if the group does not start on a new line,
         * it determines and applies indentation to the group based on factors such as the number of preceding characters.
         */
        var groupIndentLen: Int,
    )

    open var parentBlock: SqlBlock? = null
    open val childBlocks = mutableListOf<SqlBlock>()
    open var prevBlocks = emptyList<SqlBlock>()

    fun getChildrenTextLen(): Int =
        childBlocks.sumOf { child ->
            val children =
                child.childBlocks.filter { it !is SqlLineCommentBlock && it !is SqlBlockCommentBlock }
            if (children.isNotEmpty()) {
                child
                    .getChildrenTextLen()
                    .plus(child.getNodeText().length)
            } else if (child.node.elementType == SqlTypes.DOT ||
                child.node.elementType == SqlTypes.RIGHT_PAREN
            ) {
                // Since elements do not include surrounding spaces, they should be excluded from the character count.
                0
            } else {
                child.getNodeText().length.plus(1)
            }
        }

    fun getChildBlocksDropLast(
        dropIndex: Int = 1,
        skipCommentBlock: Boolean = true,
    ): List<SqlBlock> {
        val children = childBlocks.dropLast(dropIndex)
        if (skipCommentBlock) {
            return children.filter { it !is SqlLineCommentBlock && it !is SqlBlockCommentBlock }
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

    open fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        parentBlock?.let { parent ->
            if (parent is SqlElConditionLoopCommentBlock) {
                val prevBlock =
                    prevBlocks.lastOrNull { it !is SqlDefaultCommentBlock }
                return prevBlock is SqlElConditionLoopCommentBlock &&
                    (prevBlock.conditionType.isElse() || prevBlock.conditionType.isEnd()) ||
                    parent.childBlocks.dropLast(1).isEmpty()
            }
            if (parent is SqlNewGroupBlock) {
                val prevWord = prevBlocks.lastOrNull { it !is SqlCommentBlock }
                if (SqlKeywordUtil.isSetLineKeyword(getNodeText(), parent.getNodeText()) ||
                    SqlKeywordUtil.isSetLineKeyword(getNodeText(), prevWord?.getNodeText() ?: "")
                ) {
                    return false
                }
                return childBlocks.lastOrNull() is SqlElConditionLoopCommentBlock ||
                    prevBlocks.lastOrNull() is SqlElConditionLoopCommentBlock
            }
        }
        return false
    }

    /**
     * Creates the indentation length for the block.
     */
    open fun createBlockIndentLen(): Int = 0

    open fun createGroupIndentLen(): Int = 0

    open fun getBlock(child: ASTNode): SqlBlock = this

    /**
     * Creates a spacing builder for custom spacing rules.
     */
    protected open fun createSpacingBuilder(): SqlCustomSpacingBuilder = SqlCustomSpacingBuilder()

    override fun buildChildren(): List<Block?>? = emptyList()

    /**
     * Determines whether to adjust the indentation on pressing Enter.
     */
    fun isAdjustIndentOnEnter(): Boolean = formatMode == FormattingMode.ADJUST_INDENT_ON_ENTER && !isEnableFormat()

    /**
     * Returns the indentation for the block.
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
     * Returns the child indentation for the block.
     */
    override fun getChildIndent(): Indent? =
        if (isEnableFormat()) {
            Indent.getSpaceIndent(4)
        } else {
            Indent.getSpaceIndent(0)
        }

    /**
     * Determines whether the block is a leaf node.
     */
    override fun isLeaf(): Boolean = myNode.firstChildNode == null
}
