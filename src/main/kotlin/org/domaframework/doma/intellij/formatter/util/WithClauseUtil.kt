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
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithColumnGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithOptionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.psi.SqlTypes

object WithClauseUtil {
    fun getWithClauseSubGroup(
        lastGroup: SqlBlock,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock? {
        when (lastGroup) {
            is SqlWithCommonTableGroupBlock -> {
                return if (lastGroup.optionKeywordBlocks.isEmpty()) {
                    when (child.elementType) {
                        SqlTypes.LEFT_PAREN -> SqlWithColumnGroupBlock(child, sqlBlockFormattingCtx)
                        SqlTypes.COMMA -> SqlWithCommonTableGroupBlock(child, sqlBlockFormattingCtx)
                        else -> SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
                    }
                } else {
                    SqlWithQuerySubGroupBlock(child, sqlBlockFormattingCtx)
                }
            }

            is SqlWithQuerySubGroupBlock -> return SqlSubQueryGroupBlock(child, sqlBlockFormattingCtx)
        }
        return null
    }

    fun getWithClauseKeywordGroup(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock? {
        if (SqlKeywordUtil.isWithOptionKeyword(child.text)) {
            return SqlWithOptionGroupBlock(child, sqlBlockFormattingCtx)
        }
        if (lastGroup is SqlWithOptionGroupBlock) {
            when (child.text.lowercase()) {
                "set" -> return SqlKeywordBlock(child, IndentType.ATTACHED, sqlBlockFormattingCtx)
            }
        }
        return null
    }
}
