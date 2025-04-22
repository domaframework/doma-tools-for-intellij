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

import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType

class PsiClassTypeUtil {
    companion object {
        fun getPsiTypeByList(
            classType: PsiClassType,
            useListParam: Boolean,
        ): PsiType? {
            if (classType.className == "List" && useListParam) {
                return classType.parameters.firstOrNull()
            }
            return classType
        }
    }
}
