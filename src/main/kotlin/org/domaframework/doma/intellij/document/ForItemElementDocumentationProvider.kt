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
package org.domaframework.doma.intellij.document

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.document.generator.DocumentDaoParameterGenerator
import org.domaframework.doma.intellij.document.generator.DocumentStaticFieldGenerator
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.toml.lang.psi.ext.elementType
import java.util.LinkedList

class ForItemElementDocumentationProvider : AbstractDocumentationProvider() {
    override fun generateDoc(
        element: PsiElement?,
        originalElement: PsiElement?,
    ): String? {
        val result: MutableList<String?> = LinkedList<String?>()
        if (originalElement !is SqlElIdExpr && originalElement?.elementType != SqlTypes.EL_IDENTIFIER) {
            return super.generateDoc(
                element,
                originalElement,
            )
        }
        val project = originalElement.project
        val file = originalElement.containingFile

        val staticFieldAccessExpr =
            PsiTreeUtil.getParentOfType(originalElement, SqlElStaticFieldAccessExpr::class.java)
        val generator =
            if (staticFieldAccessExpr != null) {
                DocumentStaticFieldGenerator(
                    originalElement,
                    project,
                    result,
                    staticFieldAccessExpr,
                    file,
                )
            } else {
                DocumentDaoParameterGenerator(
                    originalElement,
                    project,
                    result,
                )
            }

        generator.generateDocument()

        return result.joinToString("\n")
    }

    override fun generateHoverDoc(
        element: PsiElement,
        originalElement: PsiElement?,
    ): String? = generateDoc(element, originalElement)

    override fun getQuickNavigateInfo(
        element: PsiElement,
        originalElement: PsiElement?,
    ): String? {
        val result: MutableList<String?> = LinkedList<String?>()
        val typeDocument = generateDoc(element, originalElement)
        result.add(typeDocument)
        return result.joinToString("\n")
    }
}
