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
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.expr.accessElementsPrevOriginalElement
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
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
        if (staticFieldAccessExpr != null) {
            generateStaticFieldDocument(
                staticFieldAccessExpr,
                file,
                originalElement,
                project,
                result,
            )
        } else {
            generateDaoFieldAccessDocument(originalElement, project, result)
        }
        return result.joinToString("\n")
    }

    private fun generateDaoFieldAccessDocument(
        originalElement: PsiElement,
        project: Project,
        result: MutableList<String?>,
    ) {
        var topParentType: PsiParentClass? = null
        val selfSkip = isSelfSkip(originalElement)
        val forDirectives = ForDirectiveUtil.getForDirectiveBlocks(originalElement, selfSkip)
        val fieldAccessExpr =
            PsiTreeUtil.getParentOfType(
                originalElement,
                SqlElFieldAccessExpr::class.java,
            )
        val fieldAccessBlocks =
            fieldAccessExpr?.accessElementsPrevOriginalElement(originalElement.textOffset)
        val searchElement = fieldAccessBlocks?.firstOrNull() ?: originalElement

        var isBatchAnnotation = false
        if (ForDirectiveUtil.findForItem(searchElement, forDirectives = forDirectives) != null) {
            topParentType = ForDirectiveUtil.getForDirectiveItemClassType(project, forDirectives)
        } else {
            val daoMethod = findDaoMethod(originalElement.containingFile) ?: return
            val param = daoMethod.findParameter(originalElement.text) ?: return
            isBatchAnnotation = PsiDaoMethod(project, daoMethod).daoType.isBatchAnnotation()
            topParentType = PsiParentClass(param.type)
        }
        if (fieldAccessExpr != null && fieldAccessBlocks != null) {
            topParentType?.let {
                ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                    fieldAccessBlocks,
                    project,
                    it,
                    isBatchAnnotation = isBatchAnnotation,
                    complete = { lastType ->
                        result.add("${generateTypeLink(lastType)} ${originalElement.text}")
                    },
                )
            }
            return
        }
        result.add("${generateTypeLink(topParentType)} ${originalElement.text}")
    }

    private fun generateStaticFieldDocument(
        staticFieldAccessExpr: SqlElStaticFieldAccessExpr,
        file: PsiFile,
        originalElement: PsiElement,
        project: Project,
        result: MutableList<String?>,
    ) {
        val fieldAccessBlocks = staticFieldAccessExpr.accessElements
        val staticElement = PsiStaticElement(fieldAccessBlocks, file)
        val referenceClass = staticElement.getRefClazz() ?: return
        if (PsiTreeUtil.getParentOfType(originalElement, SqlElClass::class.java) != null) {
            val clazzType = PsiParentClass(referenceClass.psiClassType)
            result.add("${generateTypeLink(clazzType)} ${originalElement.text}")
            return
        }

        ForDirectiveUtil.getFieldAccessLastPropertyClassType(
            fieldAccessBlocks.filter { it.textOffset <= originalElement.textOffset },
            project,
            PsiParentClass(referenceClass.psiClassType),
            complete = { lastType ->
                result.add("${generateTypeLink(lastType)} ${originalElement.text}")
            },
        )
    }

    private fun isSelfSkip(targetElement: PsiElement): Boolean {
        val forItem = ForItem(targetElement)
        val forDirectiveExpr = forItem.getParentForDirectiveExpr()
        return !(forDirectiveExpr != null && forDirectiveExpr.getForItem()?.textOffset == targetElement.textOffset)
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

    private fun generateTypeLink(parentClass: PsiParentClass?): String {
        if (parentClass?.type != null) {
            return generateTypeLinkFromCanonicalText(parentClass.type.canonicalText)
        }
        return ""
    }

    private fun generateTypeLinkFromCanonicalText(canonicalText: String): String {
        val regex = Regex("([a-zA-Z0-9_]+\\.)*([a-zA-Z0-9_]+)")
        val result = StringBuilder()
        var lastIndex = 0

        for (match in regex.findAll(canonicalText)) {
            val fullMatch = match.value
            val typeName = match.groups[2]?.value ?: fullMatch
            val startIndex = match.range.first
            val endIndex = match.range.last + 1

            if (lastIndex < startIndex) {
                result.append(canonicalText.substring(lastIndex, startIndex))
            }
            result.append("<a href=\"psi_element://$fullMatch\">$typeName</a>")
            lastIndex = endIndex
        }

        if (lastIndex < canonicalText.length) {
            result.append(canonicalText.substring(lastIndex))
        }

        return result.toString()
    }
}
