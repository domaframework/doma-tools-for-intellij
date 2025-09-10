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
package org.domaframework.doma.intellij.inspection.sql.processor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.DummyPsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationClassPathResult
import org.domaframework.doma.intellij.common.validation.result.ValidationNotFoundStaticPropertyResult
import org.domaframework.doma.intellij.common.validation.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

class InspectionStaticFieldAccessVisitorProcessor(
    val shortName: String,
) : InspectionVisitorProcessor() {
    /**
     * Check for existence of static field
     */
    fun checkBindVariableDefine(
        staticAccessor: SqlElStaticFieldAccessExpr,
        holder: ProblemsHolder,
    ) {
        val referenceClass =
            resolveReferenceClass(staticAccessor)
        if (referenceClass == null) {
            highlightClassPathError(staticAccessor, holder)
            return
        }

        val topParentClass =
            resolveTopParentClass(staticAccessor, referenceClass, shortName)
        when (topParentClass.first) {
            is DummyPsiParentClass -> {
                if (topParentClass.second is ValidationPropertyResult) {
                    highlightPropertyNotFoundError(staticAccessor, holder)
                } else {
                    topParentClass.second?.highlightElement(holder)
                }
                return
            }
            null -> {
                highlightPropertyNotFoundError(staticAccessor, holder)
                return
            }

            else -> {
                val parent: PsiParentClass = topParentClass.first ?: return
                val result = checkFieldAccessValidity(staticAccessor, parent)
                result?.highlightElement(holder)
            }
        }
    }

    private fun resolveReferenceClass(staticAccessor: SqlElStaticFieldAccessExpr): PsiClass? {
        val psiStaticClass =
            PsiStaticElement(
                staticAccessor.elClass.elIdExprList,
                staticAccessor.containingFile,
            )

        val referenceClass = psiStaticClass.getRefClazz()

        return referenceClass
    }

    private fun highlightClassPathError(
        staticAccessor: SqlElStaticFieldAccessExpr,
        holder: ProblemsHolder,
    ) {
        ValidationClassPathResult(
            staticAccessor.elClass,
            shortName,
        ).highlightElement(holder)
    }

    private fun resolveTopParentClass(
        staticAccessor: SqlElStaticFieldAccessExpr,
        referenceClass: PsiClass,
        shortName: String = "",
    ): Pair<PsiParentClass?, ValidationResult?> {
        val topParentClass =
            ForDirectiveUtil.getStaticFieldAccessTopElementClassType(
                staticAccessor,
                referenceClass,
                shortName,
            )

        val result = topParentClass?.validationResult
        if (result != null) {
            topParentClass.parent = DummyPsiParentClass()
        }

        return Pair(topParentClass?.parent, result)
    }

    private fun highlightPropertyNotFoundError(
        staticAccessor: SqlElStaticFieldAccessExpr,
        holder: ProblemsHolder,
    ) {
        val blockElements = staticAccessor.accessElements
        blockElements.firstOrNull()?.let { firstElement ->
            ValidationNotFoundStaticPropertyResult(
                firstElement,
                staticAccessor.elClass.text,
                shortName,
            ).highlightElement(holder)
        }
    }

    /**
     * Get the final class type of the static field access element
     */
    fun getStaticFieldAccessLastPropertyClassType(staticAccessor: SqlElStaticFieldAccessExpr): PsiType? {
        val referenceClass =
            resolveReferenceClass(staticAccessor)
                ?: return null

        val topParentClass =
            resolveTopParentClass(staticAccessor, referenceClass)
        if (topParentClass.first == null || topParentClass.first is DummyPsiParentClass) {
            return null
        }
        val parent: PsiParentClass = topParentClass.first ?: return null

        val result = getStaticFieldAccessLastProperty(staticAccessor, parent)
        return result.type
    }

    private fun checkFieldAccessValidity(
        staticAccessor: SqlElStaticFieldAccessExpr,
        topParentClass: PsiParentClass,
    ): ValidationResult? {
        val blockElements = staticAccessor.accessElements
        val result =
            ForDirectiveUtil.getFieldAccessLastPropertyClassType(
                blockElements,
                staticAccessor.project,
                topParentClass,
                shortName = shortName,
            )
        return result
    }

    private fun getStaticFieldAccessLastProperty(
        staticAccessor: SqlElStaticFieldAccessExpr,
        topParentClass: PsiParentClass,
    ): PsiParentClass {
        val blockElements = staticAccessor.accessElements
        var findLastTypeParent = topParentClass
        ForDirectiveUtil.getFieldAccessLastPropertyClassType(
            blockElements,
            staticAccessor.project,
            topParentClass,
            shortName = shortName,
            findFieldMethod = { type ->
                findLastTypeParent = PsiParentClass(type)
                findLastTypeParent
            },
        )
        return findLastTypeParent
    }
}
