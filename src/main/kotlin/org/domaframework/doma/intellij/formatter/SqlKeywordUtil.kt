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

enum class IndentType(
    private val level: Int,
    private val group: Boolean = false,
) {
    FILE(0, true),
    TOP(1, true),
    SECOND(2, true),
    JOIN(3, true),
    SECOND_OPTION(4, true),
    TIRD(5),
    ATTACHED(6),
    INLINE_SECOND(8, true),
    COLUMN(9, true),
    SUB(90),
    ATTRIBUTE(91),
    LITERAL(92),
    OPTIONS(93),
    COMMA(94),
    PARAM(95),
    INLINE(96),
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
            )

        fun isTopKeyword(keyword: String): Boolean = TOP_KEYWORDS.contains(keyword.lowercase())

        private val SECOND_KEYWORD =
            setOf(
                "from",
                "where",
                "order",
                "group",
                "having",
                "limit",
            )

        fun isSecondKeyword(keyword: String): Boolean = SECOND_KEYWORD.contains(keyword.lowercase())

        private val SECOND_OPTION_KEYWORD =
            setOf(
                "and",
                "or",
                "on",
            )

        fun isSecondOptionKeyword(keyword: String): Boolean = SECOND_OPTION_KEYWORD.contains(keyword.lowercase())

        private val BEFORE_TABLE_KEYWORD =
            setOf(
                "from",
                "update",
                "drop",
                "table",
            )

        fun isBeforeTableKeyword(keyword: String): Boolean = BEFORE_TABLE_KEYWORD.contains(keyword.lowercase())

        private val JOIN_KEYWORD =
            setOf(
                "left",
                "right",
                "full",
                "cross",
                "natural",
            )

        fun isJoinKeyword(keyword: String): Boolean = JOIN_KEYWORD.contains(keyword.lowercase())

        private val JOIN_ATTACHED_KEYWORD =
            setOf(
                "outer",
                "inner",
                "join",
            )

        fun isJoinAttachedKeyword(keyword: String): Boolean = JOIN_ATTACHED_KEYWORD.contains(keyword.lowercase())

        private val ATTACHED_KEYWORD =
            setOf(
                "distinct",
                "table",
                "index",
                "database",
                "view",
            )

        fun isAttachedKeyword(keyword: String): Boolean = ATTACHED_KEYWORD.contains(keyword.lowercase())

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
                "constraint",
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
                "by",
                "to",
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
                "values",
                "in",
            )

        fun isOptionSqlKeyword(keyword: String): Boolean = OPTION_SQL_KEYWORDS.contains(keyword.lowercase())

        private val SET_LINE_KEYWORDS =
            mapOf(
                "into" to setOf("insert"),
                "from" to setOf("delete"),
                "distinct" to setOf("select"),
                "table" to setOf("create", "alter", "rename", "truncate", "drop"),
                "index" to setOf("create", "alter", "rename", "truncate", "drop"),
                "view" to setOf("create", "alter", "rename", "truncate", "drop"),
                "database" to setOf("create", "alter", "rename", "truncate", "drop"),
                "join" to setOf("outer", "inner", "left", "right"),
                "outer" to setOf("left", "right"),
                "inner" to setOf("left", "right"),
                "by" to setOf("group", "order"),
                "and" to setOf("between"),
                "if" to setOf("table", "create"),
            )

        fun isSetLineKeyword(
            keyword: String,
            prevKeyword: String,
        ): Boolean = SET_LINE_KEYWORDS[keyword.lowercase()]?.contains(prevKeyword.lowercase()) == true

        fun getIndentType(keywordText: String): IndentType {
            val keyword = keywordText.lowercase()
            return when {
                isTopKeyword(keyword) -> IndentType.TOP
                isSecondKeyword(keyword) -> IndentType.SECOND
                isSecondOptionKeyword(keyword) -> IndentType.SECOND_OPTION
                isJoinKeyword(keyword) -> IndentType.JOIN
                isJoinAttachedKeyword(keyword) -> IndentType.JOIN
                isAttachedKeyword(keyword) -> IndentType.ATTACHED
                isThirdKeyword(keyword) -> IndentType.TIRD
                isInlineParentSqlKeyword(keyword) -> IndentType.INLINE
                isInlineSqlKeyword(keyword) -> IndentType.INLINE_SECOND
                isAttributeKeyword(keyword) -> IndentType.ATTRIBUTE
                isLiteralKeyword(keyword) -> IndentType.LITERAL
                isOptionSqlKeyword(keyword) -> IndentType.OPTIONS
                isColumnTypeKeyword(keyword) -> IndentType.COLUMN
                keyword == "," -> IndentType.COMMA
                else -> IndentType.NONE
            }
        }
    }
}
