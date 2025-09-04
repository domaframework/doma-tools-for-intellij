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
package org.domaframework.doma.intellij.formatter.block.other

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlArrayListGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlEscapeBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlOtherBlock(node, context) {
    // If the number of escape characters, including itself, is even
    var isEndEscape = false

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (parentBlock is SqlArrayListGroupBlock) {
            (parentBlock as SqlArrayListGroupBlock).endSymbol = this
        }
    }

    override fun createBlockIndentLen(): Int {
        val prevBlocks = parentBlock?.childBlocks?.count { it is SqlEscapeBlock } ?: 0

        val hasEvenEscapeBlocks = prevBlocks.let { it % 2 == 0 } == true
        isEndEscape = hasEvenEscapeBlocks || getNodeText() == "]"
        return if (isEndEscape) {
            0
        } else {
            calculateIndentLen()
        }
    }

    private fun calculateIndentLen(): Int {
        parentBlock?.let { parent ->
            when (parent) {
                is SqlSubQueryGroupBlock -> {
                    val parentIndentLen = parent.indent.groupIndentLen
                    val grand = parent.parentBlock
                    if (grand != null && grand.getNodeText().lowercase() == "create") {
                        val grandIndentLen = grand.indent.groupIndentLen
                        return grandIndentLen.plus(parentIndentLen).plus(1)
                    }
                    return parentIndentLen.plus(1)
                }

                else -> {
                    if (isSaveSpace(parentBlock) || conditionLoopDirective != null)return parentBlock?.indent?.groupIndentLen ?: 1
                    return 1
                }
            }
        }
        return 1
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean =
        if (isEndEscape) {
            false
        } else {
            super.isSaveSpace(lastGroup)
        }
}
