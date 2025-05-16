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

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.domaframework.doma.intellij.common.CommonPathParameterHelper.RESOURCES_META_INF_PATH

fun getJarRoot(
    virtualFile: VirtualFile,
    originalFile: PsiFile,
): VirtualFile? {
    val jarRootPath = virtualFile.path.substringBefore("jar!").plus("jar!")
    val jarRoot =
        StandardFileSystems
            .jar()
            .findFileByPath("$jarRootPath/")
    val methodDaoFilePath =
        getMethodDaoFilePath(virtualFile, jarRootPath, originalFile)

    val jarRootFile =
        jarRoot?.findFileByRelativePath(methodDaoFilePath.plus(".class"))
            ?: jarRoot?.findFileByRelativePath(methodDaoFilePath.plus(".java"))

    return jarRootFile
}

fun getMethodDaoFilePath(
    virtualFile: VirtualFile,
    jarRootPath: String,
    originalFile: PsiFile,
): String {
    val methodDaoFilePath =
        virtualFile.path
            .substringAfter(
                jarRootPath,
            ).replace("/$RESOURCES_META_INF_PATH", "")
            .replace("/${originalFile.name}", "")

    return methodDaoFilePath
}
