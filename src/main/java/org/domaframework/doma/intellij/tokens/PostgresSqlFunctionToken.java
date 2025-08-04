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

/** PostgresSQL specific functions */
public class PostgresSqlFunctionToken {
  static final Set<String> TOKENS = new HashSet<>();

  static {
    // Mathematical Functions
    {
      TOKENS.addAll(
          Set.of(
              "abs",
              "cbrt",
              "ceil",
              "ceiling",
              "degrees",
              "div",
              "erf",
              "erfc",
              "exp",
              "factorial",
              "floor",
              "gcd",
              "lcm",
              "ln",
              "log",
              "log10",
              "mod",
              "pi",
              "power",
              "radians",
              "round",
              "scale",
              "sign",
              "sqrt",
              "trim_scale",
              "trunc",
              "width_bucket"));
    }
    // Random Functions
    {
      TOKENS.addAll(Set.of("random", "random_normal", "setseed"));
    }
    // Trigonometric Functions
    {
      TOKENS.addAll(
          Set.of(
              "acos", "acosd", "asin", "asind", "atan", "atand", "atan2", "atan2d", "cos", "cosd",
              "cot", "cotd", "sin", "sind", "tan", "tand", "sinh", "cosh", "tanh", "asinh", "acosh",
              "atanh"));
    }
    // String Functions
    {
      TOKENS.addAll(
          Set.of(
              "bit_length",
              "octet_length",
              "length",
              "substring",
              "overlay",
              "position",
              "trim",
              "get_bit",
              "set_bit",
              "get_byte",
              "set_byte",
              "convert",
              "convert_from",
              "convert_to",
              "decode",
              "encode"));
    }
    // Date/Time Functions
    {
      TOKENS.addAll(
          Set.of(
              "age",
              "clock_timestamp",
              "date_part",
              "date_trunc",
              "extract",
              "isfinite",
              "justify_days",
              "justify_hours",
              "justify_interval",
              "make_date",
              "make_interval",
              "make_timestamp",
              "make_time",
              "make_timestamptz",
              "now",
              "statement_timestamp",
              "timeofday",
              "transaction_timestamp",
              "to_char",
              "to_date",
              "to_timestamp",
              "to_number",
              "ntile",
              "cume_dist",
              "percent_rank",
              "lead",
              "first_value",
              "last_value",
              "nth_value"));
    }
  }

  public static Set<String> getTokens() {
    return TOKENS;
  }
}
