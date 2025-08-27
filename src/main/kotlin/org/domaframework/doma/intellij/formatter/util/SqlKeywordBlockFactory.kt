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
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.OnConflictKeywordType
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictClauseBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateTableColumnDefinitionRawGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlExistsGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlInGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlLateralGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlWhereGroupBlock

/**
 * Factory class for creating SQL keyword blocks based on indent type and context
 */
class SqlKeywordBlockFactory(
    private val sqlBlockFormattingCtx: SqlBlockFormattingContext,
) {
    fun createInlineBlock(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        if (!SqlKeywordUtil.isSetLineKeyword(
                child.text,
                lastGroupBlock?.getNodeText() ?: "",
            )
        ) {
            SqlInlineGroupBlock(child, sqlBlockFormattingCtx)
        } else {
            SqlKeywordBlock(child, IndentType.INLINE, sqlBlockFormattingCtx)
        }

    fun createAttachedBlock(
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        if (lastGroupBlock is SqlCreateKeywordGroupBlock) {
            lastGroupBlock.setCreateQueryType(child.text)
        }
        return SqlKeywordBlock(child, IndentType.ATTACHED, sqlBlockFormattingCtx)
    }

    fun createOptionsBlock(
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        // Handle AS keyword for CREATE VIEW
        if (keywordText == "as") {
            val parentCreateBlock = findParentCreateBlock(lastGroupBlock)
            if (parentCreateBlock?.createType == CreateQueryType.VIEW) {
                return SqlCreateViewGroupBlock(child, sqlBlockFormattingCtx)
            }
        }

        // Handle LATERAL keyword
        if (keywordText == "lateral") {
            return SqlLateralGroupBlock(child, sqlBlockFormattingCtx)
        }

        // Handle IN keyword
        if (keywordText == "in") {
            return SqlInGroupBlock(child, sqlBlockFormattingCtx)
        }

        // Handle EXISTS/NOT EXISTS keywords
        if (SqlKeywordUtil.isExistsKeyword(keywordText)) {
            return createExistsBlock(keywordText, child, lastGroupBlock)
        }

        return SqlKeywordBlock(child, IndentType.OPTIONS, sqlBlockFormattingCtx)
    }

    fun createConflictBlock(
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock =
        if (lastGroupBlock is SqlConflictClauseBlock) {
            lastGroupBlock.conflictType = OnConflictKeywordType.of(keywordText)
            SqlKeywordBlock(child, IndentType.CONFLICT, sqlBlockFormattingCtx)
        } else {
            SqlConflictClauseBlock(child, sqlBlockFormattingCtx)
        }

    private fun findParentCreateBlock(block: SqlBlock?): SqlCreateKeywordGroupBlock? =
        when (block) {
            is SqlCreateKeywordGroupBlock -> block
            else -> block?.parentBlock as? SqlCreateKeywordGroupBlock
        }

    private fun createExistsBlock(
        keywordText: String,
        child: ASTNode,
        lastGroupBlock: SqlBlock?,
    ): SqlBlock {
        // If already in EXISTS group, just return a keyword block
        if (lastGroupBlock is SqlExistsGroupBlock) {
            return SqlKeywordBlock(child, IndentType.OPTIONS, sqlBlockFormattingCtx)
        }

        // Check for ELSE condition or NOT keyword outside WHERE/condition context
        val shouldCreateExistsGroup =
            when {
                lastGroupBlock is SqlElConditionLoopCommentBlock && lastGroupBlock.conditionType.isElse() -> true
                lastGroupBlock is SqlCreateTableColumnDefinitionGroupBlock ||
                    lastGroupBlock is SqlCreateTableColumnDefinitionRawGroupBlock -> false
                keywordText == "not" && !isInConditionContext(lastGroupBlock) -> true
                else -> false
            }

        return if (shouldCreateExistsGroup) {
            SqlExistsGroupBlock(child, sqlBlockFormattingCtx)
        } else {
            SqlKeywordBlock(child, IndentType.OPTIONS, sqlBlockFormattingCtx)
        }
    }

    private fun isInConditionContext(block: SqlBlock?): Boolean = block is SqlConditionKeywordGroupBlock || block is SqlWhereGroupBlock
}
