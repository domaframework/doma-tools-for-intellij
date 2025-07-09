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
package org.domaframework.doma.intellij.formatter.block.group.keyword.second

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

open class SqlSecondKeywordBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(node, IndentType.SECOND, context) {
    private val offset = 0

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createBlockIndentLen()
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            val groupLen = parent.indent.groupIndentLen
            return if (parent.indent.indentLevel == IndentType.FILE) {
                offset
            } else {
                groupLen.minus(this.getNodeText().length)
            }
        }
        return offset
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        lastGroup?.let { last ->
            val prevKeyword = last.childBlocks.findLast { it is SqlKeywordBlock }
            prevKeyword?.let { prev ->
                return !SqlKeywordUtil.isSetLineKeyword(getNodeText(), prev.getNodeText())
            }
            return !SqlKeywordUtil.isSetLineKeyword(getNodeText(), last.getNodeText())
        }
        return true
    }
}
