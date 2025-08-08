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
package org.domaframework.doma.intellij.inspection.dao.processor.option

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationAnnotationOptionEmbeddableResult
import org.domaframework.doma.intellij.common.validation.result.ValidationAnnotationOptionParameterResult
import org.domaframework.doma.intellij.common.validation.result.ValidationAnnotationOptionPrimitiveFieldResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.isEmbeddable
import org.domaframework.doma.intellij.extension.psi.isEntity
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.inspection.dao.processor.TypeCheckerProcessor

/**
 * Processor for checking annotation option parameters in DAO methods.
 * This class validates the include and exclude options in @Update and @BatchUpdate annotations.
 */
class DaoAnnotationOptionParameterCheckProcessor(
    private val psiDaoMethod: PsiDaoMethod,
    private val shortName: String,
) : TypeCheckerProcessor(psiDaoMethod) {
    fun checkAnnotationOptions(holder: ProblemsHolder) {
        val annotation = getAnnotation(psiDaoMethod.daoType.fqdn) ?: return

        // Check if sqlFile option is true - if so, include/exclude are not referenced
        if (psiDaoMethod.isUseSqlFileMethod() || psiDaoMethod.useSqlAnnotation()) {
            return
        }

        // Get the entity parameter
        val methodParams = method.parameterList.parameters
        val entityParam = methodParams.firstOrNull() ?: return
        var entityType = entityParam.type

        // For batch operations, extract the element type from Iterable
        if (psiDaoMethod.daoType.isBatchAnnotation() || psiDaoMethod.daoType == DomaAnnotationType.MultiInsert) {
            val paramClassType = entityType as? PsiClassType
            val iterableParam = paramClassType?.parameters?.firstOrNull()
            if (iterableParam != null) {
                entityType = iterableParam
            }
        }

        val entityClass = project.getJavaClazz(entityType) ?: return
        if (!entityClass.isEntity()) {
            return
        }

        checkAnnotationOptions(annotation, entityClass, holder)

        val returningOption = getDaoAnnotationOption(annotation, "returning") ?: return
        if (hasReturingOption()) {
            checkAnnotationOptions(returningOption, entityClass, holder)
        }
    }

    private fun checkAnnotationOptions(
        annotation: PsiAnnotation,
        entityClass: PsiClass,
        holder: ProblemsHolder,
    ) {
        checkArrayOption(annotation, "include", entityClass, holder)
        checkArrayOption(annotation, "exclude", entityClass, holder)
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
            var searchParamType: PsiType = entityClass.psiClassType
            var searchParamClass: PsiClass? = project.getJavaClazz(searchParamType)
            var hasError = false

            valueFields.forEachIndexed { index, field ->
                val currentField =
                    searchParamClass
                        ?.fields
                        ?.find { property -> isOptionTargetProperty(property, field, project) }
                if (searchParamType is PsiPrimitiveType) {
                    // This is a primitive/basic type but there are more fields after it
                    ValidationAnnotationOptionPrimitiveFieldResult(
                        fields,
                        shortName,
                        fields.text.replace("\"", ""),
                        field,
                        optionName,
                        field,
                    ).highlightElement(holder)
                    hasError = true
                    return@map
                } else {
                    if (currentField != null) {
                        searchParamType = currentField.type
                        searchParamClass = project.getJavaClazz(searchParamType)
                    } else {
                        ValidationAnnotationOptionParameterResult(
                            fields,
                            shortName,
                            field,
                            optionName,
                            searchParamClass?.name ?: "Unknown",
                            getTargetOptionProperties(searchParamClass),
                        ).highlightElement(holder)
                        hasError = true
                        return@map
                    }
                }
            }
            // Error if the last field is Embeddable
            if (searchParamClass?.isEmbeddable() == true) {
                ValidationAnnotationOptionEmbeddableResult(
                    fields,
                    shortName,
                    valueFields.lastOrNull() ?: "Unknown",
                    optionName,
                    searchParamClass.name ?: "Unknown",
                    getEmbeddableProperties(searchParamClass),
                ).highlightElement(holder)
            }
        }
    }

    private fun getTargetOptionProperties(paramClass: PsiClass?) =
        paramClass?.fields?.filter { isOptionTargetProperty(it, it.name, project) }?.joinToString(", ") { it.name.substringAfter(":") }
            ?: "No fields found"

    private fun getEmbeddableProperties(embeddableClass: PsiClass?) =
        embeddableClass
            ?.fields
            ?.filter { !TypeUtil.isEntity(it.type, project) && !TypeUtil.isEmbeddable(it.type, project) }
            ?.joinToString(", ") { it.name }
            ?: "No properties found"

    private fun isOptionTargetProperty(
        field: PsiField,
        optionPropertyName: String,
        project: Project,
    ): Boolean =
        (
            field.name == optionPropertyName && (
                !TypeUtil.isEntity(field.type, project) ||
                    TypeUtil.isEmbeddable(field.type, project)
            )
        )
}
