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
package org.domaframework.doma.intellij.common

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.domaframework.doma.intellij.common.CommonPathParameterHelper.SRC_MAIN_PATH

/**
 * Get extension by file type identifier
 */
fun getExtension(type: String): String =
    when (type) {
        "JAVA" -> "java"
        "Kotlin" -> "kt"
        "SQL" -> "sql"
        "CLASS" -> "class"
        else -> {
            ""
        }
    }

/**
 * Does it match the Dao file type condition?
 */
fun isJavaOrKotlinFileType(daoFile: PsiFile): Boolean {
    if (daoFile.virtualFile == null) return false
    val fileType = FileTypeManager.getInstance().getFileTypeByFile(daoFile.virtualFile)
    return when (fileType.name) {
        "JAVA", "Kotlin", "CLASS" -> true
        else -> false
    }
}

/*
 * Determine whether the open file is an SQL template file extension
 */
fun isSupportFileType(file: PsiFile): Boolean {
    val extension = file.fileType.defaultExtension
    return when (extension) {
        "sql", "script" -> true
        else -> false
    }
}

fun isInjectionSqlFile(file: PsiFile): Boolean {
    val extension = file.fileType.defaultExtension
    val filePath = file.virtualFile?.path ?: return false
    return when (extension) {
        "sql" -> true
        else -> false
    } &&
        !(filePath.endsWith(".sql") || filePath.endsWith(".script"))
}

/**
 * Dao file search for SQL files
 */
fun searchDaoFile(
    contentRoot: VirtualFile?,
    originFilePath: String,
    relativeDaoFilePath: String,
): VirtualFile? {
    val projectRootPath = contentRoot?.path ?: return null
    if (projectRootPath.endsWith(SRC_MAIN_PATH)) {
        return contentRoot.findFileByRelativePath(relativeDaoFilePath)
    }

    if (projectRootPath.length > originFilePath.length) {
        return null
    }

    val subProject =
        originFilePath.substring(projectRootPath.length, originFilePath.indexOf(SRC_MAIN_PATH))
    return contentRoot
        .findFileByRelativePath(subProject)
        ?.findFileByRelativePath(relativeDaoFilePath)
}
