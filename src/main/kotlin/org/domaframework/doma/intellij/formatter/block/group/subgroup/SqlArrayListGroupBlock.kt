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
package org.domaframework.doma.intellij.formatter.block.group.subgroup

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.other.SqlEscapeBlock
import org.domaframework.doma.intellij.formatter.block.word.SqlArrayWordBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlArrayListGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(
        node,
        context,
    ) {
    var endSymbol: SqlEscapeBlock? = null
    var arrayBlock: SqlArrayWordBlock? = null

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        arrayBlock = parentBlock?.getChildBlocksDropLast()?.findLast { it is SqlArrayWordBlock } as SqlArrayWordBlock?
        arrayBlock?.arrayParams = this
    }

    override fun createBlockIndentLen(): Int = arrayBlock?.indent?.groupIndentLen ?: 1

    override fun createGroupIndentLen(): Int = indent.indentLen.plus(1)

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = false
}
