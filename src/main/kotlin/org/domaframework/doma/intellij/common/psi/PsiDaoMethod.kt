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
package org.domaframework.doma.intellij.common.psi

import com.intellij.lang.Language
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNameValuePair
import com.intellij.util.IncorrectOperationException
import org.domaframework.doma.intellij.common.CommonPathParameterUtil
import org.domaframework.doma.intellij.common.RESOURCES_META_INF_PATH
import org.domaframework.doma.intellij.common.dao.getRelativeSqlFilePathFromDaoFilePath
import org.domaframework.doma.intellij.common.getExtension
import org.domaframework.doma.intellij.extension.findFile
import org.domaframework.doma.intellij.extension.getContentRoot
import org.domaframework.doma.intellij.extension.getModule
import org.domaframework.doma.intellij.extension.getResourcesSQLFile
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.setting.SqlLanguage
import org.jetbrains.kotlin.idea.base.util.module
import java.io.File
import java.io.IOException

/**
 * Class that handles Dao method information
 */
class PsiDaoMethod(
    private val psiProject: Project,
    val psiMethod: PsiMethod,
) {
    private var isTest = false
    var sqlFile: VirtualFile? = null
    private var sqlFilePath: String = ""

    private val daoFile: VirtualFile =
        psiMethod.containingFile.virtualFile
            ?: psiMethod.containingFile.originalFile.virtualFile
    var daoType: DomaAnnotationType = DomaAnnotationType.Unknown
    private var sqlFileOption: Boolean = false

    init {
        setDaoAnnotationType()
        setSqlFileOption()
        setSqlFilePath()
        setTest()
        setSqlFile()
    }

    private fun setTest() {
        psiMethod.module?.let { isTest = CommonPathParameterUtil.isTest(it, daoFile) }
    }

    private fun setSqlFileOption() {
        val useSqlFileOptionAnnotation = daoType.getPsiAnnotation(psiMethod) ?: return
        val isSqlFile = daoType.getSqlFileVal(useSqlFileOptionAnnotation)
        sqlFileOption = isSqlFile == true
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun setDaoAnnotationType() {
        DomaAnnotationType.entries.forEach { type ->
            if (type != DomaAnnotationType.Sql &&
                type != DomaAnnotationType.Unknown &&
                type.getPsiAnnotation(psiMethod) != null
            ) {
                daoType = type
                return
            }
        }
        daoType = DomaAnnotationType.Unknown
    }

    fun isUseSqlFileMethod(): Boolean =
        when {
            useSqlAnnotation() -> false
            daoType.isRequireSqlTemplate() -> true
            else -> sqlFileOption
        }

    private fun getSqlAnnotation(): PsiAnnotation? = DomaAnnotationType.Sql.getPsiAnnotation(psiMethod)

    fun isSelectTypeCollect(): Boolean {
        val selectAnnotation = DomaAnnotationType.Select.getPsiAnnotation(psiMethod) ?: return false
        return daoType.isSelectTypeCollect(selectAnnotation)
    }

    fun useSqlAnnotation(): Boolean = getSqlAnnotation() != null

    private fun setSqlFilePath() {
        val methodName = psiMethod.name

        val sqlExtension = daoType.extension
        val contentRoot = this.psiProject.getContentRoot(daoFile)?.path
        if (contentRoot == null) {
            val fileType = getExtension(daoFile.fileType.name)
            val daoRelativePath =
                psiMethod.containingFile.originalFile.virtualFile.path
                    .substringAfter(".jar!")
            sqlFilePath =
                "$RESOURCES_META_INF_PATH${daoRelativePath.replace(".$fileType","")}/$methodName.$sqlExtension"
        } else {
            sqlFilePath =
                contentRoot.let {
                    getRelativeSqlFilePathFromDaoFilePath(daoFile, psiMethod.module)
                        .plus("/$methodName.$sqlExtension")
                }
        }
    }

    private fun setSqlFile() {
        if (isUseSqlFileMethod()) {
            val module = psiProject.getModule(daoFile)
            if (module == null) {
                val daoPath = daoFile.path
                val jarRootPath =
                    daoPath.substringBefore(".jar!") + ".jar!"
                val jarRoot =
                    StandardFileSystems
                        .jar()
                        .findFileByPath("$jarRootPath/")
                sqlFile = jarRoot?.findFileByRelativePath(sqlFilePath)
                return
            } else {
                sqlFile =
                    module.getResourcesSQLFile(
                        sqlFilePath,
                        isTest,
                    )
                return
            }
        }
        // the injection part as a custom language file
        getSqlAnnotation()?.let { annotation ->
            InjectedLanguageManager.getInstance(psiProject)
            annotation.parameterList.children
                .firstOrNull { it is PsiNameValuePair }
                ?.let { sql ->
                    val valuePair = sql as PsiNameValuePair
                    val valueExpression =
                        valuePair.value as? PsiLiteralExpression ?: return
                    val valueText = valueExpression.value as? String ?: return
                    val psiFileFactory = PsiFileFactory.getInstance(psiProject)
                    sqlFile =
                        psiFileFactory
                            .createFileFromText(
                                "tempFile",
                                Language.findLanguageByID(SqlLanguage.INSTANCE.id) ?: return,
                                valueText,
                            ).virtualFile
                }
        }
    }

    fun generateSqlFile() {
        ApplicationManager.getApplication().runReadAction {
            val rootDir = psiProject.getContentRoot(daoFile) ?: return@runReadAction
            val sqlFile = File(sqlFilePath)
            val sqlFileName = sqlFile.name
            val resourceDir =
                psiMethod.module
                    ?.let { CommonPathParameterUtil.getResources(it, daoFile).firstOrNull() }
                    ?: return@runReadAction
            val parentDir = "${resourceDir.nameWithoutExtension}/${sqlFile.parent?.replace("\\", "/")}"
            val parenDirPathSpirit = parentDir.split("/").toTypedArray()

            WriteCommandAction.runWriteCommandAction(psiProject) {
                try {
                    VfsUtil.createDirectoryIfMissing(rootDir, parentDir)
                } catch (e: IOException) {
                    throw IncorrectOperationException(e)
                }

                val virtualFile =
                    VfsUtil.findRelativeFile(rootDir, *parenDirPathSpirit)
                        ?: return@runWriteCommandAction
                val sqlOutputDirPath =
                    PsiManager
                        .getInstance(psiProject)
                        .findDirectory(virtualFile) ?: return@runWriteCommandAction
                val sqlVirtualFile = sqlOutputDirPath.createFile(sqlFileName).virtualFile ?: return@runWriteCommandAction
                FileEditorManager
                    .getInstance(psiProject)
                    .openFile(sqlVirtualFile, true)
                writeEmptyElementSqlFile(sqlVirtualFile)
            }
        }
    }

    /**
     * Add one line to display Gutter in the newly created SQL file
     */
    private fun writeEmptyElementSqlFile(sqlVirtualFile: VirtualFile) {
        val psiFile = psiProject.findFile(sqlVirtualFile) ?: return
        val document =
            PsiDocumentManager.getInstance(psiProject).getDocument(psiFile) ?: return
        WriteCommandAction.runWriteCommandAction(psiProject) {
            document.insertString(0, "-- Generated By Doma Tools")
        }
    }
}
