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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlReturningGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock
import org.domaframework.doma.intellij.psi.SqlTypes

object NotQueryGroupUtil {
    fun getSubGroup(
        lastGroup: SqlBlock,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock? {
        val lastKeyword =
            lastGroup.childBlocks
                .lastOrNull { SqlKeywordUtil.isOptionSqlKeyword(it.getNodeText()) }
        if (lastKeyword != null && lastKeyword.getNodeText().lowercase() == "in") {
            return SqlParallelListBlock(child, sqlBlockFormattingCtx)
        }

        if (PsiTreeUtil.prevLeaf(child.psi)?.elementType == SqlTypes.WORD) {
            return SqlFunctionParamBlock(child, sqlBlockFormattingCtx)
        }

        if (lastGroup is SqlConditionKeywordGroupBlock) {
            return SqlConditionalExpressionGroupBlock(
                child,
                sqlBlockFormattingCtx,
            )
        }

        return null
    }

    fun getKeywordGroup(
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock? {
        val keyword = child.text.lowercase()
        if (keyword == "returning") {
            return SqlReturningGroupBlock(child, sqlBlockFormattingCtx)
        }
        return null
    }
}
