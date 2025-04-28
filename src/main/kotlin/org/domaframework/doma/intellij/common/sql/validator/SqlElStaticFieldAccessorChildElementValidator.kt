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
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationCompleteResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationIgnoreResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationPropertyResult
import org.domaframework.doma.intellij.common.sql.validator.result.ValidationResult
import org.domaframework.doma.intellij.extension.expr.fqdn
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElParameters
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

class SqlElStaticFieldAccessorChildElementValidator(
    override val blocks: List<PsiElement>,
    private val staticAccuser: SqlElStaticFieldAccessExpr,
    override val shorName: String = "",
) : SqlElChildElementValidator(blocks, shorName) {
    val project = staticAccuser.containingFile.project

    override fun validateChildren(
        dropIndex: Int,
        findFieldMethod: (PsiType) -> PsiParentClass,
        complete: (PsiParentClass) -> Unit,
    ): ValidationResult? {
        val getParentResult = getFieldTopParent()
        when (getParentResult) {
            is ValidationCompleteResult -> {
                val parent = getParentResult.parentClass
                return validateFieldAccess(
                    project,
                    parent,
                    dropLastIndex = dropIndex,
                    complete = complete,
                )
            }
            is ValidationIgnoreResult -> return null
            else -> return getParentResult
        }
    }

    override fun validateChildren(dropIndex: Int): ValidationResult? {
        val getParentResult = getFieldTopParent()
        when (getParentResult) {
            is ValidationCompleteResult -> {
                val parent = getParentResult.parentClass
                return validateFieldAccess(project, parent, dropLastIndex = dropIndex)
            }
            is ValidationIgnoreResult -> return null
            else -> return getParentResult
        }
    }

    fun getClassType(): PsiParentClass? {
        val fqdn = staticAccuser.fqdn
        val file = staticAccuser.containingFile
        val psiStaticElement = PsiStaticElement(fqdn, file)
        val clazz = psiStaticElement.getRefClazz() ?: return null

        return PsiParentClass(clazz.psiClassType)
    }

    private fun getFieldTopParent(): ValidationResult? {
        var parent = getClassType() ?: return ValidationIgnoreResult(null)
        val staticTopElement =
            blocks.firstOrNull()
                ?: return ValidationCompleteResult(staticAccuser.elClass, parent)
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
