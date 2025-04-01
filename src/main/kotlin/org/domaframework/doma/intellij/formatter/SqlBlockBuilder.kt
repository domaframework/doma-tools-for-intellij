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
package org.domaframework.doma.intellij.formatter

import org.domaframework.doma.intellij.formatter.block.SqlBlock
import kotlin.text.clear

open class SqlBlockBuilder {
    private val groupTopNodeIndexHistory = mutableListOf<Pair<Int, SqlBlock>>()

    fun getGroupTopNodeIndexHistory(): List<Pair<Int, SqlBlock>> = groupTopNodeIndexHistory

    fun addGroupTopNodeIndexHistory(block: Pair<Int, SqlBlock>) {
        groupTopNodeIndexHistory.add(block)
    }

    fun getLastGroupTopNodeIndexHistory(): Pair<Int, SqlBlock>? = groupTopNodeIndexHistory.lastOrNull()

    fun removeLastGroupTopNodeIndexHistory() {
        if (groupTopNodeIndexHistory.isNotEmpty()) {
            groupTopNodeIndexHistory.removeLast()
        }
    }

    fun clearSubListGroupTopNodeIndexHistory(start: Int) {
        groupTopNodeIndexHistory
            .subList(
                start,
                groupTopNodeIndexHistory.size,
            ).clear()
    }

    fun getGroupTopNodeIndexByIndentType(indentType: IndentType): Int =
        groupTopNodeIndexHistory.indexOfLast {
            it.second.indent.indentLevel == indentType
        }
}
