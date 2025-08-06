package org.domaframework.doma.intellij.inspection.dao.processor.option

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.TypeUtil
import org.domaframework.doma.intellij.common.validation.result.ValidationAnnotationOptionEmbeddableResult
import org.domaframework.doma.intellij.common.validation.result.ValidationAnnotationOptionParameterResult
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.isEmbeddable
import org.domaframework.doma.intellij.extension.psi.isEntity
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

        val entityClass = project.getJavaClazz(entityType.canonicalText) ?: return
        if (!entityClass.isEntity()) {
            return
        }

        checkArrayOption(annotation, "include", entityClass, holder)
        checkArrayOption(annotation, "exclude", entityClass, holder)

        val returningOption = getDaoAnnotationOption(annotation, "returning") ?: return
        if (hasReturingOption()) {
            checkArrayOption(returningOption, "include", entityClass, holder)
            checkArrayOption(returningOption, "exclude", entityClass, holder)
        }
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
            var preSearchParamClass: PsiClass? = entityClass
            var hasError = false
            valueFields.map { field ->
                searchParamClass
                    ?.fields
                    ?.find { property -> isOptionTargetProperty(property, field, project) }
                    ?.let { f ->
                        preSearchParamClass = searchParamClass
                        searchParamClass = project.getJavaClazz(f.type.canonicalText) ?: return@map
                    }
                    ?: run {
                        ValidationAnnotationOptionParameterResult(
                            fields,
                            shortName,
                            field,
                            optionName,
                            searchParamClass?.name ?: "Unknown",
                            getTargetOptionProperties(preSearchParamClass),
                        ).highlightElement(holder)
                        hasError = true
                        return@map
                    }
            }
            // Error if the last field is Embeddable
            if (!hasError && searchParamClass?.isEmbeddable() == true) {
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
