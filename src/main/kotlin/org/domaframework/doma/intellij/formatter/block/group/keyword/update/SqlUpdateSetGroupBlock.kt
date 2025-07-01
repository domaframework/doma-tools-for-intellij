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
package org.domaframework.doma.intellij.formatter.block.group.keyword.update

import com.intellij.formatting.Indent
import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateColumnAssignmentSymbolBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlUpdateValueGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlUpdateSetGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(
        node,
        IndentType.SECOND,
        context,
    ) {
    val updateColumnRaws: MutableList<SqlBlock> = mutableListOf()

    // A block used for bulk updates.
    // It contains a **column group**, an **equals sign (`=`)**, and a **value group block**.
    var columnDefinitionGroupBlock: SqlUpdateColumnGroupBlock? = null
    var assignmentSymbol: SqlUpdateColumnAssignmentSymbolBlock? = null
    var valueGroupBlock: SqlUpdateValueGroupBlock? = null

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLevel = IndentType.SECOND
        indent.indentLen = createBlockIndentLen(null)
        indent.groupIndentLen = indent.indentLen.plus(getNodeText().length)
    }

//    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
//        (lastGroup as? SqlUpdateQueryGroupBlock)?.setQueryGroupBlock = this
//    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()

    override fun getIndent(): Indent? = Indent.getSpaceIndent(indent.indentLen)

    override fun createBlockIndentLen(preChildBlock: SqlBlock?): Int =
        if (!isAdjustIndentOnEnter()) {
            parentBlock?.let { parent ->
                if (parent.indent.indentLevel == IndentType.SUB) {
                    parent.indent.groupIndentLen.plus(1)
                } else {
                    val parentTextLen = parent.getNodeText().length
                    val diffTextLen = parentTextLen.minus(getNodeText().length)
                    parent.indent.indentLen.plus(diffTextLen)
                }
            } ?: 0
        } else {
            0
        }
}
