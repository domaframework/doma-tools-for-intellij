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
package org.domaframework.doma.intellij.formatter.block.group

import com.intellij.formatting.Alignment
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock

class SqlSelectGroupBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    val parentGroupNode: SqlBlock?,
    spacingBuilder: SpacingBuilder,
) : SqlGroupBlock(
        node,
        wrap,
        alignment,
        parentGroupNode,
        spacingBuilder,
    ) {
    override val indentLevel = 1
    override var searchKeywordLevel = 1

    override fun isLoopContinuation(child: ASTNode): Boolean = !topLevelKeywords.contains(child.text)

    override fun getIndentCount(
        parentIndent: Int,
        parentTextLen: Int,
    ): Int =
        if (parentGroupNode is SqlSubGroupBlock) {
            val parentTextLen = parentGroupNode.node.text.length
            parentIndent + parentTextLen + 1
        } else {
            parentIndent + 2
        }
}
