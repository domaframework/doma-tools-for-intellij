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
import org.domaframework.doma.intellij.formatter.block.comma.SqlCommaBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictClauseBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlConflictExpressionSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.condition.SqlConditionKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.option.SqlInGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlReturningGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlWhereGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlConditionalExpressionGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlParallelListBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlValuesParamGroupBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlAliasBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlTableBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlWordBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

object NotQueryGroupHandler {
    private const val RETURNING_KEYWORD = "returning"

    /**
     * Creates an appropriate [org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock] based on the last group context and node type.
     * Returns null if no specific subgroup is needed.
     */
    fun getSubGroup(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock? =
        when {
            hasInKeyword(lastGroup) -> SqlParallelListBlock(child, sqlBlockFormattingCtx)
            lastGroupParentConditionKeywordGroup(lastGroup) -> createConditionalExpressionGroup(child, sqlBlockFormattingCtx)
            hasFunctionOrAliasContext(lastGroup) -> createFunctionOrValueBlock(lastGroup, child, sqlBlockFormattingCtx)
            lastGroup is SqlConflictClauseBlock -> SqlConflictExpressionSubGroupBlock(child, sqlBlockFormattingCtx)
            hasValuesContext(lastGroup) -> SqlValuesParamGroupBlock(child, sqlBlockFormattingCtx)
            hasParameterKeyword(lastGroup) -> SqlFunctionParamBlock(child, sqlBlockFormattingCtx)
            else -> null
        }

    private fun lastGroupParentConditionKeywordGroup(lastGroup: SqlBlock?): Boolean =
        lastGroup is SqlConditionKeywordGroupBlock ||
            lastGroup is SqlWhereGroupBlock

    /**
     * Creates a keyword group block for specific keywords.
     * Currently only handles 'returning' keyword.
     */
    fun getKeywordGroup(
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock? {
        val keyword = child.text.lowercase()
        return when (keyword) {
            RETURNING_KEYWORD -> SqlReturningGroupBlock(child, sqlBlockFormattingCtx)
            else -> null
        }
    }

    /**
     * Checks if the last group has an 'IN' keyword as its last option keyword.
     */
    private fun hasInKeyword(lastGroup: SqlBlock?): Boolean = lastGroup is SqlInGroupBlock

    /**
     * Creates a conditional expression group block.
     */
    private fun createConditionalExpressionGroup(
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlConditionalExpressionGroupBlock = SqlConditionalExpressionGroupBlock(child, sqlBlockFormattingCtx)

    /**
     * Checks if the last group has a word block context that requires function or alias handling.
     */
    private fun hasFunctionOrAliasContext(lastGroup: SqlBlock?): Boolean {
        val lastChild = lastGroup?.childBlocks?.lastOrNull()
        if (lastChild != null) {
            return lastGroup.childBlocks.lastOrNull() is SqlWordBlock
        }
        return false
    }

    private fun hasParameterKeyword(lastGroup: SqlBlock?): Boolean = SqlKeywordUtil.hasFilterParam(lastGroup?.getNodeText() ?: "")

    /**
     * Creates either a function parameter block or values parameter block based on the previous child type.
     */
    private fun createFunctionOrValueBlock(
        lastGroup: SqlBlock?,
        child: ASTNode,
        sqlBlockFormattingCtx: SqlBlockFormattingContext,
    ): SqlBlock {
        val prevChild = lastGroup?.childBlocks?.lastOrNull()
        return when {
            prevChild is SqlAliasBlock || prevChild is SqlTableBlock ->
                SqlValuesParamGroupBlock(child, sqlBlockFormattingCtx)
            else ->
                SqlFunctionParamBlock(child, sqlBlockFormattingCtx)
        }
    }

    /**
     * Checks if the context indicates a values parameter group should be created.
     */
    private fun hasValuesContext(lastGroup: SqlBlock?): Boolean =
        lastGroup is SqlValuesGroupBlock ||
            (lastGroup is SqlCommaBlock && lastGroup.parentBlock is SqlValuesGroupBlock)
}
