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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.extension.psi.isFirstElement
import org.domaframework.doma.intellij.inspection.sql.handler.InspectionFieldAccessVisitorProcessor
import org.domaframework.doma.intellij.inspection.sql.handler.InspectionPrimaryVisitorProcessor
import org.domaframework.doma.intellij.inspection.sql.handler.InspectionStaticFieldAccessVisitorProcessor
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : SqlVisitorBase() {
    override fun visitElement(element: PsiElement) {
        if (setFile(element)) return
        val visitFile: PsiFile = file ?: return
        if (isJavaOrKotlinFileType(visitFile) && element is PsiLiteralExpression) {
            val injectionFile = initInjectionElement(visitFile, element.project, element) ?: return
            injectionFile.accept(this)
            super.visitElement(element)
        }
        if (isInjectionSqlFile(visitFile)) {
            element.acceptChildren(this)
        }
    }

    override fun visitElStaticFieldAccessExpr(element: SqlElStaticFieldAccessExpr) {
        super.visitElStaticFieldAccessExpr(element)
        val handler = InspectionStaticFieldAccessVisitorProcessor(this.shortName)
        handler.check(element, holder)
    }

    override fun visitElFieldAccessExpr(element: SqlElFieldAccessExpr) {
        super.visitElFieldAccessExpr(element)
        if (setFile(element)) return
        val visitFile: PsiFile = file ?: return

        val handler = InspectionFieldAccessVisitorProcessor(shortName, element)
        handler.check(holder, visitFile)
    }

    override fun visitElPrimaryExpr(element: SqlElPrimaryExpr) {
        super.visitElPrimaryExpr(element)
        if (!element.isFirstElement() || element.prevSibling?.elementType == SqlTypes.AT_SIGN) return
        if (setFile(element)) return
        val visitFile: PsiFile = file ?: return

        val handler = InspectionPrimaryVisitorProcessor(this.shortName, element)
        handler.check(holder, visitFile)
    }
}
