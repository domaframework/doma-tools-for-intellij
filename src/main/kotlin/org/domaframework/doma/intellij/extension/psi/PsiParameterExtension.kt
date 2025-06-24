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
import org.domaframework.doma.intellij.common.util.DomaClassName

val PsiParameter.isFunctionClazz: Boolean
    get() {
        val functionType = DomaClassName.JAVA_FUNCTION
        val superCollection: PsiClassType? = getSuperClassType(functionType)
        return superCollection != null
    }

val PsiParameter.isBiFunctionClazz: Boolean
    get() {
        val functionType = DomaClassName.BI_FUNCTION
        val superCollection: PsiClassType? = getSuperClassType(functionType)
        return superCollection != null
    }

val PsiParameter.isSelectOption: Boolean
    get() {
        val collectorType = DomaClassName.SELECT_OPTIONS
        val superCollection: PsiClassType? = getSuperClassType(collectorType)
        return superCollection != null
    }

val PsiParameter.isCollector: Boolean
    get() {
        val collectorType = DomaClassName.JAVA_COLLECTOR
        val superCollection: PsiClassType? = getSuperClassType(collectorType)
        return superCollection != null
    }

fun PsiParameter.getSuperClassType(superClassType: DomaClassName): PsiClassType? {
    val clazzType = this.typeElement?.type as? PsiClassType
    var superCollection: PsiClassType? = clazzType
    while (superCollection != null &&
        !superClassType.isTargetClassNameStartsWith(superCollection.canonicalText)
    ) {
        superCollection =
            superCollection.getSuperType(superClassType.className)
    }
    return superCollection
}
