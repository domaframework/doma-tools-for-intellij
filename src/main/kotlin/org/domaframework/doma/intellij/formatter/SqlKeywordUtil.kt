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

class SqlKeywordUtil {
    companion object {
        val TOP_KEYWORDS: Set<String> =
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

        val SECOND_KEYWORD =
            setOf(
                "from",
                "where",
                "order",
                "group",
                "having",
                "limit",
                "and",
                "or",
            )

        fun isSecondKeyword(keyword: String): Boolean = SECOND_KEYWORD.contains(keyword.lowercase())

        val ATTACHED_KEYWORD =
            setOf(
                "distinct",
                "table",
                "left",
                "right",
                "outer",
                "inner",
                "join",
            )

        fun isAttachedKeyword(keyword: String): Boolean = ATTACHED_KEYWORD.contains(keyword.lowercase())

        val THIRD_KEYWORDS =
            setOf(
                "add",
                "between",
                "modify",
                "column",
            )

        fun isThirdKeyword(keyword: String): Boolean = THIRD_KEYWORDS.contains(keyword.lowercase())

        val COLUMN_TYPE_KEYWORDS =
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

        val LITERAL_KEYWORDS =
            setOf(
                "null",
                "true",
                "false",
                "current_date",
            )

        fun isLiteralKeyword(keyword: String): Boolean = LITERAL_KEYWORDS.contains(keyword.lowercase())

        val ATTRIBUTE_KEYWORD =
            setOf(
                "default",
                "index",
                "key",
                "unique",
                "primary",
                "foreign",
                "constraint",
            )

        fun isAttributeKeyword(keyword: String): Boolean = ATTRIBUTE_KEYWORD.contains(keyword.lowercase())

        val INLINE_PARENT_SQL_KEYWORDS =
            setOf(
                "if",
                "case",
                "end",
            )

        fun isInlineParentSqlKeyword(keyword: String): Boolean = INLINE_PARENT_SQL_KEYWORDS.contains(keyword.lowercase())

        val INLINE_SQL_KEYWORDS =
            setOf(
                "when",
                "else",
            )

        fun isInlineSqlKeyword(keyword: String): Boolean = INLINE_SQL_KEYWORDS.contains(keyword.lowercase())

        val OPTION_SQL_KEYWORDS =
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

        val SET_LINE_KEYWORDS =
            mapOf(
                "into" to setOf("insert"),
                "from" to setOf("delete"),
                "join" to setOf("outer", "inner", "left", "right"),
                "outer" to setOf("left", "right"),
                "inner" to setOf("left", "right"),
                "by" to setOf("group", "order"),
                "and" to setOf("between"),
                "if" to setOf("table"),
            )

        fun isSetLineKeyword(
            keyword: String,
            prevKeyword: String,
        ): Boolean = SET_LINE_KEYWORDS[keyword.lowercase()]?.contains(prevKeyword.lowercase()) == true
    }
}
