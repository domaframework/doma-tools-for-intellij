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
package org.domaframework.doma.intellij.formatter.block.comment

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlFileBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

abstract class SqlDefaultCommentBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlCommentBlock(node, context) {
    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun isLeaf(): Boolean = true

    /**
     * When the next element block is determined, update the indent to align it with the element below.
     */
    fun updateIndentLen(baseBlock: SqlBlock) {
        indent.indentLen =
            if (parentBlock is SqlFileBlock || isSaveSpace(parentBlock)) {
                baseBlock.indent.indentLen
            } else {
                1
            }
    }

    override fun isSaveSpace(lastGroup: SqlBlock?) = PsiTreeUtil.prevLeaf(node.psi)?.text?.contains("\n") == true
}
