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
package org.domaframework.doma.intellij.formatter.block.group

import com.intellij.formatting.Alignment
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlElSymbolBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlOperationBlock
import org.domaframework.doma.intellij.formatter.block.SqlOtherBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.SqlWordBlock
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlConditionGroupBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    private val parentGroupNode: SqlBlock?,
    spacingBuilder: SpacingBuilder,
) : SqlGroupBlock(
        node,
        wrap,
        alignment,
        parentGroupNode,
        spacingBuilder,
    ) {
    val someLevelKeyword = listOf("and", "or")

    override fun isLoopContinuation(child: ASTNode): Boolean =
        !someLevelKeyword.contains(child.text) && child.elementType != SqlTypes.RIGHT_PAREN

    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.LEFT_PAREN -> {
                SqlSubGroupBlock(
                    child,
                    wrap,
                    alignment,
                    this,
                    spacingBuilder,
                )
            }

            SqlTypes.KEYWORD -> {
                when (child.text.lowercase()) {
                    in someLevelKeyword ->
                        SqlConditionGroupBlock(
                            child,
                            wrap,
                            alignment,
                            this,
                            spacingBuilder,
                        )

                    else -> SqlKeywordBlock(child, wrap, alignment, spacingBuilder)
                }
            }

            SqlTypes.WORD -> {
                SqlWordBlock(child, wrap, alignment, spacingBuilder)
            }

            SqlTypes.OTHER -> {
                SqlOtherBlock(child, wrap, alignment, spacingBuilder)
            }

            SqlTypes.DOT -> {
                SqlElSymbolBlock(child, wrap, alignment, spacingBuilder)
            }

            SqlTypes.PLUS, SqlTypes.MINUS, SqlTypes.PERCENT, SqlTypes.SLASH, SqlTypes.ASTERISK -> {
                SqlOperationBlock(child, wrap, alignment, spacingBuilder)
            }

            else -> {
                SqlUnknownBlock(
                    child,
                    wrap,
                    alignment,
                    spacingBuilder,
                )
            }
        }

    override fun getIndentCount(
        parentIndent: Int,
        parentTextLen: Int,
    ): Int = parentIndent + (parentTextLen - this.node.text.length)
}
