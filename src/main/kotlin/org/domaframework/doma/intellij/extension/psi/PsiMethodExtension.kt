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

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeParameterList
import com.intellij.psi.impl.compiled.ClsClassImpl
import com.intellij.psi.impl.compiled.ClsTypeParametersListImpl
import com.intellij.psi.impl.source.PsiClassReferenceType

fun PsiMethod.findParameter(searchName: String): PsiParameter? = this.methodParameters.firstOrNull { it.name == searchName }

val PsiMethod.methodParameters: List<PsiParameter>
    get() = this.parameterList.parameters.toList()

fun PsiMethod.searchParameter(searchName: String): List<PsiParameter> = this.methodParameters.filter { it.name.startsWith(searchName) }

fun PsiMethod.getDomaAnnotationType(): DomaAnnotationType {
    DomaAnnotationType.entries.forEach {
        if (AnnotationUtil.findAnnotation(this, it.fqdn) != null) {
            return it
        }
    }
    return DomaAnnotationType.Unknown
}

/**
 * If the type of the variable referenced from the Dao argument is List type,
 * search processing to obtain the nested type
 */
fun PsiMethod.getMethodReturnType(
    topElementType: PsiType,
    index: Int,
): PsiClassType? {
    val returnType = this.returnType as? PsiClassType
    val cls = returnType?.resolve()?.parent as? ClsTypeParametersListImpl
    val listType = ((cls as? PsiTypeParameterList)?.parent as? ClsClassImpl)

    if (returnType?.name == "E" && listType?.qualifiedName == "java.util.List") {
        var count = 1
        var type: PsiType? = (topElementType as? PsiClassReferenceType)?.parameters?.firstOrNull()
        while (index >= count && type != null && type is PsiClassReferenceType) {
            type = type.parameters.firstOrNull()
            count++
        }
        return type as? PsiClassType
    }
    return returnType
}
