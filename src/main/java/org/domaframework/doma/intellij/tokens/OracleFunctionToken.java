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
    TOKENS.addAll(
        Set.of(
            "abs",
            "ceil",
            "floor",
            "round",
            "power",
            "sqrt",
            "exp",
            "log",
            "to_char",
            "to_date",
            "to_number",
            "current_date",
            "current_timestamp",
            "sysdate"));
  }

  public static Set<String> getTokens() {
    return TOKENS;
  }
}
