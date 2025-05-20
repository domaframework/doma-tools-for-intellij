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
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.rename.RenameJavaClassProcessor
import com.intellij.usageView.UsageInfo
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.extension.getContentRoot
import org.domaframework.doma.intellij.extension.getPackagePathFromDaoPath
import org.jetbrains.kotlin.idea.base.util.module

/**
 * Rename DAO class
 */
class DaoRenameProcessor : RenameJavaClassProcessor() {
    override fun canProcessElement(element: PsiElement): Boolean =
        super.canProcessElement(element) && getDaoClass(element.containingFile) != null

    override fun renameElement(
        element: PsiElement,
        newName: String,
        usages: Array<out UsageInfo>,
        listener: RefactoringElementListener?,
    ) {
        val startTime = System.nanoTime()
        val daoClass = getDaoClass(element.containingFile) ?: return

        val project = element.project
        val virtualFile = element.containingFile.virtualFile ?: return
        project.getContentRoot(virtualFile)?.let {
            element.module?.getPackagePathFromDaoPath(virtualFile)?.let {
                if (it.name == daoClass.name) {
                    it.rename(it, newName)
                }
            }
        }
        super.renameElement(element, newName, usages, listener)
        PluginLoggerUtil.countLogging(
            this::class.java.simpleName,
            "RenameClass",
            "Rename",
            startTime,
        )
        // Clear module directory cache on refactoring
        CommonPathParameterUtil.clearCache()
    }
}
