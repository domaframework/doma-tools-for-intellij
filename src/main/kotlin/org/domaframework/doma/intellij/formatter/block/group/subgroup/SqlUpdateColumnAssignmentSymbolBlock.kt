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
import org.domaframework.doma.intellij.formatter.block.SqlOtherBlock
import org.domaframework.doma.intellij.formatter.util.SqlBlockFormattingContext

/**
 * A block representing the `=` that connects column groups and value groups in a bulk update statement.
 */
class SqlUpdateColumnAssignmentSymbolBlock(
    node: ASTNode,
    context: SqlBlockFormattingContext,
) : SqlOtherBlock(node, context) {
    override val isNeedWhiteSpace: Boolean = true

    override fun setParentGroupBlock(lastGroup: SqlBlock?) {
        super.setParentGroupBlock(lastGroup)
        indent.indentLen = createIndentLen()
        indent.groupIndentLen = indent.groupIndentLen.plus(1)
    }

//    override fun setParentPropertyBlock(lastGroup: SqlBlock?) {
//        (lastGroup as? SqlUpdateSetGroupBlock)?.assignmentSymbol = this
//    }

    private fun createIndentLen(): Int {
        parentBlock?.let { parent -> return parent.indent.groupIndentLen.plus(1) }
            ?: return 0
    }
}
