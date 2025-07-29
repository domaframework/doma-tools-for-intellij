package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

abstract class SqlCommentSeparateBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlBlock(
        node,
        context.wrap,
        context.alignment,
        context.spacingBuilder,
        context.enableFormat,
        context.formatMode,
    ) {
    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = false

    override fun createBlockIndentLen(): Int = parentBlock?.indent?.indentLen ?: 0

    override fun createGroupIndentLen(): Int = indent.indentLen
}
