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
package org.domaframework.doma.intellij.formatter.block.group.keyword

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlFromGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlLateralGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(
        node,
        IndentType.OPTIONS,
        context,
    ) {
    var subQueryGroupBlock: SqlSubGroupBlock? = null

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlFromGroupBlock) {
            if (lastGroup.tableBlocks.isEmpty()) lastGroup.tableBlocks.add(this)
        }
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent is SqlElConditionLoopCommentBlock) return parent.indent.groupIndentLen
            return parent.indent.groupIndentLen.plus(1)
        }
        return 0
    }

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(getNodeText().length)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = false
}
