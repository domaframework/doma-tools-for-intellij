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
package org.domaframework.doma.intellij.formatter.block

import com.intellij.lang.ASTNode
import com.intellij.psi.formatter.common.AbstractBlock
import org.domaframework.doma.intellij.formatter.block.group.keyword.create.SqlCreateKeywordGroupBlock
import org.domaframework.doma.intellij.formatter.util.CreateQueryType
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

class SqlTableBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlWordBlock(
        node,
        context,
    ) {
    override val isNeedWhiteSpace = false

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
    }

    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        if (lastGroup is SqlCreateKeywordGroupBlock && lastGroup.createType == CreateQueryType.TABLE) {
            lastGroup.tableBlock = this
        }
    }

    override fun buildChildren(): MutableList<AbstractBlock> = mutableListOf()
}
