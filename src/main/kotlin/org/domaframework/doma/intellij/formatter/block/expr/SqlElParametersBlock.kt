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
package org.domaframework.doma.intellij.formatter.block.expr

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElParametersBlock(
    node: ASTNode,
    private val context: SqlBlockFormattingContext,
) : SqlExprBlock(
        node,
        context,
    ) {
    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.LEFT_PAREN, SqlTypes.RIGHT_PAREN ->
                SqlElSymbolBlock(child, context)

            SqlTypes.EL_PRIMARY_EXPR ->
                SqlElPrimaryBlock(child, context)

            SqlTypes.COMMA ->
                SqlElCommaBlock(child, context)

            SqlTypes.EL_FIELD_ACCESS_EXPR ->
                SqlElFieldAccessBlock(child, context)

            SqlTypes.EL_STATIC_FIELD_ACCESS_EXPR ->
                SqlElStaticFieldAccessBlock(child, context)

            SqlTypes.EL_PRIMARY_EXPR ->
                SqlElPrimaryBlock(child, context)

            else -> SqlUnknownBlock(child, context)
        }

    override fun isLeaf(): Boolean = false

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? {
        val childBlock1 = child1 as? SqlBlock
        val childBlock2 = child2 as? SqlBlock

        if (childBlock1 == null) return SqlCustomSpacingBuilder.nonSpacing
        if (childBlock2 != null) {
            if (childBlock2.node.elementType == SqlTypes.RIGHT_PAREN) {
                return SqlCustomSpacingBuilder.nonSpacing
            }
            return when (childBlock1) {
                is SqlElSymbolBlock -> SqlCustomSpacingBuilder.nonSpacing
                is SqlElCommaBlock -> SqlCustomSpacingBuilder.normalSpacing
                else -> SqlCustomSpacingBuilder.nonSpacing
            }
        }

        return SqlCustomSpacingBuilder.normalSpacing
    }
}
