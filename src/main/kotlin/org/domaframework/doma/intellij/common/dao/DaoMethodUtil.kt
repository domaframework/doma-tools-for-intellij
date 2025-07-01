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
import org.domaframework.doma.intellij.common.sourceExtensionNames
import org.domaframework.doma.intellij.extension.findFile
import org.domaframework.doma.intellij.extension.getContentRoot
import org.domaframework.doma.intellij.extension.getJavaClazz
import org.domaframework.doma.intellij.extension.getModule
import org.jetbrains.kotlin.idea.base.util.module
import java.nio.file.Paths

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
        // TODO: Add Support Kotlin
        val methodName = virtualFile.nameWithoutExtension
        val daoFile = daoFile ?: findDaoFile(project, originalFile) ?: return null
        if (module != null) {
            val relativePath =
                getDaoPathFromSqlFilePath(
                    originalFile,
                    project.getContentRoot(virtualFile)?.path ?: "",
                )
            // get ClassPath with package name
            val daoClassName: String =
                relativePath
                    .substringBefore(".")
                    .replace("/", ".")
                    .replace("\\", ".")
                    .replace("..", ".")
                    .trim('.')

            val daoJavaFile = project.findFile(daoFile)
            val isTest = CommonPathParameterUtil.isTest(module, originalFile.virtualFile)
            findDaoClass(module, isTest, daoClassName)
                ?.let { daoClass ->
                    val daoMethod =
                        // TODO Support Kotlin Project
                        when (daoJavaFile) {
                            is PsiJavaFile -> findUseSqlDaoMethod(daoJavaFile, methodName)
                            else -> null
                        }
                    return daoMethod
                }
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
    return searchDaoFile(sqlFile.module, contentRoot, sqlFile)
}

/**
 * DAO file search for SQL file
 */
private fun searchDaoFile(
    module: Module?,
    contentRoot: VirtualFile?,
    sqlFile: PsiFile,
): VirtualFile? {
    val contentRootPath = contentRoot?.path ?: return null
    val pathParams = module?.let { CommonPathParameterUtil.getModulePaths(it) } ?: return null
    val moduleBaseName =
        pathParams.moduleBasePaths
            .find { baseName -> Paths.get(contentRootPath).startsWith(Paths.get(baseName.path)) }
            ?.nameWithoutExtension ?: ""
    // TODO: Add Support Kotlin
    val relativeDaoFilePaths =
        getDaoPathFromSqlFilePath(sqlFile, contentRoot.path)
    val sources = CommonPathParameterUtil.getSources(module, sqlFile.virtualFile)

    if (contentRootPath.endsWith(moduleBaseName) == true) {
        sources.forEach { source ->
            sourceExtensionNames.forEach { extension ->
                val fileExtension = getExtension(extension)
                val findDaoFile =
                    contentRoot.findFileByRelativePath("${source.nameWithoutExtension}$relativeDaoFilePaths.$fileExtension")
                if (findDaoFile != null) return findDaoFile
            }
        }
    }
    return null
}

private fun findDaoClass(
    module: Module,
    includeTest: Boolean,
    daoClassName: String,
): PsiClass? = module.getJavaClazz(includeTest, daoClassName)

/**
 * Generate DAO deployment path from SQL file path
 * @param sqlFile SQL File
 * @param projectRootPath project content Root Path
 * @return
 */
private fun getDaoPathFromSqlFilePath(
    sqlFile: PsiFile,
    projectRootPath: String,
): String {
    if (isInjectionSqlFile(sqlFile)) {
        return ""
    }
    val module = sqlFile.module
    val sqlPath = sqlFile.virtualFile?.path ?: return ""
    var relativeFilePath = sqlPath.substring(projectRootPath.length)
    if (!relativeFilePath.startsWith("/")) {
        relativeFilePath = "/$relativeFilePath"
    }
    val resources =
        module?.let { CommonPathParameterUtil.getResources(it, sqlFile.virtualFile) }
            ?: emptyList()

    return resources
        .find { resource ->
            relativeFilePath.startsWith("/${resource.nameWithoutExtension}/")
        }?.let { resource ->
            relativeFilePath
                .replace("${resource.nameWithoutExtension}/$RESOURCES_META_INF_PATH/", "")
                .replace("/${sqlFile.name}", "")
        } ?: ""
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
    val daoFilePath = daoFile.path
    val pathParams = CommonPathParameterUtil.getModulePaths(module)
    val containsModuleBaseName =
        pathParams.moduleBasePaths
            .find { basePath -> daoFilePath.contains("${basePath.path}/") }
            ?.path ?: return ""
    var relativeSqlFilePath =
        daoFilePath
            .replaceFirst(containsModuleBaseName, "")
            .replace(".$extension", "")
    val sources = CommonPathParameterUtil.getSources(module, daoFile)
    sources
        .find {
            daoFilePath
                .startsWith(containsModuleBaseName.plus("/${it.nameWithoutExtension}/"))
        }?.let { source ->
            val startSourceName = "/${source.nameWithoutExtension}"
            if (relativeSqlFilePath.startsWith("$startSourceName/")) {
                relativeSqlFilePath =
                    relativeSqlFilePath.replaceFirst(
                        startSourceName,
                        RESOURCES_META_INF_PATH,
                    )
            }
        }
    return relativeSqlFilePath
}
