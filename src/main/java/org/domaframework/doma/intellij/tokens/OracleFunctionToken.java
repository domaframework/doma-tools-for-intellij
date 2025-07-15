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

/** Oracle specific functions */
public class OracleFunctionToken {
  static final Set<String> TOKENS = new HashSet<>();

  static {
    // Mathematical Functions
    {
      TOKENS.addAll(
          Set.of(
              "abs",
              "ceil",
              "floor",
              "mod",
              "power",
              "sqrt",
              "sign",
              "trunc"
          ));
    }
    // Conversion Functions
    {
      TOKENS.addAll(
          Set.of(
              "asciistr",
              "instr",
              "instrb",
              "instr4",
              "length",
              "lengthb",
              "length4",
              "cast",
              "numtodsinterval",
              "numtoyminterval",
              "to_char",
              "to_date",
              "to_number",
              "unistr"
          ));
    }
    // String Functions
    {
      TOKENS.addAll(
          Set.of(
              "substr",
              "substrb",
              "substr4"
          ));
    }
    // Lob Functions
    {
      TOKENS.addAll(
          Set.of(
              "empty_blob",
              "empty_clob",
              "to_blob",
              "to_clob",
              "to_lob",
              "to_nclob"
          ));
    }
    // Comparison functions
    {
      TOKENS.addAll(
          Set.of(
              "decode",
              "greatest",
              "least",
              "coalesce",
              "nullif",
              "nvl"
          ));
    }
    // Date/Time Functions
    {
      TOKENS.addAll(
          Set.of(
              "add_months",
              "extract",
              "months_between",
              "round",
              "sysdate",
              "getdate",
              "timestampadd",
              "timestampdiff",
              "to_date"
          ));
    }
  }

  public static Set<String> getTokens() {
    return TOKENS;
  }
}
