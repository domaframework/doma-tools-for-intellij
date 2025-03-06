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

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.domaframework.doma.intellij.SqlParser;
import org.domaframework.doma.intellij.psi.SqlFile;
import org.domaframework.doma.intellij.psi.SqlTokenSets;
import org.domaframework.doma.intellij.psi.SqlTypes;
import org.jetbrains.annotations.NotNull;

public final class SqlParserDefinition implements ParserDefinition {
  public static final IFileElementType FILE = new IFileElementType(SqlLanguage.INSTANCE);

  @Override
  public @NotNull Lexer createLexer(Project project) {
    return new SqlLexerAdapter();
  }

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new SqlParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull TokenSet getCommentTokens() {
    return SqlTokenSets.COMMENTS;
  }

  @Override
  public @NotNull TokenSet getStringLiteralElements() {
    return TokenSet.EMPTY;
  }

  @Override
  public @NotNull PsiElement createElement(ASTNode astNode) {
    return SqlTypes.Factory.createElement(astNode);
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider fileViewProvider) {
    return new SqlFile(fileViewProvider);
  }
}
