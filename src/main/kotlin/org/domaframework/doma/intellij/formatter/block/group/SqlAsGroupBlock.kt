package org.domaframework.doma.intellij.formatter.block.group

import com.intellij.formatting.Alignment
import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.SqlWhitespaceBlock
import org.domaframework.doma.intellij.formatter.block.SqlWordBlock
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlAsGroupBlock(
    node: ASTNode,
    groupTopNode: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    parentGroupNode: SqlBlock?,
    spacingBuilder: SpacingBuilder,
) : SqlGroupBlock(
        node,
        groupTopNode,
        wrap,
        alignment,
        parentGroupNode,
        spacingBuilder,
    ) {
    override val indentLevel = 5

    override fun buildChildren(): MutableList<AbstractBlock> {
        val topBlock = SqlKeywordBlock(node, wrap, alignment, spacingBuilder)
        blocks.add(topBlock)
        var child = groupTopNode.treeNext
        var nonWhiteSpaceChild = topBlock
        while (child != null) {
            if (child is PsiWhiteSpace && !blockSkip) {
                blocks.add(
                    SqlWhitespaceBlock(
                        child,
                        wrap,
                        alignment,
                        nonWhiteSpaceChild,
                        spacingBuilder,
                    ),
                )
            } else {
                val childBlock = getBlock(child)
                when (child) {
                    is SqlWordBlock -> {
                        if (blocks.isNotEmpty()) {
                            (blocks.last() as? SqlWhitespaceBlock)?.nextNode = childBlock
                        }
                        blocks.add(childBlock)
                        break
                    }
                    is SqlBlockCommentBlock, is SqlLineCommentBlock -> blocks.add(childBlock)
                    else -> break
                }
            }
            child = child.treeNext
        }

        println("End As Group: ${blocks.map{it.node.text}}")
        return blocks
    }

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? =
        SqlCustomSpacingBuilder()
            .withSpacing(SqlTypes.KEYWORD, SqlTypes.WORD, Spacing.createSpacing(1, 1, 0, false, 0))
            .getSpacing(this, child1, child2)

    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.KEYWORD -> SqlKeywordBlock(child, wrap, alignment, spacingBuilder)
            SqlTypes.WORD -> SqlWordBlock(child, wrap, alignment, spacingBuilder)
            SqlTypes.BLOCK_COMMENT -> SqlBlockCommentBlock(child, wrap, alignment, spacingBuilder)
            SqlTypes.LINE_COMMENT -> SqlLineCommentBlock(child, wrap, alignment, spacingBuilder)
            else -> SqlUnknownBlock(child, wrap, alignment, spacingBuilder)
        }
}
