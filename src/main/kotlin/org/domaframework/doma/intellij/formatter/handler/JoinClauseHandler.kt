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
package org.domaframework.doma.intellij.formatter.handler

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlJoinGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

object JoinClauseHandler {
    fun getJoinKeywordGroupBlock(
        lastGroupBlock: SqlBlock?,
        keywordText: String,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock =
        if (SqlKeywordUtil.Companion.isJoinKeyword(keywordText)) {
            SqlJoinGroupBlock(
                child,
                sqlBlockFormattingCtx,
            )
        } else if (lastGroupBlock is SqlJoinGroupBlock) {
            // JOIN_ATTACHED_KEYWORD Keywords
            SqlKeywordBlock(
                child,
                IndentType.ATTACHED,
                sqlBlockFormattingCtx,
            )
        } else {
            // Catch any unregistered JOIN keywords here if they are not already listed in JOIN_KEYWORD.
            SqlJoinGroupBlock(
                child,
                sqlBlockFormattingCtx,
            )
        }
}
