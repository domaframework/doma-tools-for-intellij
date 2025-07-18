package org.domaframework.doma.intellij.formatter.util

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.column.SqlColumnRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock

object CommaRawUtil {
    fun getCommaBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock =
        if (lastGroup is SqlElConditionLoopCommentBlock) {
            if (lastGroup.parentBlock == null) {
                getSqlCommaBlock(lastGroup.tempParentBlock, child, sqlBlockFormattingCtx)
            } else {
                getSqlCommaBlock(lastGroup.parentBlock, child, sqlBlockFormattingCtx)
            }
        } else {
            getSqlCommaBlock(lastGroup, child, sqlBlockFormattingCtx)
        }

    private fun getSqlCommaBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock =
        when (lastGroup) {
            is SqlColumnRawGroupBlock, is SqlKeywordGroupBlock -> {
                if (lastGroup.indent.indentLevel == IndentType.SECOND) {
                    SqlCommaBlock(child, sqlBlockFormattingCtx)
                } else {
                    SqlColumnRawGroupBlock(child, sqlBlockFormattingCtx)
                }
            }

            is SqlWithCommonTableGroupBlock -> SqlWithCommonTableGroupBlock(child, sqlBlockFormattingCtx)

            else -> SqlCommaBlock(child, sqlBlockFormattingCtx)
        }
}
