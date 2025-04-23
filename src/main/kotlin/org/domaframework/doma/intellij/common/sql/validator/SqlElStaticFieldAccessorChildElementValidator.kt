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
package org.domaframework.doma.intellij.common.sql.validator

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationIgnoreResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.fqdn
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.jetbrains.kotlin.idea.base.util.module

class SqlElStaticFieldAccessorChildElementValidator(
    override val blocks: List<PsiElement>,
    private val staticAccuser: SqlElStaticFieldAccessExpr,
    override val shorName: String,
) : SqlElChildElementValidator(blocks, shorName) {
    override fun validateChildren(
        dropIndex: Int,
        findFieldMethod: (PsiType) -> PsiParentClass,
        complete: (PsiParentClass) -> Unit,
    ): ValidationResult? {
        val errorElement = getParent()
        when (errorElement) {
            is ValidationCompleteResult -> {
                val parent = errorElement.parentClass
                return validateFieldAccess(
                    parent,
                    complete = complete,
                )
            }
            is ValidationIgnoreResult -> return null
            else -> return errorElement
        }
    }

    override fun validateChildren(dropIndex: Int): ValidationResult? {
        val getParentResult = getParent()
        when (getParentResult) {
            is ValidationCompleteResult -> {
                val parent = getParentResult.parentClass
                return validateFieldAccess(parent)
            }
            is ValidationIgnoreResult -> return null
            else -> return getParentResult
        }
    }

    private fun getParent(): ValidationResult {
        val staticTopElement =
            blocks.firstOrNull()
                ?: return ValidationIgnoreResult(blocks.firstOrNull())
        val module = staticAccuser.module ?: return ValidationIgnoreResult(staticTopElement)
        val fqdn = staticAccuser.fqdn
        val clazz =
            module.getJavaClazz(false, fqdn)
                ?: return ValidationIgnoreResult(staticTopElement)

        var parent = PsiParentClass(clazz.psiClassType)
        val nextSibling = staticTopElement.nextSibling
        val topField =
            if (nextSibling !is SqlElParameters) {
                parent.findStaticField(staticTopElement.text)
            } else {
                null
            }
        val topMethod =
            if (nextSibling is SqlElParameters) {
                parent.findStaticMethod(staticTopElement.text)
            } else {
                null
            }
        if (topField == null && topMethod == null) {
            return ValidationPropertyResult(
                staticTopElement,
                parent,
                shorName,
            )
        }

        (topField?.type ?: topMethod?.returnType)
            ?.let { parent = PsiParentClass(it) }
            ?: return ValidationIgnoreResult(staticTopElement)

        return ValidationCompleteResult(staticTopElement, parent)
    }
}
