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
package org.domaframework.doma.intellij.formatter.block

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithCommonTableGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlKeywordBlock(
    node: ASTNode,
    val indentLevel: IndentType = IndentType.ATTACHED,
    context: SqlBlockFormattingContext,
) : SqlNewGroupBlock(node, context) {
    override val indent =
        ElementIndent(
            indentLevel,
            0,
            0,
        )

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = indentLevel
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(getNodeText().length)
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlKeywordGroupBlock) {
            lastGroup.updateTopKeywordBlocks(this)
        }

        if (getNodeText() == "nothing" && lastGroup is SqlDoGroupBlock) {
            lastGroup.doQueryBlock = this
        }

        if (lastGroup is SqlWithQueryGroupBlock) {
            when (getNodeText()) {
                "recursive" -> lastGroup.recursiveBlock = this
            }
        }

        if (lastGroup is SqlWithCommonTableGroupBlock) {
            lastGroup.optionKeywordBlocks.add(this)
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int =
        when (indentLevel) {
            IndentType.TOP -> {
                parentBlock?.let { parent ->
                    if (parent.indent.indentLevel == IndentType.SUB) {
                        parent.indent.groupIndentLen.plus(1)
                    } else {
                        0
                    }
                } ?: 0
            }

            IndentType.SECOND -> {
                parentBlock?.let { parent ->
                    parent.indent.groupIndentLen
                        .plus(parent.getNodeText().length)
                        .minus(getNodeText().length)
                } ?: 1
            }

            IndentType.INLINE_SECOND -> {
                parentBlock?.let { parent ->
                    parent.indent.groupIndentLen
                        .plus(parent.getNodeText().length)
                        .plus(1)
                } ?: 1
            }

            else -> {
                parentBlock?.let { parent ->
                    if (parent is SqlElConditionLoopCommentBlock) {
                        parent.indent.groupIndentLen
                    } else {
                        1
                    }
                } ?: 1
            }
        }
}
