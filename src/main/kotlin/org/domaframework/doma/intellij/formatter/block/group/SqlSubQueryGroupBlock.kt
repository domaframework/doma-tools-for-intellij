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

class SqlSubQueryGroupBlock(
    node: ASTNode,
    groupTopNode: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    parentGroupNode: SqlBlock?,
    spacingBuilder: SpacingBuilder,
) : SqlGroupBlock(
        node,
        groupTopNode,
        wrap,
        alignment,
        parentGroupNode,
        spacingBuilder,
    ) {
    override val indentLevel = 3

    val someLevelKeyword = mutableListOf("inner", "left", "right", "outer", "join", "cross")

    private fun isOptionalSubQuery(): Boolean = listOf("inner", "left", "right", "outer", "cross").contains(node.text.lowercase())

    override fun isLoopContinuation(child: ASTNode): Boolean = !someLevelKeyword.contains(child.text) && isOptionalSubQuery()

    override fun getIndentCount(
        parentIndent: Int,
        parentTextLen: Int,
    ): Int = parentIndent + parentTextLen + 1
}
