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

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.impl.source.PsiParameterImpl
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.extension.findFile
import org.domaframework.doma.intellij.extension.psi.isCollector
import org.domaframework.doma.intellij.extension.psi.isFunctionClazz
import org.domaframework.doma.intellij.extension.psi.isSelectOption
import org.domaframework.doma.intellij.extension.psi.methodParameters

class UsedDaoMethodParamInspectionVisitor(
    private val holder: ProblemsHolder,
) : JavaElementVisitor() {
    override fun visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        val file = method.containingFile
        if (!isJavaOrKotlinFileType(file) || getDaoClass(file) == null) return

        val psiDaoMethod = PsiDaoMethod(method.project, method)
        if (!psiDaoMethod.useSqlAnnotation() && !psiDaoMethod.isUseSqlFileMethod()) return

        val methodParameters =
            method.methodParameters
                .filter { !it.isFunctionClazz && !it.isSelectOption && !(it.isCollector && psiDaoMethod.isSelectTypeCollect()) }
        val sqlFileManager =
            psiDaoMethod.sqlFile?.let {
                method.project.findFile(it)
            } ?: return

        val params = methodParameters.toList()
        val result = findElementsInSqlFile(sqlFileManager, params)
        params.forEach { param ->
            if (!result.elements.contains(param)) {
                val message =
                    if (result.deplicateForItemElements.contains(param)) {
                        MessageBundle.message("inspection.invalid.dao.duplicate")
                    } else {
                        MessageBundle.message(
                            "inspection.invalid.dao.paramUse",
                            param.name,
                        )
                    }
                holder.registerProblem(
                    (param.originalElement as PsiParameterImpl).nameIdentifier,
                    message,
                    ProblemHighlightType.ERROR,
                )
            }
        }
    }

    private data class DaoMethodVariableVisitorResult(
        val elements: List<PsiParameter>,
        val deplicateForItemElements: List<PsiParameter>,
    )

    private fun findElementsInSqlFile(
        sqlFile: PsiFile,
        args: List<PsiParameter>,
    ): DaoMethodVariableVisitorResult {
        val elements = mutableListOf<PsiParameter>()
        val deplicateForItemElements = mutableListOf<PsiParameter>()

        sqlFile.accept(DaoMethodRelatedSqlVisitor(args, elements, deplicateForItemElements))
        val result = DaoMethodVariableVisitorResult(elements, deplicateForItemElements)
        return result
    }
}
