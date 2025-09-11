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
package org.domaframework.doma.intellij.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.inspection.sql.processor.InspectionFunctionCallVisitorProcessor
import org.domaframework.doma.intellij.psi.SqlElFunctionCallExpr

class SqlElFunctionCallExprReference(
    element: PsiElement,
) : SqlElExprReference(element) {
    override fun superResolveLogic(
        startTime: Long,
        file: PsiFile,
    ): PsiElement? {
        val functionCallExpr =
            element as? SqlElFunctionCallExpr ?: PsiTreeUtil.getParentOfType(element, SqlElFunctionCallExpr::class.java)
                ?: return null
        val processor = InspectionFunctionCallVisitorProcessor("", functionCallExpr)
        val reference: PsiMethod? = processor.getFunctionCallType()

        if (reference == null) {
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceCustomFunctions",
                "Reference",
                startTime,
            )
        }
        return reference
    }
}
