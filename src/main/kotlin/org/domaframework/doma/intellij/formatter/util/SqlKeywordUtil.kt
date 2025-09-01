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
package org.domaframework.doma.intellij.formatter.util

enum class IndentType(
    private val level: Int,
    private val group: Boolean = false,
) {
    FILE(0, true),
    CONFLICT(1, true),
    TOP(1, true),
    SECOND(2, true),
    JOIN(3, true),
    INLINE(2),
    INLINE_SECOND(3, true),
    SECOND_OPTION(4, true),
    TIRD(5),
    ATTACHED(6),
    COLUMN(7, true),
    SUB(90),
    ATTRIBUTE(91),
    LITERAL(92),
    COMMA(93),
    OPTIONS(94),
    PARAM(95),
    NONE(99),
    ;

    fun isNewLineGroup(): Boolean = this.group || this.level == COMMA.level
}

enum class CreateQueryType {
    TABLE,
    INDEX,
    VIEW,
    DATABASE,
    NONE,
    ;

    companion object {
        fun getCreateTableType(text: String): CreateQueryType =
            when (text.lowercase()) {
                "table" -> TABLE
                "index" -> INDEX
                "view" -> VIEW
                "database" -> DATABASE
                else -> NONE
            }
    }
}

class SqlKeywordUtil {
    companion object {
        private val TOP_KEYWORDS: Set<String> =
            setOf(
                "with",
                "select",
                "update",
                "insert",
                "delete",
                "drop",
                "alter",
                "create",
                "truncate",
                "rename",
                "union",
                "intersect",
                "except",
                "do",
            )

        private val SECOND_KEYWORDS: Set<String> =
            setOf(
                "set",
                "from",
                "where",
                "order",
                "group",
                "having",
                "limit",
                "values",
                "returning",
            )

        private val TABLE_MODIFY_KEYWORDS: Set<String> =
            setOf(
                "add",
                "drop",
                "rename",
                "modify",
                "references",
            )

        fun isTopKeyword(keyword: String): Boolean = TOP_KEYWORDS.contains(keyword.lowercase())

        fun isSecondKeyword(keyword: String): Boolean = SECOND_KEYWORDS.contains(keyword.lowercase())

        fun isTableModifyKeyword(keyword: String): Boolean = TABLE_MODIFY_KEYWORDS.contains(keyword.lowercase())

        private val SECOND_OPTION_KEYWORDS: Set<String> =
            setOf("and", "or", "on")

        private val CONDITION_KEYWORDS: Set<String> =
            setOf("and", "or")

        fun isConditionKeyword(keyword: String): Boolean = CONDITION_KEYWORDS.contains(keyword.lowercase())

        fun isSecondOptionKeyword(keyword: String): Boolean = SECOND_OPTION_KEYWORDS.contains(keyword.lowercase())

        private val BEFORE_TABLE_KEYWORDS: Set<String> =
            setOf("from", "update", "drop", "table")

        private val SELECT_SECOND_OPTION_KEYWORDS: Set<String> =
            setOf("from", "where", "group", "having", "order", "rows")

        fun isSelectSecondOptionKeyword(keyword: String): Boolean = SELECT_SECOND_OPTION_KEYWORDS.contains(keyword.lowercase())

        fun isBeforeTableKeyword(keyword: String): Boolean = BEFORE_TABLE_KEYWORDS.contains(keyword.lowercase())

        private val JOIN_KEYWORDS: Set<String> =
            setOf("left", "right", "full", "cross", "natural")

        fun isJoinKeyword(keyword: String): Boolean = JOIN_KEYWORDS.contains(keyword.lowercase())

        private val JOIN_ATTACHED_KEYWORDS: Set<String> =
            setOf("outer", "inner", "join")

        fun isJoinAttachedKeyword(keyword: String): Boolean = JOIN_ATTACHED_KEYWORDS.contains(keyword.lowercase())

        private val ATTACHED_KEYWORDS: Set<String> =
            setOf("distinct", "into", "table", "index", "database", "view")

        fun isAttachedKeyword(keyword: String): Boolean = ATTACHED_KEYWORDS.contains(keyword.lowercase())

        private val THIRD_KEYWORDS =
            setOf(
                "add",
                "between",
                "modify",
                "column",
            )

        fun isThirdKeyword(keyword: String): Boolean = THIRD_KEYWORDS.contains(keyword.lowercase())

        private val COLUMN_TYPE_KEYWORDS =
            setOf(
                "int",
                "integer",
                "smallint",
                "bigint",
                "tinyint",
                "float",
                "double",
                "decimal",
                "numeric",
                "char",
                "varchar",
                "text",
                "date",
                "time",
                "timestamp",
                "datetime",
                "boolean",
                "bit",
                "binary",
                "varbinary",
                "blob",
                "clob",
                "json",
                "enum",
                "set",
            )

        fun isColumnTypeKeyword(keyword: String): Boolean = COLUMN_TYPE_KEYWORDS.contains(keyword.lowercase())

        private val LITERAL_KEYWORDS =
            setOf(
                "null",
                "true",
                "false",
                "current_date",
            )

        fun isLiteralKeyword(keyword: String): Boolean = LITERAL_KEYWORDS.contains(keyword.lowercase())

        private val ATTRIBUTE_KEYWORD =
            setOf(
                "default",
                "key",
                "unique",
                "primary",
                "foreign",
            )

        fun isAttributeKeyword(keyword: String): Boolean = ATTRIBUTE_KEYWORD.contains(keyword.lowercase())

        private val INLINE_PARENT_SQL_KEYWORDS =
            setOf(
                "if",
                "case",
            )

        fun isInlineParentSqlKeyword(keyword: String): Boolean = INLINE_PARENT_SQL_KEYWORDS.contains(keyword.lowercase())

        private val INLINE_SQL_KEYWORDS =
            setOf(
                "when",
                "else",
                "end",
            )

        fun isInlineSqlKeyword(keyword: String): Boolean = INLINE_SQL_KEYWORDS.contains(keyword.lowercase())

        private val OPTION_SQL_KEYWORDS =
            setOf(
                "as",
                "not",
                "materialized",
                "by",
                "asc",
                "desc",
                "all",
                "check",
                "exists",
                "full",
                "is",
                "like",
                "offset",
                "then",
                "in",
                "recursive",
                "breadth",
                "depth",
                "first",
                "to",
                "using",
                "lateral",
            )

        fun isOptionSqlKeyword(keyword: String): Boolean = OPTION_SQL_KEYWORDS.contains(keyword.lowercase())

        private val CONFLICT_ATTACHED_KEYWORDS =
            setOf(
                "conflict",
                "constraint",
            )

        fun isConflictAttachedKeyword(keyword: String): Boolean = CONFLICT_ATTACHED_KEYWORDS.contains(keyword.lowercase())

        private val WITH_OPTION_KEYWORDS =
            setOf(
                "search",
                "cycle",
            )

        fun isWithOptionKeyword(keyword: String): Boolean = WITH_OPTION_KEYWORDS.contains(keyword.lowercase())

        private val EXISTS_KEYWORDS =
            setOf(
                "if",
                "exists",
                "not",
            )

        fun isExistsKeyword(keyword: String): Boolean = EXISTS_KEYWORDS.contains(keyword.lowercase())

        private val HAS_FILTER_PARAM =
            setOf("group", "in", "over", "into", "values", "filter", "references", "using")

        fun hasFilterParam(keyword: String): Boolean = HAS_FILTER_PARAM.contains(keyword.lowercase())

        private val SET_LINE_KEYWORDS =
            mapOf(
                "into" to setOf("insert"),
                "column" to setOf("add", "modify", "drop"),
                "from" to setOf("delete", "distinct", "year"),
                "distinct" to setOf("select"),
                "table" to setOf("create", "alter", "rename", "truncate", "drop"),
                "index" to setOf("create", "alter", "rename", "truncate", "drop"),
                "view" to setOf("create", "alter", "rename", "truncate", "drop"),
                "database" to setOf("create", "alter", "rename", "truncate", "drop"),
                "join" to setOf("outer", "inner", "left", "right"),
                "outer" to setOf("left", "right"),
                "inner" to setOf("left", "right"),
                "group" to setOf("within"),
                "by" to setOf("group", "order", "first", "partition"),
                "and" to setOf("between", "preceding"),
                // "if" to setOf("table", "index","view"),
                "exists" to setOf("if", "where"),
                "conflict" to setOf("on"),
                "nothing" to setOf("do"),
                "constraint" to setOf("on"),
                "update" to setOf("do"),
                "set" to setOf("by", "cycle"),
                "order" to setOf("partition"),
                "select" to setOf("if", "exists"),
            )

        fun isSetLineKeyword(
            keyword: String,
            prevKeyword: String,
        ): Boolean = SET_LINE_KEYWORDS[keyword.lowercase()]?.contains(prevKeyword.lowercase()) == true

        fun isComma(keyword: String): Boolean = keyword == ","

        fun getIndentType(keywordText: String): IndentType {
            val keyword = keywordText.lowercase()
            return when {
                isTopKeyword(keyword) -> IndentType.TOP
                isSecondKeyword(keyword) || isSelectSecondOptionKeyword(keyword) || isWithOptionKeyword(keyword) ||
                    isTableModifyKeyword(keyword) -> IndentType.SECOND
                isSecondOptionKeyword(keyword) || isConditionKeyword(keyword) -> IndentType.SECOND_OPTION
                isJoinKeyword(keyword) || isJoinAttachedKeyword(keyword) -> IndentType.JOIN
                isAttachedKeyword(keyword) -> IndentType.ATTACHED
                isThirdKeyword(keyword) -> IndentType.TIRD
                isInlineParentSqlKeyword(keyword) -> IndentType.INLINE
                isInlineSqlKeyword(keyword) -> IndentType.INLINE_SECOND
                isAttributeKeyword(keyword) -> IndentType.ATTRIBUTE
                isLiteralKeyword(keyword) -> IndentType.LITERAL
                isOptionSqlKeyword(keyword) -> IndentType.OPTIONS
                isColumnTypeKeyword(keyword) -> IndentType.COLUMN
                isConflictAttachedKeyword(keyword) -> IndentType.ATTACHED
                isComma(keyword) -> IndentType.COMMA
                else -> IndentType.NONE
            }
        }
    }
}
