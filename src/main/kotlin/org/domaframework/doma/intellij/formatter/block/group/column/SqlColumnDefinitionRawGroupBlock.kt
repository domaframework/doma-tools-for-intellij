package org.domaframework.doma.intellij.formatter.block.group.column

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Column definition group block in the column list group attached to Create Table
 * The parent must be SqlCreateTableColumnDefinitionGroupBlock
 */
open class SqlColumnDefinitionRawGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlRawGroupBlock(
        node,
        context,
    ) {
    // TODO:Customize indentation within an inline group
    open val defaultOffset = 0
    val isFirstColumnRaw = node.elementType != SqlTypes.COMMA

    open var columnBlock: SqlBlock? = if (isFirstColumnRaw) this else null

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    /**
     * Right-justify the longest column name in the column definition.
     */
    override fun createBlockIndentLen(): Int = if (isFirstColumnRaw) 1 else defaultOffset
}
