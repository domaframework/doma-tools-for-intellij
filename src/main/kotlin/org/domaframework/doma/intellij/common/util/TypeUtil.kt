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
package org.domaframework.doma.intellij.common.util

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import org.domaframework.doma.intellij.common.psi.PsiTypeChecker
import org.domaframework.doma.intellij.common.util.StringUtil.SINGLE_SPACE
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.psi.getClassAnnotation
import org.domaframework.doma.intellij.extension.psi.isDomain
import org.domaframework.doma.intellij.extension.psi.isEntity
import org.domaframework.doma.intellij.formatter.block.SqlBlock
import kotlin.reflect.KClass

object TypeUtil {
    /**
     * Unwraps the type parameter from Optional if present, otherwise returns the original type.
     */
    fun unwrapOptional(type: PsiType?): PsiType? {
        if (type == null) return null
        if (DomaClassName.OPTIONAL.isTargetClassNameStartsWith(type.canonicalText)) {
            val classType = type as? PsiClassType
            return classType?.parameters?.firstOrNull()
        }
        return type
    }

    /**
     * Checks if the given type is an entity.
     */
    fun isEntity(
        type: PsiType?,
        project: Project,
    ): Boolean {
        val clazz = type?.canonicalText?.let { project.getJavaClazz(it) }
        return clazz?.isEntity() == true
    }

    fun isImmutableEntity(
        project: Project,
        canonicalText: String,
    ): Boolean {
        val returnTypeClass = project.getJavaClazz(canonicalText)
        val entity =
            returnTypeClass?.getClassAnnotation(DomaClassName.ENTITY.className) ?: return false
        return entity.let { entity ->
            AnnotationUtil.getBooleanAttributeValue(entity, "immutable") == true
        } == true ||
            returnTypeClass.isRecord == true
    }

    /**
     * Checks if the given type is a domain.
     */
    fun isDomain(
        type: PsiType?,
        project: Project,
    ): Boolean {
        val clazz = type?.canonicalText?.let { project.getJavaClazz(it) }
        return clazz?.isDomain() == true
    }

    /**
     * Checks if the given type is a valid Map<String, Object>.
     */
    fun isValidMapType(type: PsiType?): Boolean {
        val canonical = type?.canonicalText?.replace(SINGLE_SPACE, "") ?: return false
        val expected =
            DomaClassName.MAP
                .getGenericParamCanonicalText(
                    DomaClassName.STRING.className,
                    DomaClassName.OBJECT.className,
                ).replace(SINGLE_SPACE, "")
        return canonical == expected
    }

    /**
     * Checks if the given type is a base class type or an optional wrapper type.
     */
    fun isBaseOrOptionalWrapper(type: PsiType?): Boolean {
        if (type == null) return false
        return PsiTypeChecker.isBaseClassType(type) || DomaClassName.isOptionalWrapperType(type.canonicalText)
    }

    /**
     * Determines whether the specified class instance matches.
     */
    fun isExpectedClassType(
        expectedClasses: List<KClass<*>>,
        childBlock: SqlBlock?,
    ): Boolean =
        expectedClasses.any { clazz ->
            clazz.isInstance(childBlock)
        }
}
