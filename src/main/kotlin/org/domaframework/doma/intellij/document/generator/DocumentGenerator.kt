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
package org.domaframework.doma.intellij.document.generator

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil
import org.domaframework.doma.intellij.common.sql.foritem.ForItem
import org.domaframework.doma.intellij.extension.psi.getForItem

abstract class DocumentGenerator(
    open val project: Project,
) {
    abstract fun generateDocument()

    protected fun isSelfSkip(targetElement: PsiElement): Boolean {
        val forItem = ForItem(targetElement)
        val forDirectiveExpr = forItem.getParentForDirectiveExpr()
        return !(forDirectiveExpr != null && forDirectiveExpr.getForItem()?.textOffset == targetElement.textOffset)
    }

    protected fun generateTypeLink(parentClass: PsiParentClass?): String {
        val parentClassType = parentClass?.type
        if (parentClassType != null) {
            val convertOptionalType = PsiClassTypeUtil.convertOptionalType(parentClassType, project)
            return generateTypeLinkFromCanonicalText(convertOptionalType.canonicalText)
        }
        return ""
    }

    private fun generateTypeLinkFromCanonicalText(canonicalText: String): String {
        val regex = Regex("([a-zA-Z0-9_]+\\.)*([a-zA-Z0-9_]+)")
        val result = StringBuilder()
        var lastIndex = 0
        val optionalPackage = "java.util.Optional"
        val optionalTypeMap =
            listOf(
                optionalPackage,
                "${optionalPackage}Int",
                "${optionalPackage}Double",
                "${optionalPackage}Long",
            )
        var skipCount = 0

        for (match in regex.findAll(canonicalText)) {
            val fullMatch = match.value
            val optionalSkip = optionalTypeMap.contains(fullMatch)
            if (optionalSkip) skipCount++

            val typeName = match.groups[2]?.value ?: fullMatch
            val startIndex = match.range.first
            val endIndex = match.range.last + 1

            if (lastIndex < startIndex && !optionalSkip) {
                result.append(canonicalText.substring(lastIndex, startIndex))
            }
            if (!optionalSkip) result.append("<a href=\"psi_element://$fullMatch\">$typeName</a>")
            lastIndex = endIndex
        }

        if (lastIndex + skipCount < canonicalText.length) {
            result.append(canonicalText.substring(lastIndex + skipCount))
        }

        return result.toString()
    }
}
