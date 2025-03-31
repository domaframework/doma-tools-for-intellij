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
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElFunctionCallBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    customSpacingBuilder: SqlCustomSpacingBuilder?,
    spacingBuilder: SpacingBuilder,
) : SqlBlock(
        node,
        wrap,
        alignment,
        customSpacingBuilder,
        spacingBuilder,
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
                SqlElAtSignBlock(child, wrap, alignment, createSpacingBuilder(), spacingBuilder)

            SqlTypes.EL_IDENTIFIER ->
                SqlElIdentifierBlock(child, wrap, alignment, spacingBuilder)

            SqlTypes.EL_PARAMETERS ->
                SqlElParametersBlock(child, wrap, alignment, createSpacingBuilder(), spacingBuilder)

            else -> SqlBlock(child, wrap, alignment, createSpacingBuilder(), spacingBuilder)
        }

    override fun isLeaf(): Boolean = false
}
