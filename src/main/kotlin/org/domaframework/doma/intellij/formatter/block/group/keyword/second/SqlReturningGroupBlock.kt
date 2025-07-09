package org.domaframework.doma.intellij.formatter.block.group.keyword.second

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlReturningGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSecondKeywordBlock(node, context) {
    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            return parent.indent.indentLen
        }
        return 0
    }
}
