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
package org.domaframework.doma.intellij.inspection.dao.inspector

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.domaframework.doma.intellij.inspection.dao.visitor.DaoMethodVariableInspectionVisitor

/**
 * Check if DAO method arguments are used in the corresponding SQL file
 */
class DaoMethodVariableInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun getDisplayName(): String = "Check usage of DAO method arguments in corresponding SQL file."

    override fun getShortName(): String = "org.domaframework.doma.intellij.variablechecker"

    override fun getGroupDisplayName(): String = "DomaTools"

    override fun isEnabledByDefault(): Boolean = true

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): PsiElementVisitor = DaoMethodVariableInspectionVisitor(holder)
}
