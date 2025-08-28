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
package org.domaframework.doma.intellij.tokens;

import java.util.HashSet;
import java.util.Set;

/** SQL Keyword Token Provider */
public class SqlKeywordTokenUtil {
  static final Set<String> TOKENS = new HashSet<>();

  static {
    TOKENS.addAll(
        Set.of(
            "add",
            "after",
            "alter",
            "all",
            "and",
            "as",
            "asc",
            "between",
            "breadth",
            "by",
            "case",
            "change",
            "check",
            "collate",
            "column",
            "comment",
            "conflict",
            "constraint",
            "create",
            "cross",
            "cycle",
            "database",
            "default",
            "delete",
            "desc",
            "distinct",
            "do",
            "drop",
            "else",
            "end",
            "except",
            "exists",
            "first",
            "following",
            "foreign",
            "from",
            "full",
            "group",
            "having",
            "if",
            "in",
            "index",
            "inner",
            "insert",
            "intersect",
            "into",
            "is",
            "join",
            "key",
            "lateral",
            "left",
            "like",
            "limit",
            "materialized",
            "modify",
            "not",
            "nothing",
            "null",
            "offset",
            "on",
            "or",
            "order",
            "outer",
            "partition",
            "preceding",
            "primary",
            "range",
            "recursive",
            "references",
            "rename",
            "returning",
            "right",
            "row",
            "rows",
            "search",
            "select",
            "set",
            "table",
            "temporary",
            "then",
            "to",
            "truncate",
            "unbounded",
            "union",
            "unique",
            "update",
            "using",
            "values",
            "view",
            "when",
            "where",
            "with",
            "within"));
  }

  public static Set<String> getTokens() {
    return TOKENS;
  }
}
