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
package org.domaframework.doma.intellij;

import java.util.HashSet;
import java.util.Set;
import org.domaframework.doma.intellij.tokens.MySqlFunctionToken;
import org.domaframework.doma.intellij.tokens.OracleFunctionToken;
import org.domaframework.doma.intellij.tokens.PostgresSqlFunctionToken;
import org.domaframework.doma.intellij.tokens.SqlDataTypeTokenUtil;
import org.domaframework.doma.intellij.tokens.SqlFunctionToken;
import org.domaframework.doma.intellij.tokens.SqlKeywordTokenUtil;

public class SqlTokenHelper {

  public static Set<String> getKeyword() {
    return SqlKeywordTokenUtil.getTokens();
  }

  public static Set<String> getDataTypeTokens() {
    return SqlDataTypeTokenUtil.getTokens();
  }

  // Functions
  public static Set<String> getFunctionTokens() {
    Set<String> tokens = new HashSet<>(SqlFunctionToken.getTokens());
    tokens.addAll(SqlFunctionToken.getTokens());
    tokens.addAll(PostgresSqlFunctionToken.getTokens());
    tokens.addAll(MySqlFunctionToken.getTokens());
    tokens.addAll(OracleFunctionToken.getTokens());

    return tokens;
  }
}
