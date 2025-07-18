/*
 * Copyright Doma Tools Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
