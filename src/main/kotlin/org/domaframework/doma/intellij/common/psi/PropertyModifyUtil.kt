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

import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiUtil

open class PropertyModifyUtil {
    companion object {
        /**
         * If it is a Java package, exclude fields other than public.
         */
        fun filterPrivateField(
            field: PsiField,
            type: PsiType,
        ): Boolean {
            if (isJavaPackage(type)) {
                return field.hasModifierProperty(PsiModifier.PUBLIC)
            }
            return true
        }

        private fun isJavaPackage(type: PsiType): Boolean {
            val clazzType = PsiUtil.resolveClassInType(type)
            return clazzType?.qualifiedName?.startsWith("java.") == true ||
                clazzType?.qualifiedName?.startsWith("javax.") == true ||
                clazzType?.qualifiedName?.startsWith("jakarta.") == true
        }
    }
}
