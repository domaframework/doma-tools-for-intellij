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
package org.domaframework.doma.intellij.inspection.dao.processor

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.common.util.DomaClassName
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.getSuperType
import org.domaframework.doma.intellij.extension.psi.isDomain

/**
 * Abstract base class for type checking processors in DAO inspections.
 *
 * Provides utility methods for retrieving annotations, parameters, and checking types
 * in the context of Doma DAO method inspections.
 *
 * @property method The inspected DAO method.
 * @property project The IntelliJ project instance.
 */
abstract class TypeCheckerProcessor(
    psiDaoMethod: PsiDaoMethod,
) {
    protected val method = psiDaoMethod.psiMethod
    protected val project = method.project

    protected fun getAnnotation(fqName: String): PsiAnnotation? = method.annotations.find { it.qualifiedName == fqName }

    protected fun getMethodParamTargetType(typeName: String): PsiParameter? =
        method.parameterList.parameters.find { param ->
            (param.type as? PsiClassType)?.getSuperType(typeName) != null
        }

    protected fun getDaoAnnotationOption(
        psiAnnotation: PsiAnnotation,
        findOptionName: String,
    ): PsiAnnotation? {
        val returningOption =
            psiAnnotation.parameterList.attributes
                .firstOrNull { param ->
                    param.name == findOptionName
                }?.value as? PsiAnnotation

        return returningOption
    }

    protected fun getDaoPsiReferenceOption(
        psiAnnotation: PsiAnnotation,
        findOptionName: String,
    ): PsiField? {
        val strategy =
            psiAnnotation.parameterList.attributes
                .firstOrNull { param ->
                    param.name == findOptionName
                }?.value as? PsiReferenceExpression

        val field = strategy?.reference?.resolve() as? PsiField

        return field
    }

    open fun checkParamType(paramType: PsiType): Boolean {
        if (PsiTypeChecker.isBaseClassType(paramType)) return true

        if (DomaClassName.isOptionalWrapperType(paramType.canonicalText)) {
            return true
        }

        if (DomaClassName.OPTIONAL.isTargetClassNameStartsWith(paramType.canonicalText)) {
            val paramClassType = paramType as? PsiClassType ?: return false
            val optionalParam = paramClassType.parameters.firstOrNull()
            return optionalParam?.let {
                val optionalParamClass = project.getJavaClazz(it.canonicalText)
                optionalParamClass?.isDomain() == true || PsiTypeChecker.isBaseClassType(it)
            } == true
        }

        val paramClass = project.getJavaClazz(paramType.canonicalText)
        return paramClass?.isDomain() == true
    }

    protected fun checkMapType(paramTypeCanonicalText: String): Boolean {
        val mapClassName = paramTypeCanonicalText.replace(" ", "")
        val mapExpectedType =
            DomaClassName.MAP
                .getGenericParamCanonicalText(
                    DomaClassName.STRING.className,
                    DomaClassName.OBJECT.className,
                ).replace(" ", "")
        return mapClassName == mapExpectedType
    }
}
