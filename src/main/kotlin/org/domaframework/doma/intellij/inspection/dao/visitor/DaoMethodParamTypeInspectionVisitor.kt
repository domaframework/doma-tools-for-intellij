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
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.BatchParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.FactoryParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.MultiInsertParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.ParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.ProcedureParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.ScriptParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.SelectParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.SqlProcessorParamTypeCheckProcessor
import org.domaframework.doma.intellij.inspection.dao.processor.paramtype.UpdateParamTypeCheckProcessor

class DaoMethodParamTypeInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : JavaElementVisitor() {
    override fun visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        val file = method.containingFile
        if (!isJavaOrKotlinFileType(file) || getDaoClass(file) == null) return

        val psiDaoMethod = PsiDaoMethod(method.project, method)
        var processor: ParamTypeCheckProcessor? = getParamTypeCheckProcessor(psiDaoMethod)
        processor?.checkParams(holder)
    }

    private fun getParamTypeCheckProcessor(psiDaoMethod: PsiDaoMethod): ParamTypeCheckProcessor? =
        when (psiDaoMethod.daoType) {
            DomaAnnotationType.Select -> {
                SelectParamTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            DomaAnnotationType.Insert, DomaAnnotationType.Update, DomaAnnotationType.Delete -> {
                UpdateParamTypeCheckProcessor(psiDaoMethod, this.shortName)
            }

            DomaAnnotationType.BatchInsert, DomaAnnotationType.BatchUpdate, DomaAnnotationType.BatchDelete -> {
                BatchParamTypeCheckProcessor(psiDaoMethod, this.shortName)
            }

            DomaAnnotationType.Procedure, DomaAnnotationType.Function -> {
                ProcedureParamTypeCheckProcessor(psiDaoMethod, this.shortName)
            }
            DomaAnnotationType.SqlProcessor -> {
                SqlProcessorParamTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }
            DomaAnnotationType.MultiInsert ->
                MultiInsertParamTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            DomaAnnotationType.Script -> {
                ScriptParamTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            DomaAnnotationType.ArrayFactory, DomaAnnotationType.BlobFactory, DomaAnnotationType.ClobFactory,
            DomaAnnotationType.NClobFactory, DomaAnnotationType.SQLXMLFactory,
            -> {
                FactoryParamTypeCheckProcessor(
                    psiDaoMethod,
                    this.shortName,
                )
            }

            DomaAnnotationType.Sql, DomaAnnotationType.Unknown -> null
        }
}
