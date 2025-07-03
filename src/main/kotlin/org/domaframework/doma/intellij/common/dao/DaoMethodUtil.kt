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
package org.domaframework.doma.intellij.common.dao

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.RESOURCES_META_INF_PATH
import org.domaframework.doma.intellij.common.getExtension
import org.domaframework.doma.intellij.common.getJarRoot
import org.domaframework.doma.intellij.common.getMethodDaoFilePath
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.extension.getContentRoot
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.getModule
import org.domaframework.doma.intellij.extension.getSourceRootDir
import org.jetbrains.kotlin.idea.base.util.module

/**
 * Get DAO method corresponding to SQL file
 */
fun findDaoMethod(
    originalFile: PsiFile,
    daoFile: VirtualFile? = null,
): PsiMethod? {
    val project = originalFile.project
    val virtualFile = originalFile.virtualFile ?: return null
    val module = project.getModule(virtualFile)

    if (isInjectionSqlFile(originalFile)) {
        originalFile.let {
            return PsiTreeUtil.getParentOfType(originalFile.context, PsiMethod::class.java)
        }
    } else if (isSupportFileType(originalFile)) {
        val methodName = virtualFile.nameWithoutExtension
        val daoFile = daoFile ?: findDaoFile(project, originalFile) ?: return null
        if (module != null) {
            val contentRootPath = project.getContentRoot(virtualFile)?.path ?: return null
            val daoJavaFile =
                getDaoPathFromSqlFilePath(
                    originalFile,
                    contentRootPath,
                ) ?: return null

            // TODO Support Kotlin Project
            val daoFile = daoJavaFile.containingFile
            val daoMethod =
                when (daoFile) {
                    is PsiJavaFile -> findUseSqlDaoMethod(daoFile, methodName)
                    else -> null
                }
            return daoMethod
        } else {
            val fileType = getExtension(daoFile.fileType.name)
            val jarRootPath = virtualFile.path.substringBefore("jar!").plus("jar!")
            val methodDaoFilePath = getMethodDaoFilePath(virtualFile, jarRootPath, originalFile)
            val daoClassName = getDaoClassName(methodDaoFilePath, fileType)
            val daoFile = getJarRoot(virtualFile, originalFile) ?: return null
            val psiClassFile = PsiManager.getInstance(originalFile.project).findFile(daoFile)
            val psiClassOwner = psiClassFile as? PsiClassOwner ?: return null
            val psiClass =
                psiClassOwner.classes
                    .firstOrNull { it.name == daoClassName }
                    ?: run {
                        val fqn =
                            methodDaoFilePath
                                .trimStart('/')
                                .removeSuffix(".$fileType")
                                .replace('/', '.')
                        JavaPsiFacade
                            .getInstance(project)
                            .findClass(fqn, GlobalSearchScope.allScope(project))
                    } ?: return null
            return psiClass.findMethodsByName(methodName, false).firstOrNull()
        }
    }
    return null
}

private fun getDaoClassName(
    methodDaoFilePath: String,
    extensionName: String,
): String = methodDaoFilePath.substringBefore(".$extensionName").substringAfter("dao/")

/**
 * Get jump destination DAO method file from SQL file
 */
fun findDaoFile(
    project: Project,
    sqlFile: PsiFile,
): VirtualFile? {
    val virtualFile = sqlFile.virtualFile

    val contentRoot = project.getContentRoot(virtualFile)
    if (contentRoot == null) {
        return getJarRoot(virtualFile, sqlFile)
    }
    return getDaoPathFromSqlFilePath(
        sqlFile,
        contentRoot.path,
    )?.containingFile?.virtualFile
}

/**
 * Generate DAO deployment path from SQL file path
 * @param sqlFile SQL File
 * @param contentRootPath project content Root Path
 * @return
 */
private fun getDaoPathFromSqlFilePath(
    sqlFile: PsiFile,
    contentRootPath: String,
): PsiClass? {
    if (isInjectionSqlFile(sqlFile)) {
        return null
    }
    val module = sqlFile.module ?: return null
    val sqlPath = sqlFile.virtualFile?.path ?: return null
    val relativePath =
        sqlPath.substringAfter(contentRootPath, "").replace("/$RESOURCES_META_INF_PATH", "")
    val resourcesRootPath = module.project.getSourceRootDir(sqlFile.virtualFile)?.name ?: ""
    val isTest = CommonPathParameterUtil.isTest(module, sqlFile.virtualFile)

    val packageName = relativePath.replaceFirst("/$resourcesRootPath/", "").replace("/${sqlFile.name}", "").replace("/", ".")
    val daoClassFile = module.getJavaClazz(isTest, packageName)

    return daoClassFile
}

/**
 * Generate SqlFile path from DAO file path
 * @param daoFile DAO File
 * @param module The module to which the DAO file belongs
 * @return SqlFile path ex) META-INF/package/dao/DaoClassName/
 */
fun getRelativeSqlFilePathFromDaoFilePath(
    daoFile: VirtualFile,
    module: Module?,
): String {
    if (module == null) return ""
    val extension = daoFile.fileType.defaultExtension
    val sourceRoot = module.project.getSourceRootDir(daoFile) ?: return ""

    val project = module.project
    val sourceRootParent = project.getContentRoot(daoFile) ?: return ""
    val daoFilePath = daoFile.path

    return daoFilePath
        .substringAfter(sourceRootParent.path)
        .replaceFirst("/${sourceRoot.name}", RESOURCES_META_INF_PATH)
        .replace(".$extension", "")
}
