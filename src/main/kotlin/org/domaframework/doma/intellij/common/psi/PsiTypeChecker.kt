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
package org.domaframework.doma.intellij.common.psi

import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiUtil
import org.domaframework.doma.intellij.common.util.DomaClassName

object PsiTypeChecker {
    private val TARGET_CLASSES: MutableSet<String> = HashSet()
    private val WRAPPER_CLASSES: MutableSet<String> = HashSet()

    init {
        TARGET_CLASSES.add(DomaClassName.STRING.className)
        TARGET_CLASSES.add(DomaClassName.OBJECT.className)
        TARGET_CLASSES.add(DomaClassName.BIG_DECIMAL.className)
        TARGET_CLASSES.add(DomaClassName.BIG_INTEGER.className)
        TARGET_CLASSES.add(DomaClassName.LOCAL_DATE.className)
        TARGET_CLASSES.add(DomaClassName.LOCAL_TIME.className)
        TARGET_CLASSES.add(DomaClassName.LOCAL_DATE_TIME.className)
        TARGET_CLASSES.add(DomaClassName.SQL_DATE.className)
        TARGET_CLASSES.add(DomaClassName.SQL_TIME.className)
        TARGET_CLASSES.add(DomaClassName.SQL_TIMESTAMP.className)
        TARGET_CLASSES.add(DomaClassName.SQL_ARRAY.className)
        TARGET_CLASSES.add(DomaClassName.SQL_BLOB.className)
        TARGET_CLASSES.add(DomaClassName.SQL_CLOB.className)
        TARGET_CLASSES.add(DomaClassName.SQL_XML.className)
        TARGET_CLASSES.add(DomaClassName.UTIL_DATE.className)

        WRAPPER_CLASSES.add(DomaClassName.BYTE.className)
        WRAPPER_CLASSES.add(DomaClassName.SHORT.className)
        WRAPPER_CLASSES.add(DomaClassName.INTEGER.className)
        WRAPPER_CLASSES.add(DomaClassName.LONG.className)
        WRAPPER_CLASSES.add(DomaClassName.FLOAT.className)
        WRAPPER_CLASSES.add(DomaClassName.DOUBLE.className)
        WRAPPER_CLASSES.add(DomaClassName.BOOLEAN.className)
    }

    /**
     * Determines whether the specified PsiType satisfies the conditions.
     * @param psiType Check target PsiType
     * @return true if the condition is met
     */
    fun isBaseClassType(psiType: PsiType?): Boolean {
        if (psiType == null) return false
        // Check if the type is a primitive type
        if (psiType is PsiPrimitiveType) {
            // char is not supported, but other primitive types are
            return psiType.canonicalText != "char"
        }

        // Check if the type is a wrapper class
        if (psiType is PsiClassType) {
            val psiClass = PsiUtil.resolveClassInType(psiType)
            if (psiClass != null) {
                val qualifiedName = psiClass.qualifiedName
                if (qualifiedName != null) {
                    if (WRAPPER_CLASSES.contains(qualifiedName) ||
                        TARGET_CLASSES.contains(qualifiedName)
                    ) {
                        return true
                    }
                }
                if (psiClass.isEnum) {
                    return true
                }
            }
        }
        if (psiType is PsiArrayType) {
            val componentType = psiType.componentType.canonicalText
            return DomaClassName.BYTE.className == componentType
        }
        // TODO If the condition does not match, return false to strengthen type checking.
        return false
    }
}
