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

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.util.PsiNavigateUtil
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.extension.findFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.utils.rethrow

fun jumpSqlFromDao(
    project: Project,
    sqlFile: VirtualFile,
) {
    FileEditorManager.getInstance(project).openFile(sqlFile, true)
}

fun jumpToDaoMethod(
    project: Project,
    sqlFileName: String,
    daoFile: VirtualFile,
) {
    when (val daoPsiFile = project.findFile(daoFile)) {
        is PsiJavaFile -> getJavaFunctionOffset(daoPsiFile, sqlFileName)
        is KtFile -> getKotlinFunctions(daoPsiFile, sqlFileName)
    }
}

// TODO Support Kotlin Project
private fun getKotlinFunctions(
    file: KtFile,
    targetMethodName: String,
) {
    val method =
        file.declarations
            .filterIsInstance<KtNamedFunction>()
            .find { f -> f.name == targetMethodName }
    if (method != null) {
        PsiNavigateUtil.navigate(method)
    }
}

private fun getJavaFunctionOffset(
    file: PsiJavaFile,
    targetMethodName: String,
) {
    try {
        val dapMethod = findUseSqlDaoMethod(file, targetMethodName) ?: return
        PsiNavigateUtil.navigate(dapMethod)
    } catch (e: Exception) {
        rethrow(e)
    }
}

fun findUseSqlDaoMethod(
    file: PsiJavaFile,
    targetMethodName: String,
): PsiMethod? {
    for (clazz in file.classes) {
        val methods = clazz.findMethodsByName(targetMethodName, true)
        if (methods.isNotEmpty()) {
            val targetMethod =
                methods.firstOrNull { method ->
                    val psiDaoMethod = PsiDaoMethod(file.project, method)
                    // When jumping after generating an annotation from an SQL file,
                    // since the SQL annotation is already present, allow jumping even if the SQL file also exists.
                    psiDaoMethod.isUseSqlFileMethod() || (psiDaoMethod.useSqlAnnotation() && psiDaoMethod.sqlFile != null)
                }
            return targetMethod
            break
        }
    }
    return null
}
