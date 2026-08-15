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
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiMethod
import com.intellij.util.ExceptionUtil
import com.intellij.util.PsiNavigateUtil
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.extension.findFile
import org.jetbrains.uast.UFile
import org.jetbrains.uast.toUElementOfType

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
        null -> return
        else -> getFunctionOffsetByUast(daoPsiFile, sqlFileName)
    }
}

/**
 * Navigate to a DAO method in a non Java source file.
 *
 * UAST keeps this language agnostic: the Kotlin plugin contributes its UAST support through an
 * extension point, so no Kotlin plugin class is referenced from here. That matters because the
 * Kotlin plugin is only an optional dependency of this plugin.
 */
private fun getFunctionOffsetByUast(
    file: PsiFile,
    targetMethodName: String,
) {
    val uastFile = file.toUElementOfType<UFile>() ?: return
    val method =
        uastFile.classes
            .flatMap { clazz -> clazz.methods.asIterable() }
            .find { m -> m.name == targetMethodName } ?: return
    PsiNavigateUtil.navigate(method.sourcePsi ?: method.javaPsi)
}

private fun getJavaFunctionOffset(
    file: PsiJavaFile,
    targetMethodName: String,
) {
    try {
        val dapMethod = findUseSqlDaoMethod(file, targetMethodName) ?: return
        PsiNavigateUtil.navigate(dapMethod)
    } catch (e: Exception) {
        ExceptionUtil.rethrow(e)
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
