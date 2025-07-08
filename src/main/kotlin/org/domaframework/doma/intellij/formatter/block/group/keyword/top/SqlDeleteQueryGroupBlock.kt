package org.domaframework.doma.intellij.formatter.block.group.keyword.top

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlDeleteQueryGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlTopQueryGroupBlock(node, context)
