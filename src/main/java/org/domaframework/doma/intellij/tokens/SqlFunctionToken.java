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

/** Default SQL Function Token */
public class SqlFunctionToken {
  static final Set<String> TOKENS = new HashSet<>();

  static {
    TOKENS.addAll(
        Set.of(
            "coalesce",
            "row_number",
            "row_count",
            "sum",
            "avg",
            "count",
            "max",
            "min",
            "log",
            "substring",
            "trim",
            "ltrim",
            "rtlim",
            "trim_array",
            "btrim",
            "replace",
            "regexp_replace",
            "over",
            "rank",
            "percent_rank",
            "dense_rank",
            "current_date",
            "current_timestamp",
            "now",
            "mod"));
  }

  public static Set<String> getTokens() {
    return TOKENS;
  }
}
