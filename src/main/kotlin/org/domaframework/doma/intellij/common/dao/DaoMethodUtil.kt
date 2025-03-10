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
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.CommonPathParameter.Companion.RESOURCES_META_INF_PATH
import org.domaframework.doma.intellij.common.CommonPathParameter.Companion.RESOURCES_PATH
import org.domaframework.doma.intellij.common.getExtension
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.common.searchDaoFile
import org.domaframework.doma.intellij.extension.findFile
import org.domaframework.doma.intellij.extension.getContentRoot
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.getModule

/**
 * Get Dao method corresponding to SQL file
 */
fun findDaoMethod(originalFile: PsiFile): PsiMethod? {
    val project = originalFile.project
    val module = project.getModule(originalFile.virtualFile) ?: return null

    if (isInjectionSqlFile(originalFile)) {
        originalFile.let {
            return PsiTreeUtil.getParentOfType(originalFile.context, PsiMethod::class.java)
        }
    } else if (isSupportFileType(originalFile)) {
        // TODO: Add Support Kotlin
        val fileTypeName = "JAVA"
        val daoFile = findDaoFile(project, originalFile) ?: return null
        val relativePath =
            formatDaoPathFromSqlFilePath(
                originalFile,
                project.getContentRoot(originalFile.virtualFile)?.path ?: "",
                fileTypeName,
            )
        val daoClassName: String =
            relativePath
                .substringBefore(".")
                .replace("/", ".")
                .replace("\\", ".")
                .substringAfter(".${getExtension(fileTypeName)}")
                .replace("..", ".")
                .trim('.')

        val daoJavaFile = project.findFile(daoFile)
        findDaoClass(module, daoClassName)?.let { daoClass ->
            val methodName = originalFile.name.substringBeforeLast(".")
            val daoMethod =
                when (daoJavaFile) {
                    is PsiJavaFile -> findUseSqlDaoMethod(daoJavaFile, methodName)
                    else -> null
                }
            return daoMethod
        }
    }
    return null
}

/**
 * Get jump destination Dao method file from SQL file
 */
fun findDaoFile(
    project: Project,
    sqlFile: PsiFile,
): VirtualFile? {
    val virtualFile = sqlFile.virtualFile ?: return null
    project.getModule(virtualFile) ?: return null
    val contentRoot = project.getContentRoot(virtualFile) ?: return null
    // TODO: Add Support Kotlin
    val relativeFilePath =
        formatDaoPathFromSqlFilePath(sqlFile, contentRoot.path, "JAVA")
    return searchDaoFile(contentRoot, virtualFile.path, relativeFilePath)
}

private fun findDaoClass(
    module: Module,
    daoClassName: String,
): PsiClass? = module.getJavaClazz(true, daoClassName)

/**
 * Generate Dao deployment path from SQL file path
 */
fun formatDaoPathFromSqlFilePath(
    relativeBaseSqlFile: PsiFile,
    projectRootPath: String,
    extension: String,
): String {
    if (isInjectionSqlFile(relativeBaseSqlFile)) {
        return ""
    }
    val sqlPath = relativeBaseSqlFile.virtualFile.path
    var relativeFilePath = sqlPath.substring(projectRootPath.length)
    if (!relativeFilePath.startsWith("/")) {
        relativeFilePath = "/$relativeFilePath"
    }
    val extensionType = getExtension(extension.uppercase())
    return relativeFilePath
        .replace("/$RESOURCES_PATH", "")
        .replace(RESOURCES_META_INF_PATH, extension.lowercase())
        .replace("/${relativeBaseSqlFile.name}", "")
        .plus(".$extensionType")
}

/**
 * Generate SqlFile path from Dao file path
 */
fun formatSqlPathFromDaoPath(
    contentRootPath: String,
    daoFile: VirtualFile,
): String {
    val fileType = daoFile.fileType.name
    val extension = daoFile.fileType.defaultExtension
    val daoFilePath = daoFile.path
    return daoFilePath
        .replace(contentRootPath, RESOURCES_META_INF_PATH)
        .replace("/${fileType.lowercase()}/", "/")
        .replace(".$extension", "")
}
