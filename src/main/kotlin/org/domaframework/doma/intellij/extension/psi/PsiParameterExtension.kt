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
package org.domaframework.doma.intellij.extension.psi

import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiParameter
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.sql.PsiClassTypeUtil

/**
 * For List type, if the annotation type is Batch type,
 * return the content type.
 */
fun PsiParameter.getIterableClazz(annotationType: DomaAnnotationType): PsiParentClass = getIterableClazz(annotationType.isBatchAnnotation())

fun PsiParameter.getIterableClazz(useListParam: Boolean): PsiParentClass {
    val immediate = this.type as? PsiClassType
    val classType = immediate?.let { PsiClassTypeUtil.getPsiTypeByList(it, this.project, useListParam) }
    if (classType != null) {
        return PsiParentClass(classType)
    }
    return PsiParentClass(this.type)
}

val PsiParameter.isFunctionClazz: Boolean
    get() =
        (this.typeElement?.type as? PsiClassType)
            ?.resolve()
            ?.qualifiedName
            ?.contains("java.util.function") == true

val PsiParameter.isSelectOption: Boolean
    get() =
        (this.typeElement?.type as? PsiClassType)
            ?.resolve()
            ?.qualifiedName == "org.seasar.doma.jdbc.SelectOptions"

val PsiParameter.isCollector: Boolean
    get() =
        (this.typeElement?.type as? PsiClassType)
            ?.resolve()
            ?.qualifiedName == "java.util.stream.Collector"
