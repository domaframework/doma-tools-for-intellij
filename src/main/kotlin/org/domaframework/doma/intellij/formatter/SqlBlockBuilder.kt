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
