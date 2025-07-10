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
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlDeleteQueryGroupBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.top.SqlSelectQueryGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlFromGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSecondKeywordBlock(node, context) {
    val tableBlocks: MutableList<SqlBlock> = mutableListOf()

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlSelectQueryGroupBlock)?.secondGroupBlocks?.add(this)
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = parentBlock !is SqlDeleteQueryGroupBlock && super.isSaveSpace(lastGroup)
}
