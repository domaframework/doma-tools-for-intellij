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
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElPrimaryBlock(
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

            SqlTypes.EL_PRIMARY_EXPR, SqlTypes.EL_NUMBER, SqlTypes.EL_STRING, SqlTypes.BOOLEAN, SqlTypes.EL_NULL ->
                SqlElPrimaryBlock(child, context)

            else -> SqlUnknownBlock(child, context)
        }

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? = spacingBuilder.getSpacing(this, child1, child2)

    override fun isLeaf(): Boolean = true
}
