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
package org.domaframework.doma.intellij.formatter.block.word

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlFunctionGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlWordBlock(node, context) {
    var parameterGroupBlock: SqlFunctionParamBlock? = null
    var prevChildren: List<SqlBlock> = emptyList()

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        prevChildren = lastGroup?.childBlocks?.toList() ?: emptyList()
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            val children = prevChildren.dropLast(1).filter { it !is SqlLineCommentBlock && it !is SqlBlockCommentBlock }
            val prevBlocksLength =
                children
                    .sumOf { prev ->
                        prev
                            .getChildrenTextLen()
                            .plus(
                                if (prev.node.elementType == SqlTypes.DOT ||
                                    prev.node.elementType == SqlTypes.RIGHT_PAREN
                                ) {
                                    0
                                } else {
                                    prev.getNodeText().length.plus(1)
                                },
                            )
                    }.plus(parent.indent.groupIndentLen)
            return if (parent is SqlSubGroupBlock) {
                // parent.indent.groupIndentLen
                prevBlocksLength
            } else {
                prevBlocksLength.plus(1)
            }
        }
        return 0
    }

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(getNodeText().length)
}
