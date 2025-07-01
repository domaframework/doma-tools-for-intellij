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
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTopQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.IndentType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext
import org.domaframework.doma.intellij.formatter.util.SqlKeywordUtil

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
        createGroupIndentLen()
    }

    override fun getBaseIndentLen(
        preChildBlock: SqlBlock?,
        block: SqlBlock?,
    ): Int {
        if (block == null) {
            return createBlockIndentLen(preChildBlock)
        }
        if (preChildBlock == null) return createBlockIndentLen(preChildBlock)

        if (preChildBlock.indent.indentLevel == this.indent.indentLevel &&
            !SqlKeywordUtil.Companion.isSetLineKeyword(getNodeText(), preChildBlock.getNodeText())
        ) {
            if (indent.indentLevel == IndentType.SECOND) {
                val diffPreBlockTextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
                return preChildBlock.indent.indentLen.minus(diffPreBlockTextLen)
            } else {
                val diffPretextLen = getNodeText().length.minus(preChildBlock.getNodeText().length)
                return preChildBlock.indent.indentLen.minus(diffPretextLen)
            }
        } else {
            return createBlockIndentLen(preChildBlock)
        }
    }
}
