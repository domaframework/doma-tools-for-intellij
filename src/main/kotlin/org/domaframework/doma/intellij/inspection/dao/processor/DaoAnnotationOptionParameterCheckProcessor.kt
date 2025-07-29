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

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiLiteralExpression
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationAnnotationOptionParameterResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.isEntity

/**
 * Processor for checking annotation option parameters in DAO methods.
 * This class validates the include and exclude options in @Update and @BatchUpdate annotations.
 */
class DaoAnnotationOptionParameterCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : TypeCheckerProcessor(psiDaoMethod) {
    fun checkAnnotationOptions(holder: ProblemsHolder) {
        val annotation =
            when (psiDaoMethod.daoType) {
                DomaAnnotationType.Update -> getAnnotation(DomaAnnotationType.Update.fqdn)
                DomaAnnotationType.BatchUpdate -> getAnnotation(DomaAnnotationType.BatchUpdate.fqdn)
                else -> null
            } ?: return

        // Check if sqlFile option is true - if so, include/exclude are not referenced
        val sqlFileValue = AnnotationUtil.getBooleanAttributeValue(annotation, "sqlFile")
        if (sqlFileValue == true) {
            return
        }

        // Get the entity parameter
        val entityParam = method.parameterList.parameters.find { TypeUtil.isEntity(it.type, method.project) } ?: return
        var entityType = entityParam.type

        // For batch operations, extract the element type from Iterable
        if (psiDaoMethod.daoType == DomaAnnotationType.BatchUpdate) {
            val paramClassType = entityType as? PsiClassType
            val iterableParam = paramClassType?.parameters?.firstOrNull()
            if (iterableParam != null) {
                entityType = iterableParam
            }
        }

        // Check if the parameter is an entity
        if (!TypeUtil.isEntity(entityType, project)) {
            return
        }

        // Get entity class
        val entityClass = project.getJavaClazz(entityType.canonicalText) ?: return
        if (!entityClass.isEntity()) {
            return
        }

        // Check include option
        checkArrayOption(annotation, "include", entityClass, holder)

        // Check exclude option
        checkArrayOption(annotation, "exclude", entityClass, holder)

        // TODO: Do the same check for the Returning option
    }

    private fun checkArrayOption(
        annotation: PsiAnnotation,
        optionName: String,
        entityClass: PsiClass,
        holder: ProblemsHolder,
    ) {
        val arrayValues =
            annotation.parameterList.attributes
                .find { it.name == optionName }
                ?.value
                ?.children
                ?.filter { it is PsiLiteralExpression } ?: return
        if (arrayValues.isEmpty()) return

        val project = method.project
        arrayValues.map { fields ->
            val valueFields = fields.text.replace("\"", "").split(".")
            var searchParamClass: PsiClass? = entityClass
            valueFields.map { field ->
                searchParamClass?.fields?.find { it.name == field }?.let { f ->
                    searchParamClass = project.getJavaClazz(f.type.canonicalText) ?: return@map
                } ?: run {
                    ValidationAnnotationOptionParameterResult(
                        fields,
                        shortName,
                        field,
                        optionName,
                        searchParamClass?.name ?: "Unknown",
                        searchParamClass?.fields?.joinToString(", ") { it.name.substringAfter(":") } ?: "No fields found",
                    ).highlightElement(holder)
                }
            }
        }
    }
}
