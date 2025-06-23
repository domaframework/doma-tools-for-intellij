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
package org.domaframework.doma.intellij.inspection.dao.processor.returntype

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.common.validation.result.ValidationResult
import org.domaframework.doma.intellij.common.validation.result.ValidationReturnTypeImmutableResult
import org.domaframework.doma.intellij.common.validation.result.ValidationReturnTypeResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getClassAnnotation
import org.domaframework.doma.intellij.inspection.dao.processor.TypeCheckerProcessor

abstract class ReturnTypeCheckerProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : TypeCheckerProcessor(psiDaoMethod) {
    protected val returningFqn = DomaClassName.RETURNING.className
    protected val returnType = psiDaoMethod.psiMethod.returnType

    abstract fun checkReturnType(): ValidationResult?

    protected fun isImmutableEntity(canonicalText: String): Boolean {
        val returnTypeClass = method.project.getJavaClazz(canonicalText)
        val entity = returnTypeClass?.getClassAnnotation(DomaClassName.ENTITY.className) ?: return false
        return entity.let { entity ->
            AnnotationUtil.getBooleanAttributeValue(entity, "immutable") == true
        } == true ||
            returnTypeClass.isRecord == true
    }

    protected fun hasReturingOption(): Boolean {
        val methodAnnotation: PsiAnnotation =
            getAnnotation(psiDaoMethod.daoType.fqdn) ?: return false
        val returningOption: PsiAnnotation? = getDaoAnnotationOption(methodAnnotation, "returning")
        return returningOption?.nameReferenceElement?.qualifiedName == returningFqn
    }

    protected fun generatePsiTypeReturnTypeResult(methodOtherReturnType: PsiType): ValidationResult? =
        if (returnType != methodOtherReturnType) {
            ValidationReturnTypeResult(
                method.nameIdentifier,
                shortName,
                methodOtherReturnType.presentableText,
            )
        } else {
            null
        }

    protected fun generateResultReturnTypeImmutable(
        annotation: DomaAnnotationType,
        immutableParamClassType: PsiType,
        methodResultClassName: String,
        typeName: String,
    ): ValidationResult? {
        val actualResultTypeName =
            "$methodResultClassName<${immutableParamClassType.canonicalText}>"
        return if (returnType?.canonicalText != actualResultTypeName) {
            ValidationReturnTypeImmutableResult(
                method.nameIdentifier,
                shortName,
                annotation.name,
                typeName,
                immutableParamClassType.presentableText,
            )
        } else {
            null
        }
    }
}
