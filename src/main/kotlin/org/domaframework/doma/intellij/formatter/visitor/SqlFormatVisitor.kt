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
package org.domaframework.doma.intellij.formatter.visitor

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.extension.expr.isConditionOrLoopDirective
import org.domaframework.doma.intellij.psi.SqlBlockComment
import org.domaframework.doma.intellij.psi.SqlCustomElCommentExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlFormatVisitor : PsiRecursiveElementVisitor() {
    val replaces = mutableListOf<PsiElement>()
    var lastElement: PsiElement? = null

    override fun visitElement(element: PsiElement) {
        super.visitElement(element)
        if (element !is PsiFile && element.nextSibling == null) {
            lastElement = element
        }

        if (PsiTreeUtil.getParentOfType(element, SqlBlockComment::class.java) == null) {
            when (element.elementType) {
                SqlTypes.KEYWORD, SqlTypes.COMMA, SqlTypes.LEFT_PAREN, SqlTypes.RIGHT_PAREN, SqlTypes.WORD -> {
                    replaces.add(element)
                }

                SqlTypes.OTHER -> {
                    if (element.text == "=") {
                        val updateSetKeyword =
                            replaces
                                .lastOrNull { it.elementType == SqlTypes.KEYWORD }
                        if (updateSetKeyword?.text?.lowercase() == "set") {
                            replaces.add(element)
                        }
                    }
                }

                SqlTypes.BLOCK_COMMENT ->
                    if (
                        element is SqlCustomElCommentExpr &&
                        element.isConditionOrLoopDirective()
                    ) {
                        replaces.add(element)
                    }
            }
        }
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        super.visitWhiteSpace(space)
        if (PsiTreeUtil.getParentOfType(space, SqlBlockComment::class.java) == null) {
            replaces.add(space)
        }
        return

        val nextElement = space.nextSibling
        if (nextElement != null &&
            (
                space.text.contains("\n") ||
                    nextElement.elementType == SqlTypes.LINE_COMMENT ||
                    nextElement.elementType == SqlTypes.BLOCK_COMMENT
            )
        ) {
            replaces.add(space)
        }
    }
}
