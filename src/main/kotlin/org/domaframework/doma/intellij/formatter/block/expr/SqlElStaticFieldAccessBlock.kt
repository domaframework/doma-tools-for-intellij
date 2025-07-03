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

import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElStaticFieldAccessBlock(
    node: ASTNode,
    private val context: SqlBlockFormattingContext,
) : SqlExprBlock(
        node,
        context,
    ) {
    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.AT_SIGN -> {
                if (PsiTreeUtil.skipSiblingsBackward(
                        child.psi,
                        PsiWhiteSpace::class.java,
                    ) is SqlElClass
                ) {
                    SqlElClassRightBlock(child, context)
                } else {
                    SqlElAtSignBlock(child, context, null)
                }
            }

            SqlTypes.EL_CLASS ->
                SqlElClassBlock(child, context, null)

            SqlTypes.EL_IDENTIFIER ->
                SqlElIdentifierBlock(child, context)

            else -> SqlUnknownBlock(child, context)
        }

    override fun createSpacingBuilder(): SqlCustomSpacingBuilder =
        SqlCustomSpacingBuilder()
            .withSpacing(
                SqlTypes.AT_SIGN,
                SqlTypes.EL_CLASS,
                Spacing.createSpacing(0, 0, 0, false, 0),
            ).withSpacing(
                SqlTypes.AT_SIGN,
                SqlTypes.EL_IDENTIFIER,
                Spacing.createSpacing(0, 0, 0, false, 0),
            ).withSpacing(
                SqlTypes.EL_IDENTIFIER,
                SqlTypes.DOT,
                Spacing.createSpacing(0, 0, 0, false, 0),
            ).withSpacing(
                SqlTypes.EL_IDENTIFIER,
                SqlTypes.EL_PARAMETERS,
                Spacing.createSpacing(0, 0, 0, false, 0),
            )

    override fun isLeaf(): Boolean = false
}
