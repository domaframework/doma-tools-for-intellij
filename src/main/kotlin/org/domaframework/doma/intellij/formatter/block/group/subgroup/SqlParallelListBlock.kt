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
package org.domaframework.doma.intellij.formatter.block.group.subgroup

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.comment.SqlBlockCommentBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlLineCommentBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * A class that represents the List type test data after the IN clause.
 * If the child element is a [org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock],
 * it controls the indentation in the same way as a [SqlSubQueryGroupBlock].
 * If the direct child element is a comma, it controls the line break.
 */
class SqlParallelListBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubQueryGroupBlock(
        node,
        context,
    ) {
    override val indent =
        ElementIndent(
            IndentType.SUB,
            0,
            0,
        )

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            return parent
                .getChildBlocksDropLast()
                .filter { it !is SqlLineCommentBlock && it !is SqlBlockCommentBlock }
                .sumOf { it.getNodeText().length.plus(1) }
                .plus(parent.indent.indentLen)
                .plus(parent.getNodeText().length)
                .plus(1)
        }
        return 0
    }

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(1)
}
