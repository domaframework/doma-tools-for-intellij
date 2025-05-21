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
package org.domaframework.doma.intellij.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an implementation of a SQL EL (Expression Language) comment expression in the PSI
 * (Program Structure Interface). This implementation serves as a base class for more specialized EL
 * comment expressions and inherits from ASTWrapperPsiElement. It also implements
 * SqlCustomElCommentExpr, which designates it as a PSI comment in the PSI tree.
 *
 * <p>The main purpose of this class is to provide methods to interact with the underlying AST node,
 * define its token type, and delegate visitor-specific behavior for handling SQL EL comment
 * expressions.
 *
 * <p>Key Responsibilities: - Retrieve the underlying AST node associated with this PSI element. -
 * Identify the element type of the token in the AST. - Implement visitor pattern methods for
 * processing SQL expressions and comments.
 */
public class SqlElCommentExprImpl extends ASTWrapperPsiElement implements SqlCustomElCommentExpr {

  public SqlElCommentExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull IElementType getTokenType() {
    return getNode().getElementType();
  }

  public void accept(@NotNull SqlVisitor visitor) {
    visitor.visitElExpr((SqlElExpr) this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof SqlVisitor) accept((SqlVisitor) visitor);
    else super.accept(visitor);
  }
}
