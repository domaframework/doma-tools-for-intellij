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

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.ex.ToolsImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.profile.codeInspection.InspectionProfileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import org.domaframework.doma.intellij.common.psi.PsiParentClass

abstract class ValidationResult(
    open val identify: PsiElement?,
    open val parentClass: PsiParentClass?,
    open val shortName: String,
) {
    fun highlightElement(holder: ProblemsHolder) {
        val element = identify ?: return
        if (identify is PsiErrorElement) return

        val highlightElm = element.originalElement
        val highlightRange =
            TextRange(0, element.textRange.length)

        setHighlight(
            highlightRange,
            highlightElm,
            holder,
            parentClass,
        )
    }

    abstract fun setHighlight(
        highlightRange: TextRange,
        identify: PsiElement,
        holder: ProblemsHolder,
        parent: PsiParentClass?,
    )

    /**
     * Highlight level is determined according to the settings
     */
    protected fun problemHighlightType(
        project: Project,
        shortName: String,
    ) = when (
        getInspectionErrorLevel(
            project,
            shortName,
        )
    ) {
        HighlightDisplayLevel.Companion.ERROR -> ProblemHighlightType.ERROR
        HighlightDisplayLevel.Companion.WARNING -> ProblemHighlightType.WARNING
        else -> ProblemHighlightType.WARNING
    }

    private fun getInspectionErrorLevel(
        project: Project,
        inspectionShortName: String,
    ): HighlightDisplayLevel? {
        val profileManager = InspectionProfileManager.getInstance(project)
        val currentProfile = profileManager.currentProfile
        val toolState: ToolsImpl? = currentProfile.getToolsOrNull(inspectionShortName, project)
        return toolState?.level
    }
}
