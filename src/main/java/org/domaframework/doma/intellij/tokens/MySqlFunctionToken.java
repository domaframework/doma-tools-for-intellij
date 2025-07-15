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

/** MySQL specific functions */
public class MySqlFunctionToken {
  static final Set<String> TOKENS = new HashSet<>();

  static {
    // Date and Time Functions
    {
      TOKENS.addAll(
          Set.of(
              "adddate",
              "addtime",
              "convert_tz",
              "curdate",
              "current_date",
              "current_time",
              "current_timestamp",
              "curtime",
              "date",
              "date_add",
              "date_format",
              "date_sub",
              "datediff",
              "day",
              "dayname",
              "dayofmonth",
              "dayofyear",
              "extract",
              "from_days",
              "from_unixtime",
              "get_format",
              "hour",
              "last_day",
              "localtime",
              "localtimestamp",
              "makedate",
              "maketime",
              "microsecond",
              "minute",
              "month",
              "monthname",
              "now",
              "period_add",
              "period_diff",
              "quarter",
              "sec_to_time",
              "second",
              "str_to_date",
              "subdate",
              "subtime",
              "sysdate",
              "time",
              "time_format",
              "time_to_sec",
              "timediff",
              "timestamp",
              "timestampadd",
              "timestampdiff",
              "to_days",
              "to_seconds",
              "unix_timestamp",
              "unix_timestamp()",
              "utc_date",
              "utc_time",
              "utc_timestamp",
              "week",
              "weekday",
              "weekofyear",
              "year",
              "yearweek"));
    }
    // Mathematical Functions
    {
      TOKENS.addAll(
          Set.of(
              "abs",
              "acos",
              "asin",
              "atan",
              "atan2",
              "ceil",
              "ceiling",
              "conv",
              "cos",
              "cot",
              "crc32",
              "degrees",
              "div",
              "exp",
              "floor",
              "ln",
              "log10",
              "log2",
              "mod",
              "pi",
              "pow",
              "power",
              "radians",
              "rand",
              "sign",
              "sin",
              "sqrt",
              "tan",
              "truncate"));
    }
    // String Functions
    {
      TOKENS.addAll(
          Set.of(
              "ascii",
              "bin",
              "bit_length",
              "char",
              "char_length",
              "character_length",
              "concat",
              "concat_ws",
              "elt",
              "export_set",
              "field",
              "find_in_set",
              "format",
              "from_base64",
              "hex",
              "insert",
              "instr",
              "lcase",
              "left",
              "length",
              "like",
              "load_file",
              "locate",
              "lower",
              "lpad",
              "ltrim",
              "make_set",
              "match",
              "mid",
              "not like",
              "not regexp",
              "oct",
              "octet_length",
              "ord",
              "position",
              "quote",
              "regexp",
              "regexp_instr",
              "regexp_like",
              "regexp_replace",
              "regexp_substr",
              "repeat",
              "replace",
              "reverse",
              "right",
              "rlike",
              "rpad",
              "rtrim",
              "soundex",
              "sounds like",
              "space",
              "strcmp",
              "substr",
              "substring",
              "substring_index",
              "to_base64",
              "trim",
              "ucase",
              "unhex",
              "upper",
              "weight_string"));
    }
    // Convert Functions
    {
      TOKENS.addAll(Set.of("binary", "cast", "convert"));
    }
  }

  public static Set<String> getTokens() {
    return TOKENS;
  }
}
