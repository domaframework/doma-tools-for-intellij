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

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.util.PsiTypesUtil
import org.domaframework.doma.intellij.common.psi.PropertyModifyUtil
import org.domaframework.doma.intellij.common.util.DomaClassName

val PsiClass.psiClassType: PsiClassType
    get() = PsiTypesUtil.getClassType(this)

fun PsiClass.findStaticField(searchName: String): PsiField? =
    this.allFields.firstOrNull {
        it.name == searchName &&
            it.hasModifierProperty(PsiModifier.STATIC) &&
            PropertyModifyUtil.filterPrivateField(it, this.psiClassType)
    }

fun PsiClass.findStaticMethod(searchName: String): PsiMethod? =
    this.allMethods.firstOrNull {
        it.name == searchName &&
            it.hasModifierProperty(PsiModifier.STATIC) &&
            it.hasModifierProperty(PsiModifier.PUBLIC)
    }

fun PsiClass.getClassAnnotation(annotationClassName: String): PsiAnnotation? =
    this.annotations
        .firstOrNull { it.qualifiedName == annotationClassName }

fun PsiClass.isEntity(): Boolean = this.getClassAnnotation(DomaClassName.ENTITY.className) != null

fun PsiClass.isDomain(): Boolean = this.getClassAnnotation(DomaClassName.DOMAIN.className) != null

fun PsiClass.isDataType(): Boolean = this.getClassAnnotation(DomaClassName.DATATYPE.className) != null

fun PsiClassType.getSuperType(superClassName: String): PsiClassType? {
    var parent: PsiClassType? = this
    while (parent != null && !parent.canonicalText.startsWith(superClassName)) {
        parent =
            parent.superTypes.find { superType ->
                superType.canonicalText.startsWith(superClassName)
            } as? PsiClassType
    }
    return parent
}
