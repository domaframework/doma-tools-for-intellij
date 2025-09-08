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
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.SqlRightPatternBlock
import org.domaframework.doma.intellij.formatter.block.group.SqlNewGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.SqlKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlFunctionParamBlock
import org.domaframework.doma.intellij.formatter.block.group.subgroup.SqlSubGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

open class SqlSecondKeywordBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlKeywordGroupBlock(node, IndentType.SECOND, context) {
    override val offset = 0

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            val groupLen = parent.indent.groupIndentLen
            return if (parent.indent.indentLevel == IndentType.FILE) {
                offset
            } else if (parent is SqlSubGroupBlock) {
                val space =
                    if (TypeUtil.isExpectedClassType(SqlRightPatternBlock.NOT_INDENT_EXPECTED_TYPES, parent)) {
                        0
                    } else {
                        1
                    }
                groupLen.plus(space)
            } else {
                groupLen.minus(this.getNodeText().length)
            }
        }
        return offset
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean {
        parentBlock?.let { parent ->
            if (parent is SqlFunctionParamBlock) {
                val firstKeywordParam =
                    parent.childBlocks.firstOrNull { it is SqlNewGroupBlock }
                return firstKeywordParam != null && firstKeywordParam != this
            } else {
                val prevKeywordGroupBlock =
                    prevBlocks.lastOrNull()?.childBlocks?.lastOrNull {
                        it is SqlKeywordBlock ||
                            it is SqlKeywordGroupBlock
                    }
                if (SqlKeywordUtil.isSetLineKeyword(getNodeText(), prevKeywordGroupBlock?.getNodeText() ?: "")) {
                    return false
                }
                return super.isSaveSpace(lastGroup)
            }
        }
        return true
    }
}
