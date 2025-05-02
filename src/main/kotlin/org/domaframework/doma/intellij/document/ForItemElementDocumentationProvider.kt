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
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.psi.getForItem
import org.domaframework.doma.intellij.inspection.ForDirectiveInspection
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr
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

        val file = originalElement.containingFile
        val daoMethod = findDaoMethod(file) ?: return ""
        val forDirectiveInspection = ForDirectiveInspection(daoMethod, "")

        val currentForItem = ForItem(originalElement)
        val forDirectiveExpr = currentForItem.getParentForDirectiveExpr()
        if (forDirectiveExpr != null) {
            val declarationClassType =
                forDirectiveInspection.validateFieldAccessByForItem(
                    listOf(originalElement),
                    skipSelf = false,
                )
            if (declarationClassType != null) {
                generateDocumentInForDirective(
                    forDirectiveInspection,
                    declarationClassType,
                    forDirectiveExpr,
                    originalElement,
                    result,
                )
            }
        } else {
            generateDocumentInForItemVariable(
                forDirectiveInspection,
                originalElement,
                result,
            ) { parent -> return@generateDocumentInForItemVariable parent }
        }
        return result.joinToString("\n")
    }

    private fun generateDocumentInForItemVariable(
        forDirectiveInspection: ForDirectiveInspection,
        originalElement: PsiElement,
        result: MutableList<String?>,
        nestClass: (PsiParentClass?) -> PsiParentClass?,
    ) {
        val declarationClassType =
            forDirectiveInspection.validateFieldAccessByForItem(listOf(originalElement))
        if (declarationClassType != null) {
            val parentClass = nestClass(declarationClassType.parentClass)
            result.add("${generateTypeLink(parentClass)} ${originalElement.text}")
        }
    }

    private fun generateDocumentInForDirective(
        forDirectiveInspection: ForDirectiveInspection,
        declarationClassType: ValidationResult,
        forDirectiveExpr: SqlElForDirective,
        originalElement: PsiElement,
        result: MutableList<String?>,
    ) {
        val parentClass = declarationClassType.parentClass
        val parentType = parentClass?.type as? PsiClassType

        if (forDirectiveExpr.getForItem()?.textOffset != originalElement.textOffset) {
            generateDocumentForItemSelf(parentType, originalElement, result)
        } else {
            generateDocumentInForItemVariable(forDirectiveInspection, originalElement, result) { parent ->
                val nestClass =
                    (parent?.type as? PsiClassType)?.parameters?.firstOrNull()
                        ?: return@generateDocumentInForItemVariable parent
                return@generateDocumentInForItemVariable PsiParentClass(nestClass)
            }
        }
    }

    private fun generateDocumentForItemSelf(
        parentType: PsiClassType?,
        originalElement: PsiElement,
        result: MutableList<String?>,
    ) {
        if (parentType != null) {
            val forItemParentClassType = PsiParentClass(parentType)
            result.add("${generateTypeLink(forItemParentClassType)} ${originalElement.text}")
        }
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
