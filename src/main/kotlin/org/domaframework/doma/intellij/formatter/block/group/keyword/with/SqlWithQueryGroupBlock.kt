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
package org.domaframework.doma.intellij.formatter.block.group.keyword.with

import com.intellij.lang.ASTNode
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import org.domaframework.doma.intellij.formatter.block.SqlKeywordBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlTopQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * For Example:
 * -- [SqlWithQueryGroupBlock]
 * WITH cte_name ([SqlWithColumnGroupBlock]) AS NOT MATERIALIZED ( [SqlWithCommonTableGroupBlock]
 *      SQL_query or [SqlWithQuerySubGroupBlock]
 * )
 * , cte_name ([SqlWithColumnGroupBlock]) AS ( [SqlWithCommonTableGroupBlock]
 *      ([SqlWithQuerySubGroupBlock])
 * )
 */
class SqlWithQueryGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlTopQueryGroupBlock(
        node,
        context,
    ) {
    var recursiveBlock: SqlKeywordBlock? = null
    val commonTableBlocks: MutableList<SqlWithCommonTableGroupBlock> = mutableListOf()

    override fun addChildBlock(childBlock: SqlBlock) {
        super.addChildBlock(childBlock)
        indent.groupIndentLen = createGroupIndentLen()
    }

    override fun createBlockIndentLen(): Int = 0

    override fun createGroupIndentLen(): Int =
        childBlocks
            .sumOf { it.getNodeText().length.plus(1) }
            .plus(getNodeText().length)
}
