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
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.BatchReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.FactoryReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.FunctionReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.MultiInsertReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.ProcedureReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.ReturnTypeCheckerProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.ScriptReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.SelectReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.SqlProcessorReturnTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.returntype.UpdateReturnTypeCheckProcessor

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
            DomaAnnotationType.Select -> {
                SelectReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            DomaAnnotationType.Insert, DomaAnnotationType.Update, DomaAnnotationType.Delete -> {
                UpdateReturnTypeCheckProcessor(psiDaoMethod, this.shortName)
            }

            DomaAnnotationType.BatchInsert, DomaAnnotationType.BatchUpdate, DomaAnnotationType.BatchDelete -> {
                BatchReturnTypeCheckProcessor(psiDaoMethod, this.shortName)
            }

            DomaAnnotationType.Procedure -> {
                ProcedureReturnTypeCheckProcessor(psiDaoMethod, this.shortName)
            }

            DomaAnnotationType.SqlProcessor -> {
                SqlProcessorReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            DomaAnnotationType.MultiInsert ->
                MultiInsertReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )

            DomaAnnotationType.Script -> {
                ScriptReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            DomaAnnotationType.Function -> {
                FunctionReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            DomaAnnotationType.ArrayFactory, DomaAnnotationType.BlobFactory, DomaAnnotationType.ClobFactory,
            DomaAnnotationType.NClobFactory, DomaAnnotationType.SQLXMLFactory,
            -> {
                FactoryReturnTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            else -> null
        }
}
