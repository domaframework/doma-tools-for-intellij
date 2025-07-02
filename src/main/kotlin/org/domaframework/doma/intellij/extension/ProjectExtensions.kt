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
package org.domaframework.doma.intellij.extension

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope

fun Project.getContentRoot(baseFile: VirtualFile): VirtualFile? =
    ProjectRootManager
        .getInstance(this)
        .fileIndex
        .getContentRootForFile(baseFile)

fun Project.getModule(virtualFile: VirtualFile): Module? =
    ProjectRootManager
        .getInstance(this)
        .fileIndex
        .getModuleForFile(virtualFile)

fun Project.findFile(file: VirtualFile): PsiFile? = PsiManager.getInstance(this).findFile(file)

fun Project.getJavaClazz(fqdn: String): PsiClass? {
    val scope = GlobalSearchScope.allScope(this)
    val topClassName = fqdn.substringBefore("<")
    return JavaPsiFacade
        .getInstance(this)
        .findClasses(topClassName, scope)
        .firstOrNull()
        ?: JavaPsiFacade.getInstance(this).findClass(
            topClassName,
            GlobalSearchScope.allScope(this),
        )
}

fun Project.getSourceRootDir(file: VirtualFile): VirtualFile? = ProjectFileIndex.getInstance(this).getSourceRootForFile(file)
