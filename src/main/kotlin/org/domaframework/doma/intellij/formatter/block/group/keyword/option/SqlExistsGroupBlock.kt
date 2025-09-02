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
package org.domaframework.doma.intellij.formatter.block.group.keyword.option

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.comment.SqlElConditionLoopCommentBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.inline.SqlInlineSecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlTableModifySecondGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTableModificationKeyword
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlExistsGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(node, IndentType.OPTIONS, context) {
    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            if (parent.parentBlock is SqlElConditionLoopCommentBlock) {
                return parent.indent.groupIndentLen
            }
        }
        return parentBlock?.indent?.groupIndentLen?.plus(1) ?: 1
    }

    override fun createGroupIndentLen(): Int {
        // If this group is not the top of a line, there must be one space between it and the block before it.
       val correctionSpace = if(childBlocks.firstOrNull() is SqlElConditionLoopCommentBlock){
            0
        }else 1
        val parentGroupIndent = parentBlock?.indent?.groupIndentLen ?: 0
        return getTotalTopKeywordLength().plus(parentGroupIndent).plus(correctionSpace)
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        if (lastGroup is SqlTableModificationKeyword ||
            lastGroup is SqlTableModifySecondGroupBlock ||
            lastGroup is SqlCreateKeywordGroupBlock ||
            lastGroup is SqlInlineSecondGroupBlock
        ) {
            return false
        }
        return super.isSaveSpace(lastGroup)
    }
}
