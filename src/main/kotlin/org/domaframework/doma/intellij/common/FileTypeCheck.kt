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
import com.intellij.psi.PsiFile

val sourceExtensionNames: List<String> = listOf("JAVA", "Kotlin", "CLASS")

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
 * Does it match the DAO file type condition?
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
