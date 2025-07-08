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
package org.domaframework.doma.intellij.formatter.block.group.keyword.with

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlWithColumnGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(node, context) {
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlWithCommonTableGroupBlock)?.columnGroupBlock = this
    }

    override fun createBlockIndentLen(): Int = parentBlock?.indent?.groupIndentLen?.minus(1) ?: 0

    override fun createGroupIndentLen(): Int {
        parentBlock?.let { parent ->
            parent.parentBlock?.let { grand ->
                val topKeywordLen = grand.getNodeText().length.plus(1)
                return grand.childBlocks
                    .sumOf { it.getNodeText().length.plus(1) }
                    .plus(topKeywordLen)
                    .plus(1)
            }
        }
        return 2
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = false
}
