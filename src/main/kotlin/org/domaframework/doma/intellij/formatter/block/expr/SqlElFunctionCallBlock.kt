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

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElFunctionCallBlock(
    node: ASTNode,
    private val context: SqlBlockFormattingContext,
    customSpacingBuilder: SqlCustomSpacingBuilder?,
) : SqlExprBlock(
        node,
        context,
    ) {
    override fun buildChildren(): MutableList<AbstractBlock> {
        val blocks = mutableListOf<AbstractBlock>()
        var child = node.firstChildNode
        while (child != null) {
            if (child !is PsiWhiteSpace) {
                val block = getBlock(child)
                blocks.add(block)
            }
            child = child.treeNext
        }
        return blocks
    }

    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.AT_SIGN ->
                SqlElAtSignBlock(child, context, createSpacingBuilder())

            SqlTypes.EL_IDENTIFIER ->
                SqlElIdentifierBlock(child, context)

            SqlTypes.EL_PARAMETERS ->
                SqlElParametersBlock(child, context, createSpacingBuilder())

            else ->
                SqlBlock(
                    child,
                    wrap,
                    alignment,
                    createSpacingBuilder(),
                    spacingBuilder,
                    context.enableFormat,
                    context.formatMode,
                )
        }

    override fun isLeaf(): Boolean = false
}
