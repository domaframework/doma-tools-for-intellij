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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiPackage
import com.intellij.refactoring.listeners.RefactoringElementListener
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider
import com.intellij.util.IncorrectOperationException
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.RESOURCES_META_INF_PATH
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.util.PluginLoggerUtil
import org.domaframework.doma.intellij.extension.getResourcesSQLFile
import org.jetbrains.kotlin.idea.base.util.module
import java.io.IOException

/**
 * SQL directory factoring
 * Perform SQL directory rename processing after standard refactoring
 */
class DaoPackageRenameListenerProcessor : RefactoringElementListenerProvider {
    override fun getListener(element: PsiElement): RefactoringElementListener? {
        if (!isPackageElement(element)) return null
        val startTime = System.nanoTime()
        val clazzName = this::class.java.simpleName
        val psiPackage = getPsiPackage(element) ?: return null

        val oldQualifiedName = psiPackage.qualifiedName
        val module = getModule(element) ?: return null

        return object : RefactoringElementListener {
            override fun elementMoved(newelement: PsiElement) {
                // Clear module directory cache on refactoring
                CommonPathParameterUtil.clearCache()
                if (newelement is PsiClass) {
                    if (getDaoClass(newelement.containingFile) == null) return
                    refactoringMoveClassFile(module, newelement)
                } else {
                    refactoringPackageRenameOrMove(module, newelement)
                }
            }

            override fun elementRenamed(newelement: PsiElement) {
                // Clear module directory cache on refactoring
                CommonPathParameterUtil.clearCache()
                if (newelement is PsiClass) return
                refactoringPackageRenameOrMove(module, newelement)
            }

            private fun refactoringPackageRenameOrMove(
                module: Module,
                newElement: PsiElement,
            ) {
                val psiPackage = getPsiPackage(newElement) ?: return
                val directories =
                    psiPackage.directories
                        .filter { !it.name.contains("build") }
                directories.forEach { dir ->
                    newElement.containingFile
                        ?.virtualFile
                        ?.let { handlePackageMove(psiPackage, oldQualifiedName, module, it) }
                }

                PluginLoggerUtil.countLogging(
                    clazzName,
                    "RenamePackage",
                    "Rename",
                    startTime,
                )
            }

            private fun refactoringMoveClassFile(
                module: Module,
                newElement: PsiElement,
            ) {
                val psiPackage = getPsiPackage(newElement) ?: return
                val moveFileName = newElement.containingFile?.virtualFile?.nameWithoutExtension
                val directories =
                    psiPackage.directories
                        .filter { !it.name.contains("build") }
                directories.forEach { dir ->
                    element.containingFile
                        ?.virtualFile
                        ?.let { handlePackageMove(psiPackage, oldQualifiedName, module, it, moveFileName) }
                }

                PluginLoggerUtil.countLogging(
                    clazzName,
                    "MoveFile",
                    "Move",
                    startTime,
                )
            }

            private fun handlePackageMove(
                packageElement: PsiPackage,
                oldQualifiedName: String,
                module: Module,
                file: VirtualFile,
                moveFileName: String? = null,
            ) {
                val newQualifiedName = packageElement.qualifiedName
                renameOrMovePackage(
                    module,
                    oldQualifiedName,
                    newQualifiedName,
                    file,
                    moveFileName,
                )
            }

            private fun renameOrMovePackage(
                module: Module,
                oldQualifiedName: String,
                newQualifiedName: String,
                file: VirtualFile,
                moveFileName: String? = null,
            ) {
                val resources = CommonPathParameterUtil.getResources(module, file)
                val isTest = CommonPathParameterUtil.isTest(module, file)
                val baseDirs: List<String> = resources.map { resource -> "${resource.path}/$RESOURCES_META_INF_PATH/" }
                val newPaths = baseDirs.map { baseDir -> "$baseDir/${newQualifiedName.replace(".", "/")}" }

                ApplicationManager.getApplication().runWriteAction {
                    newPaths.forEach { newPath ->
                        try {
                            val newDir = VfsUtil.createDirectories(newPath)
                            val oldResourcePath =
                                module.getResourcesSQLFile(
                                    RESOURCES_META_INF_PATH + "/" + oldQualifiedName.replace(".", "/"),
                                    isTest,
                                )
                            if (oldResourcePath != null) {
                                oldResourcePath.children?.forEach { old ->
                                    if (moveFileName != null && old.name == moveFileName) {
                                        old?.move(this, newDir)
                                    } else if (moveFileName == null) {
                                        old?.move(this, newDir)
                                    }
                                }
                                return@forEach
                            }
                        } catch (e: IOException) {
                            when (e) {
                                is FileSystemException -> {
                                    e.printStackTrace()
                                }

                                else -> throw IncorrectOperationException(e)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getPsiPackage(newElement: PsiElement): PsiPackage? =
        when (newElement) {
            is PsiPackage -> newElement
            is PsiClass, is PsiJavaFile ->
                JavaDirectoryService
                    .getInstance()
                    .getPackage(newElement.containingFile.containingDirectory)

            else -> null
        }

    private fun getModule(element: PsiElement): Module? =
        when (element) {
            is PsiPackage -> {
                val modules = element.directories.map { it.module }.toSet()
                if (modules.size == 1) {
                    modules.first()
                } else {
                    ModuleUtilCore.findModuleForPsiElement(element)
                }
            }

            is PsiClass, is PsiJavaFile -> {
                ModuleUtilCore.findModuleForFile(element.containingFile)
            }

            is PsiDirectory -> element.module
            else -> null
        }

    private fun isPackageElement(element: PsiElement): Boolean =
        element is PsiPackage || element is PsiClass || element is PsiJavaFile || element is PsiDirectory
}
