package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlCommentContentBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlCommentBlock(node, context) {
    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = false

    override fun createBlockIndentLen(): Int = parentBlock?.indent?.indentLen ?: 1

    override fun createGroupIndentLen(): Int = indent.indentLen
}
