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
package org.domaframework.doma.intellij.inspection.sql.inspector

import com.intellij.codeHighlighting.HighlightDisplayLevel
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import org.domaframework.doma.intellij.inspection.sql.visitor.SqlTestDataInspectionVisitor
import org.domaframework.doma.intellij.psi.SqlVisitor

/**
 * Code inspection for SQL bind variables
 */
class SqlTestDataInspection : LocalInspectionTool() {
    override fun getDisplayName(): String = "Verify the presence of test data after SQL bind variables"

    override fun getShortName(): String = "org.domaframework.doma.intellij.existaftertestdata"

    override fun getGroupDisplayName(): String = "DomaTools"

    override fun isEnabledByDefault(): Boolean = true

    override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.Companion.ERROR

    override fun runForWholeFile(): Boolean = true

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean,
    ): SqlVisitor = SqlTestDataInspectionVisitor(holder, this.shortName)
}
