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

import com.intellij.formatting.Alignment
import com.intellij.formatting.FormattingMode
import com.intellij.formatting.SpacingBuilder
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubQueryGroupBlock

open class SqlLineCommentBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    spacingBuilder: SpacingBuilder,
    enableFormat: Boolean,
    formatMode: FormattingMode,
) : SqlCommentBlock(
        node,
        wrap,
        alignment,
        spacingBuilder,
        enableFormat,
        formatMode,
    ) {
    override fun setParentGroupBlock(block: SqlBlock?) {
        super.setParentGroupBlock(block)
        indent.indentLen = createBlockIndentLen()
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun isLeaf(): Boolean = true

    override fun createBlockIndentLen(): Int {
        parentBlock?.let {
            if (it is SqlSubQueryGroupBlock) {
                if (it.childBlocks.dropLast(1).isEmpty()) {
                    return 1
                }
                if (it.isFirstLineComment) {
                    return it.indent.groupIndentLen.minus(2)
                }
            }
            return it.indent.indentLen
        }
        return 1
    }
}
