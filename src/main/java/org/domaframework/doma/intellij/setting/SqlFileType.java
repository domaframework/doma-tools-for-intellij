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
package org.domaframework.doma.intellij.setting;

import com.intellij.openapi.fileTypes.LanguageFileType;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public final class SqlFileType extends LanguageFileType {

  public static final SqlFileType INSTANCE = new SqlFileType();

  private SqlFileType() {
    super(SqlLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "DomaSql";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Doma sql template";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "sql";
  }

  @Override
  public Icon getIcon() {
    return SqlIcon.FILE;
  }
}
