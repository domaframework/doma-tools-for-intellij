package org.domaframework.doma.intellij.formatter.block.group.keyword.insert

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlInsertValueGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.groupIndentLen = createBlockIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlKeywordGroupBlock && lastGroup.getNodeText() == "values") {
            (lastGroup as? SqlInsertQueryGroupBlock)?.valueGroupBlock = this
        }
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            return parent.indent.groupIndentLen.plus(1)
            return 1
        } ?: return 1
    }
}
