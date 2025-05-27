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

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElParametersBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    customSpacingBuilder: SqlCustomSpacingBuilder?,
    spacingBuilder: SpacingBuilder,
    enableFormat: Boolean,
    private val formatMode: FormattingMode,
) : SqlBlock(
        node,
        wrap,
        alignment,
        customSpacingBuilder,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.LEFT_PAREN, SqlTypes.RIGHT_PAREN ->
                SqlElSymbolBlock(child, wrap, alignment, spacingBuilder, isEnableFormat(), formatMode)

            SqlTypes.EL_PRIMARY_EXPR ->
                SqlElPrimaryBlock(child, wrap, alignment, spacingBuilder, isEnableFormat(), formatMode)

            SqlTypes.COMMA ->
                SqlElCommaBlock(child, wrap, alignment, spacingBuilder, isEnableFormat(), formatMode)

            else -> SqlUnknownBlock(child, wrap, alignment, spacingBuilder, isEnableFormat(), formatMode)
        }

    override fun isLeaf(): Boolean = false
}
