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
import org.domaframework.doma.intellij.formatter.block.group.SqlSelectGroupBlock

class SqlFromGroupBlock(
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
    override val indentLevel = 2

    override fun isLoopContinuation(child: ASTNode): Boolean {
        var childLowercaseName = child.text.lowercase()
        return !someLevelKeywords.contains(childLowercaseName) &&
            !topLevelKeywords.contains(childLowercaseName) &&
            !subQueryKeywords.contains(childLowercaseName)
    }

/**
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
     return when (child.text.lowercase()) {
     "select" -> {
     SqlSelectGroupBlock(
     child,
     wrap,
     alignment,
     this,
     spacingBuilder,
     )
     }

     "from" -> {
     SqlFromGroupBlock(
     child,
     wrap,
     alignment,
     this,
     spacingBuilder,
     )
     }

     "where" -> {
     SqlWhereGroupBlock(
     child,
     wrap,
     alignment,
     this,
     spacingBuilder,
     )
     }

     "left", "right", "inner", "outer", "join" -> {
     SqlSubQueryGroupBlock(
     child,
     wrap,
     alignment,
     this,
     spacingBuilder,
     )
     }

     else -> SqlKeywordBlock(child, wrap, alignment, spacingBuilder)
     }
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

     override fun getSpacing(
     child1: Block?,
     child2: Block,
     ): Spacing? =
     if (parentGroupNode != null) {
     SqlCustomSpacingBuilder()
     .withSpacing(
     null,
     SqlTypes.KEYWORD,
     Spacing.createSpacing(0, 0, 0, false, 0),
     ).withSpacing(
     SqlTypes.KEYWORD,
     SqlTypes.WORD,
     Spacing.createSpacing(1, 1, 0, false, 0),
     ).withSpacing(
     SqlTypes.KEYWORD,
     SqlTypes.ASTERISK,
     Spacing.createSpacing(1, 1, 0, false, 0),
     ).withSpacing(
     SqlTypes.OTHER,
     SqlTypes.COMMA,
     Spacing.createSpacing(0, 0, 1, false, 0),
     ).withSpacing(
     SqlTypes.ASTERISK,
     SqlTypes.COMMA,
     Spacing.createSpacing(0, 0, 1, false, 0),
     ).withSpacing(
     SqlTypes.WORD,
     SqlTypes.DOT,
     Spacing.createSpacing(0, 0, 0, false, 0),
     ).withSpacing(
     SqlTypes.DOT,
     SqlTypes.OTHER,
     Spacing.createSpacing(0, 0, 0, false, 0),
     ).withSpacing(
     SqlTypes.DOT,
     SqlTypes.ASTERISK,
     Spacing.createSpacing(0, 0, 0, false, 0),
     ).getSpacing(this, child1, child2)
     } else {
     spacingBuilder.getSpacing(this, child1, child2)
     }
     */
    override fun getIndentCount(
        parentIndent: Int,
        parentTextLen: Int,
    ): Int = parentIndent + (parentTextLen - this.node.text.length)
}
