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
package org.domaframework.doma.intellij.formatter.block.group.keyword.top

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateViewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.with.SqlWithQuerySubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

abstract class SqlTopQueryGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(
        node,
        IndentType.TOP,
        context,
    ) {
    companion object {
        private val PARENT_INDENT_SYNC_TYPES =
            listOf(
                SqlCreateViewGroupBlock::class,
                SqlWithQuerySubGroupBlock::class,
                SqlElConditionLoopCommentBlock::class,
            )
        private const val OFFSET = 0
    }

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = indentLevel
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent.indent.indentLevel == IndentType.FILE) return OFFSET
            if (parent is SqlElConditionLoopCommentBlock) {
                return createIndentLenInConditionLoopDirective(parent)
            }
            var baseIndent = parent.indent.groupIndentLen
            if (!TypeUtil.isExpectedClassType(PARENT_INDENT_SYNC_TYPES, parent)) {
                baseIndent = baseIndent.plus(1)
            }
            return baseIndent
        }
        return 0
    }

    protected fun createIndentLenInConditionLoopDirective(parent: SqlElConditionLoopCommentBlock): Int {
        // When the parent is a conditional directive, adjust the indent considering loop nesting
        val parentConditionLoopNests = mutableListOf<SqlBlock>()
        var blockParent: SqlBlock? = parent
        parentConditionLoopNests.add(parent)
        while (blockParent is SqlElConditionLoopCommentBlock) {
            blockParent = blockParent.parentBlock
            if (blockParent != null) parentConditionLoopNests.add(blockParent)
        }
        val prevGroupBlock = parentConditionLoopNests.lastOrNull()
        parentConditionLoopNests.dropLast(1).reversed().forEachIndexed { index, p ->
            if (index == 0) {
                // For the first conditional loop directive, if it has a parent block whose indent level is lower than itself,
                // align with the indent of that parent's parent
                prevGroupBlock?.let { prev ->
                    if (prev.indent.indentLevel >= indent.indentLevel) {
                        p.indent.indentLen = prev.parentBlock?.indent?.indentLen ?: OFFSET
                    }
                }
            } else {
                // For subsequent conditional loop directives, adjust the indent by the nesting count * 2
                p.indent.indentLen = p.parentBlock
                    ?.indent
                    ?.indentLen
                    ?.plus(2) ?: (index * 2)
            }
            p.indent.groupIndentLen = p.indent.indentLen
        }

        return parent.indent.indentLen
    }
}
