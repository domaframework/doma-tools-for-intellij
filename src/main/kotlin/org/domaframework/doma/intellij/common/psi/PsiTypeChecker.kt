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

object PsiTypeChecker {
    private val TARGET_CLASSES: MutableSet<String> = HashSet()
    private val WRAPPER_CLASSES: MutableSet<String> = HashSet()

    init {
        TARGET_CLASSES.add("java.lang.String")
        TARGET_CLASSES.add("java.lang.Object")
        TARGET_CLASSES.add("java.math.BigDecimal")
        TARGET_CLASSES.add("java.math.BigInteger")
        TARGET_CLASSES.add("java.time.LocalDate")
        TARGET_CLASSES.add("java.time.LocalTime")
        TARGET_CLASSES.add("java.time.LocalDateTime")
        TARGET_CLASSES.add("java.sql.Date")
        TARGET_CLASSES.add("java.sql.Time")
        TARGET_CLASSES.add("java.sql.Timestamp")
        TARGET_CLASSES.add("java.sql.Array")
        TARGET_CLASSES.add("java.sql.Blob")
        TARGET_CLASSES.add("java.sql.Clob")
        TARGET_CLASSES.add("java.sql.SQLXML")
        TARGET_CLASSES.add("java.util.Date")

        WRAPPER_CLASSES.add("java.lang.Byte")
        WRAPPER_CLASSES.add("java.lang.Short")
        WRAPPER_CLASSES.add("java.lang.Integer")
        WRAPPER_CLASSES.add("java.lang.Long")
        WRAPPER_CLASSES.add("java.lang.Float")
        WRAPPER_CLASSES.add("java.lang.Double")
        WRAPPER_CLASSES.add("java.lang.Boolean")
    }

    /**
     * Determines whether the specified PsiType satisfies the conditions.
     * @param psiType Check target PsiType
     * @return true if the condition is met
     */
    fun isTargetType(psiType: PsiType?): Boolean {
        if (psiType == null) return false
        if (psiType is PsiPrimitiveType && psiType.canonicalText == "char") {
            return false
        }
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
            if ("java.lang.Byte" == componentType) {
                return true
            }
        }
        return true
    }
}
