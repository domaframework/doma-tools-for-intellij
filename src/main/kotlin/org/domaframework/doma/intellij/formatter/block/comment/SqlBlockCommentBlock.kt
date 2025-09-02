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
package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.formatting.Block
import com.intellij.formatting.Spacing
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlUnknownBlock
import org.domaframework.doma.intellij.formatter.block.expr.SqlElSymbolBlock
import org.domaframework.doma.intellij.formatter.builder.SqlCustomSpacingBuilder
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

open class SqlBlockCommentBlock(
    node: ASTNode,
    private val context: SqlBlockFormattingContext,
) : SqlDefaultCommentBlock(node, context) {
    override fun buildChildren(): MutableList<AbstractBlock> = buildChildBlocks { getBlock(it) }

    override fun getBlock(child: ASTNode): SqlBlock =
        when (child.elementType) {
            SqlTypes.BLOCK_COMMENT_START -> SqlCommentStartBlock(child, context)
            SqlTypes.BLOCK_COMMENT_END -> SqlCommentEndBlock(child, context)
            SqlTypes.BLOCK_COMMENT_CONTENT -> SqlCommentContentBlock(child, context)
            SqlTypes.EL_PARSER_LEVEL_COMMENT -> SqlElSymbolBlock(child, context)
            else -> SqlUnknownBlock(child, context)
        }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = true

    override fun getSpacing(
        child1: Block?,
        child2: Block,
    ): Spacing? = SqlCustomSpacingBuilder.nonSpacing
}
