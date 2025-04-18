package org.domaframework.doma.intellij.common

import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.domaframework.doma.intellij.common.CommonPathParameter.Companion.RESOURCES_META_INF_PATH

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
    return jarRoot?.findFileByRelativePath(methodDaoFilePath)
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
            .plus(".class")
    return methodDaoFilePath
}
