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
package org.domaframework.doma.intellij.inspection.dao.visitor

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiMethod
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.BatchAnnotationReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.MultiInsertAnnotationReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.ProcedureAnnotationReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.ReturnTypeCheckerProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.ScriptAnnotationReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.SqlProcessorAnnotationReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.UpdateAnnotationReturnTypeCheckProcessor

class DaoMethodReturnTypeInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : JavaElementVisitor() {
    override fun visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        val file = method.containingFile
        if (!isJavaOrKotlinFileType(file) || getDaoClass(file) == null) return

        val psiDaoMethod = PsiDaoMethod(method.project, method)
        var processor: ReturnTypeCheckerProcessor? = getReturnTypeCheckProcessor(psiDaoMethod)
        val result = processor?.checkReturnType()
        result?.highlightElement(holder)
    }

    private fun getReturnTypeCheckProcessor(psiDaoMethod: PsiDaoMethod): ReturnTypeCheckerProcessor? =
        when (psiDaoMethod.daoType) {
            DomaAnnotationType.Insert, DomaAnnotationType.Update, DomaAnnotationType.Delete -> {
                UpdateAnnotationReturnTypeCheckProcessor(psiDaoMethod, this.shortName)
            }

            DomaAnnotationType.BatchInsert, DomaAnnotationType.BatchUpdate, DomaAnnotationType.BatchDelete -> {
                BatchAnnotationReturnTypeCheckProcessor(psiDaoMethod, this.shortName)
            }

            DomaAnnotationType.Procedure -> {
                ProcedureAnnotationReturnTypeCheckProcessor(psiDaoMethod, this.shortName)
            }
            DomaAnnotationType.SqlProcessor -> {
                SqlProcessorAnnotationReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }
            DomaAnnotationType.MultiInsert ->
                MultiInsertAnnotationReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            DomaAnnotationType.Script -> {
                ScriptAnnotationReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            else -> null
        }
}
