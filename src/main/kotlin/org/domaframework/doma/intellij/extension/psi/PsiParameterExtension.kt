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

private val ignoreUsageCheckType =
    listOf<DomaClassName>(
        DomaClassName.JAVA_FUNCTION,
        DomaClassName.BI_FUNCTION,
        DomaClassName.SELECT_OPTIONS,
        DomaClassName.JAVA_COLLECTOR,
    )

fun PsiParameter.isIgnoreUsageCheck(): Boolean =
    ignoreUsageCheckType.any { type ->
        when (type) {
            DomaClassName.SELECT_OPTIONS -> {
                // Only ignore the exact SelectOptions class, not its subtypes
                val clazzType = this.typeElement?.type as? PsiClassType
                clazzType?.canonicalText == type.className
            }
            else -> getSuperClassType(type) != null
        }
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
