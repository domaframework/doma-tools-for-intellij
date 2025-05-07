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
package org.domaframework.doma.intellij.refactoring.dao

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenameJavaMethodProcessor
import com.intellij.usageView.UsageInfo
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil

/**
 * Rename DAO method
 */
class DaoMethodRenameProcessor : RenameJavaMethodProcessor() {
    // Target classes with @Select and no @Sql, or with sqlFile=true
    override fun canProcessElement(element: PsiElement): Boolean {
        if (super.canProcessElement(element) && element is PsiMethod) {
            val psiDaoMethod = PsiDaoMethod(element.project, element)
            return psiDaoMethod.isUseSqlFileMethod()
        }
        return false
    }

    override fun renameElement(
        element: PsiElement,
        newName: String,
        usages: Array<out UsageInfo>,
        listener: RefactoringElementListener?,
    ) {
        val startTime = System.nanoTime()
        if (element is PsiMethod) {
            val psiDaoMethod = PsiDaoMethod(element.project, element)
            val sqlExtension = psiDaoMethod.daoType.extension
            val sqlFile = psiDaoMethod.sqlFile
            if (sqlFile != null) {
                if (sqlFile.nameWithoutExtension == element.name) {
                    sqlFile.rename(sqlFile, "$newName.$sqlExtension")
                }
            }
        }
        super.renameElement(element, newName, usages, listener)
        PluginLoggerUtil.countLogging(
            this::class.java.simpleName,
            "RenameDaoMethod",
            "Rename",
            startTime,
        )
    }
}
