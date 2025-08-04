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
import org.domaframework.doma.intellij.formatter.block.group.keyword.second.SqlValuesGroupBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

open class SqlValuesParamGroupBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlSubGroupBlock(node, context) {
    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
        (lastGroup as? SqlValuesGroupBlock)?.valueGroupBlocks?.add(this)
    }

    override fun createBlockIndentLen(): Int {
        parentBlock?.let { parent ->
            return parent.indent.indentLen
        }

        return offset
    }

    override fun isSaveSpace(lastGroup: SqlBlock?): Boolean = parentBlock is SqlValuesGroupBlock
}
