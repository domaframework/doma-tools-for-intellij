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
package org.domaframework.doma.intellij.common.sql.validator.result

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.psi.PsiParentClass

/**
 * This class indicates that there is no field or method defined in the class that matches the target name.
 */
class ValidationPropertyResult(
    override val identify: PsiElement,
    override val parentClass: PsiParentClass?,
    override val shortName: String,
) : ValidationResult(identify, parentClass, shortName) {
    override fun setHighlight(
        highlightRange: TextRange,
        identify: PsiElement,
        holder: ProblemsHolder,
        parent: PsiParentClass?,
    ) {
        val project = identify.project
        val parentName =
            parentClass?.clazz?.name
                ?: (parentClass?.type as? PsiClassType)?.name
                ?: ""
        holder.registerProblem(
            identify,
            MessageBundle.message(
                "inspection.invalid.sql.property",
                parentName,
                identify.text ?: "",
            ),
            problemHighlightType(project, shortName),
            highlightRange,
        )
    }
}
