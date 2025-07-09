package org.domaframework.doma.intellij.formatter.block.group.keyword.top

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 *  Join Queries Keyword Group Block
 *  [UNION, INTERSECT, EXCEPT]
 */
class SqlJoinQueriesGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(node, IndentType.TOP, context) {
    // TODO Customize offset
    val offset = 0

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlWithQuerySubGroupBlock) {
                return parent.indent.groupIndentLen
            }
            return parent.indent.groupIndentLen.plus(1)
        }
        return offset
    }

    override fun createGroupIndentLen(): Int =
        topKeywordBlocks
            .sumOf { it.getNodeText().length.plus(1) }
            .plus(indent.indentLen)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true
}
