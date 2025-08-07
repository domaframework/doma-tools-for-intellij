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
import org.domaframework.doma.intellij.inspection.dao.processor.option.DaoAnnotationOptionParameterCheckProcessor

class DaoAnnotationOptionParameterInspectionVisitor(
    private val holder: ProblemsHolder,
    private val shortName: String,
) : JavaElementVisitor() {
    override fun visitMethod(method: PsiMethod) {
        super.visitMethod(method)
        val file = method.containingFile
        if (!isJavaOrKotlinFileType(file) || getDaoClass(file) == null) return

        val psiDaoMethod = PsiDaoMethod(method.project, method)
        if (listOf(
                DomaAnnotationType.Update,
                DomaAnnotationType.BatchUpdate,
                DomaAnnotationType.Insert,
                DomaAnnotationType.BatchInsert,
                DomaAnnotationType.MultiInsert,
            ).contains(psiDaoMethod.daoType)
        ) {
            val processor = DaoAnnotationOptionParameterCheckProcessor(psiDaoMethod, shortName)
            processor.checkAnnotationOptions(holder)
        }
    }
}
