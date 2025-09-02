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

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.conflict.SqlDoGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlTableModifySecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTableModificationKeyword
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTopQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlUpdateQueryGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlTopQueryGroupBlock(
        node,
        context,
    ) {
    var setQueryGroupBlock: SqlUpdateSetGroupBlock? = null

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        val preChildBlock = lastGroup?.childBlocks?.dropLast(1)?.lastOrNull()
        indent.indentLevel = indentLevel

        val baseIndentLen = getBaseIndentLen(preChildBlock, lastGroup)
        indent.groupIndentLen = baseIndentLen.plus(getNodeText().length)
        indent.indentLen = baseIndentLen
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlDoGroupBlock)?.doQueryBlock = this
    }

    override fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        lastGroup: SqlBlock?,
    ): Int {
        if (lastGroup == null) return 0

        return if (lastGroup is SqlDoGroupBlock) {
            lastGroup.getNodeText().length.plus(1)
        } else {
            createBlockIndentLen(preChildBlock)
        }
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean =
        parentBlock !is SqlDoGroupBlock &&
            parentBlock !is SqlTableModifySecondGroupBlock && parentBlock !is SqlTableModificationKeyword
}
