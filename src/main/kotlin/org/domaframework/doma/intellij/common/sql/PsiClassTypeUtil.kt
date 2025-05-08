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
package org.domaframework.doma.intellij.common.sql

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope

class PsiClassTypeUtil {
    companion object {
        fun getPsiTypeByList(
            classType: PsiClassType,
            project: Project,
            useListParam: Boolean,
        ): PsiType? {
            val isIterableType = isIterableType(classType, project)
            if (isIterableType && useListParam) {
                return classType.parameters.firstOrNull()
            }
            return classType
        }

        fun isIterableType(
            type: PsiClassType,
            project: Project,
        ): Boolean {
            val iterableType =
                PsiType.getTypeByName(
                    "java.lang.Iterable",
                    project,
                    GlobalSearchScope.allScope(project),
                )
            return iterableType.isAssignableFrom(type)
        }

        fun getParameterType(
            project: Project,
            baseType: PsiType?,
            preReturnListType: PsiType,
            index: Int,
        ): PsiClassType? {
            val returnType = baseType as? PsiClassType ?: return null
            val preReturnType = preReturnListType as? PsiClassType ?: return null

            if (returnType.name == "E" && isIterableType(preReturnListType, project)) {
                var count = 1
                var type: PsiType? = preReturnType.parameters.firstOrNull()
                while (index > count && type != null && type is PsiClassType) {
                    type = type.parameters.firstOrNull()
                    count++
                }
                val convertOptional = type?.let { convertOptionalType(it, project) }
                return convertOptional as? PsiClassType
            }
            return null
        }

        /**
         * Check if daoParamType is an instance of PsiClassType representing Optional or its primitive variants
         */
        fun convertOptionalType(
            daoParamType: PsiType,
            project: Project,
        ): PsiType {
            if (daoParamType is PsiClassType) {
                val resolved = daoParamType.resolve()
                val optionalTypeMap =
                    mapOf(
                        "java.util.OptionalInt" to "java.lang.Integer",
                        "java.util.OptionalDouble" to "java.lang.Double",
                        "java.util.OptionalLong" to "java.lang.Long",
                    )
                if (resolved != null) {
                    when (resolved.qualifiedName) {
                        "java.util.Optional" -> return daoParamType.parameters.firstOrNull()
                            ?: daoParamType

                        else ->
                            optionalTypeMap[resolved.qualifiedName]?.let { optionalType ->
                                val newType =
                                    PsiType.getTypeByName(
                                        optionalType,
                                        project,
                                        GlobalSearchScope.allScope(project),
                                    )
                                return newType
                            }
                    }
                }
            }
            return daoParamType
        }
    }
}
