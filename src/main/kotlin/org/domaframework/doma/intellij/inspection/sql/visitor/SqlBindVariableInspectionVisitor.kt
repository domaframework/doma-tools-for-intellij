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
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.util.InjectionSqlUtil.isInjectedSqlFile
import org.domaframework.doma.intellij.extension.psi.isFirstElement
import org.domaframework.doma.intellij.inspection.sql.processor.InspectionFieldAccessVisitorProcessor
import org.domaframework.doma.intellij.inspection.sql.processor.InspectionPrimaryVisitorProcessor
import org.domaframework.doma.intellij.inspection.sql.processor.InspectionStaticFieldAccessVisitorProcessor
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlBindVariableInspectionVisitor(
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
        if (isInjectedSqlFile(file)) {
            element.acceptChildren(this)
        }
    }

    override fun visitElStaticFieldAccessExpr(element: SqlElStaticFieldAccessExpr) {
        super.visitElStaticFieldAccessExpr(element)
        val processor = InspectionStaticFieldAccessVisitorProcessor(this.shortName)
        processor.checkBindVariableDefine(element, holder)
    }

    override fun visitElFieldAccessExpr(element: SqlElFieldAccessExpr) {
        super.visitElFieldAccessExpr(element)
        val processor = InspectionFieldAccessVisitorProcessor(shortName, element)
        processor.checkBindVariableDefine(holder)
    }

    override fun visitElPrimaryExpr(element: SqlElPrimaryExpr) {
        val file = element.containingFile ?: return
        if (!element.isFirstElement() || element.prevSibling?.elementType == SqlTypes.AT_SIGN) return

        val processor = InspectionPrimaryVisitorProcessor(this.shortName, element)
        processor.check(holder, file)
    }
}
