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
package org.domaframework.doma.intellij.formatter.block.group.keyword.inline

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlInlineGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlNewGroupBlock(
        node,
        context,
    ) {
    val inlineConditions: MutableList<SqlInlineSecondGroupBlock> = mutableListOf()

    override val indent =
        ElementIndent(
            IndentType.INLINE,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.INLINE
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int =
        parentBlock?.let { parent ->
            if (parent is SqlElConditionLoopCommentBlock || childBlocks.firstOrNull() is SqlElConditionLoopCommentBlock ||
                parent is SqlSubGroupBlock
            ) {
                parent.indent.groupIndentLen
            } else {
                parent.indent.groupIndentLen.plus(1)
            }
        } ?: 1

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(getNodeText().length)
}
