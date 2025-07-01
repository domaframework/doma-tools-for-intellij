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
package org.domaframework.doma.intellij.formatter.block.group.keyword.insert

import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlInsertQueryGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(
        node,
        IndentType.TOP,
        context,
    ) {
    var columnDefinitionGroupBlock: SqlInsertColumnGroupBlock? = null
    var valueKeywordBlock: SqlKeywordGroupBlock? = null
    var valueGroupBlock: SqlInsertValueGroupBlock? = null

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.TOP
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = indent.indentLen.plus(getNodeText().length)
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? = Indent.getSpaceIndent(indent.indentLen)

    override fun createBlockIndentLen(preChildBlock: SqlBlock?): Int =
        if (!isAdjustIndentOnEnter()) {
            parentBlock?.let {
                if (it.indent.indentLevel == IndentType.SUB) {
                    it.indent.groupIndentLen.plus(1)
                } else {
                    0
                }
            } ?: 0
        } else {
            0
        }
}
