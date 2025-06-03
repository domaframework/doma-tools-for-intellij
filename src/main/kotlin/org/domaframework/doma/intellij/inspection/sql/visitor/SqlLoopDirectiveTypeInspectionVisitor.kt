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
package org.domaframework.doma.intellij.inspection.sql.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.inspection.sql.processor.InspectionForDirectiveVisitorProcessor
import org.domaframework.doma.intellij.psi.SqlElForDirective

class SqlLoopDirectiveTypeInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : SqlVisitorBase() {
    override fun visitElement(element: PsiElement) {
        val file = element.containingFile ?: return
        if (isJavaOrKotlinFileType(file) && element is PsiLiteralExpression) {
            val injectionFile = initInjectionElement(file, element.project, element) ?: return
            injectionFile.accept(this)
            super.visitElement(element)
        }
        if (isInjectionSqlFile(file)) {
            element.acceptChildren(this)
        }
    }

    override fun visitElForDirective(element: SqlElForDirective) {
        super.visitElForDirective(element)
        val process = InspectionForDirectiveVisitorProcessor(shortName, element)
        process.check(holder)
    }
}
