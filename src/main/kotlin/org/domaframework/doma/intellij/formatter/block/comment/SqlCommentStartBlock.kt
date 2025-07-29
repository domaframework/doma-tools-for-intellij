package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlCommentStartBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlCommentSeparateBlock(node, context) {
    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = false
}
