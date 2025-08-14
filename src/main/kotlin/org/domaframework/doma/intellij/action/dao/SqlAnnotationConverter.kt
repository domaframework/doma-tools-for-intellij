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
package org.domaframework.doma.intellij.action.dao

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNameValuePair
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import org.domaframework.doma.intellij.common.dao.jumpToDaoMethod
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.common.util.InjectionSqlUtil
import org.domaframework.doma.intellij.common.util.StringUtil
import org.domaframework.doma.intellij.extension.findFile
import org.domaframework.doma.intellij.extension.psi.DomaAnnotationType
import org.domaframework.doma.intellij.formatter.processor.InjectionSqlFormatter
import org.domaframework.doma.intellij.formatter.visitor.FormattingTask

/**
 * Class that handles conversion between @Sql annotation and SQL files
 */
class SqlAnnotationConverter(
    private val project: Project,
    private val method: PsiMethod,
) {
    private val psiDaoMethod = PsiDaoMethod(project, method)
    private val elementFactory = JavaPsiFacade.getElementFactory(project)

    /**
     * Convert @Sql annotation to SQL file
     */
    fun convertToSqlFile() {
        val sqlAnnotation = DomaAnnotationType.Sql.getPsiAnnotation(method) ?: return
        val sqlContent = extractSqlContent(sqlAnnotation) ?: return
        val targetAnnotation = findTargetAnnotation() ?: return

        setSqlFileOption(targetAnnotation, true)
        sqlAnnotation.delete()
        generateSqlFileWithContent(sqlContent)
    }

    /**
     * Convert SQL file to @Sql annotation
     */
    fun convertToSqlAnnotation() {
        val sqlFile = psiDaoMethod.sqlFile ?: return
        val sqlContent = readSqlFileContent(sqlFile) ?: return
        val targetAnnotation = findTargetAnnotation() ?: return

        setSqlFileOption(targetAnnotation, false)
        addSqlAnnotation(sqlContent)
        deleteSqlFile(sqlFile)
        formatInjectionSql(targetAnnotation)
    }

    private fun extractSqlContent(sqlAnnotation: PsiAnnotation): String? {
        val valueExpression = getSqlAnnotationLiteralExpression(sqlAnnotation)
        return valueExpression?.value as? String
    }

    private fun getSqlAnnotationLiteralExpression(sqlAnnotation: PsiAnnotation): PsiLiteralExpression? {
        val valuePair =
            sqlAnnotation.parameterList.children
                .firstOrNull { it is PsiNameValuePair } as? PsiNameValuePair

        val valueExpression = valuePair?.value as? PsiLiteralExpression
        return valueExpression
    }

    private fun readSqlFileContent(virtualFile: VirtualFile): String? {
        val psiFile = project.findFile(virtualFile) ?: return null
        return psiFile.text
    }

    private fun findTargetAnnotation(): PsiAnnotation? {
        val supportedTypes =
            listOf(
                DomaAnnotationType.Select,
                DomaAnnotationType.Script,
                DomaAnnotationType.SqlProcessor,
                DomaAnnotationType.Insert,
                DomaAnnotationType.Update,
                DomaAnnotationType.Delete,
                DomaAnnotationType.BatchInsert,
                DomaAnnotationType.BatchUpdate,
                DomaAnnotationType.BatchDelete,
            )

        for (type in supportedTypes) {
            val annotation = type.getPsiAnnotation(method)
            if (annotation != null) {
                return annotation
            }
        }

        return null
    }

    private fun setSqlFileOption(
        annotation: PsiAnnotation,
        value: Boolean,
    ) {
        var existsAnnotation = annotation
        val useSqlFileOptionAnnotationList =
            listOf(
                DomaAnnotationType.Insert,
                DomaAnnotationType.Update,
                DomaAnnotationType.Delete,
                DomaAnnotationType.BatchInsert,
                DomaAnnotationType.BatchUpdate,
                DomaAnnotationType.BatchDelete,
            )

        val documentManager = PsiDocumentManager.getInstance(project)
        if (useSqlFileOptionAnnotationList.contains(psiDaoMethod.daoType)) {
            val existingAttribute =
                annotation.parameterList.attributes
                    .find { it.name == "sqlFile" }

            if (value) {
                // Add or update sqlFile = true
                val attributeText = "sqlFile = true"
                val dummyAnnotation =
                    elementFactory.createAnnotationFromText(
                        "@Dummy($attributeText)",
                        null,
                    )
                val newAttribute = dummyAnnotation.parameterList.attributes[0]

                if (existingAttribute != null) {
                    existingAttribute.replace(newAttribute)
                } else {
                    annotation.parameterList.add(newAttribute)
                }
            } else {
                existingAttribute?.delete()
                // If no attributes remain, recreate annotation without parentheses
                if (annotation.parameterList.attributes.isEmpty()) {
                    val annotationName = annotation.qualifiedName
                    val newAnnotationText = "@$annotationName"
                    val newAnnotation = elementFactory.createAnnotationFromText(newAnnotationText, annotation)
                    val modifierList = method.modifierList
                    modifierList.addBefore(newAnnotation, annotation)
                    annotation.delete()
                    existsAnnotation = newAnnotation
                }
            }
        }

        val psiFile = annotation.containingFile
        val document = documentManager.getDocument(psiFile)
        if (document != null) {
            documentManager.doPostponedOperationsAndUnblockDocument(document)
        }
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(existsAnnotation)
    }

    private fun addSqlAnnotation(sqlContent: String) {
        val escapedContent =
            sqlContent
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")

        val annotationText = "@Sql(\"\"\"\n$escapedContent\"\"\")"
        val sqlAnnotation = elementFactory.createAnnotationFromText(annotationText, null)

        val modifierList = method.modifierList
        val targetAnnotation = findTargetAnnotation()

        if (targetAnnotation != null) {
            // Add after the target annotation
            modifierList.addAfter(sqlAnnotation, targetAnnotation)
            // modifierList.addAfter(elementFactory.createWhiteSpaceFromText("\n"), targetAnnotation)
        } else {
            // Add at the beginning
            modifierList.add(sqlAnnotation)
        }
        val psiFile = method.containingFile
        val documentManager = PsiDocumentManager.getInstance(project)
        val document = documentManager.getDocument(psiFile)
        if (document != null) {
            documentManager.doPostponedOperationsAndUnblockDocument(document)
        }
        // Add import if needed
        val containingFile = method.containingFile
        // TODO Support Kotlin files in the future
        if (containingFile is PsiJavaFile) {
            val importList = containingFile.importList
            val sqlImport = DomaAnnotationType.Sql.fqdn

            val hasImport =
                importList?.importStatements?.any {
                    it.qualifiedName == sqlImport
                } == true

            if (!hasImport) {
                val importStatement =
                    elementFactory.createImportStatement(
                        JavaPsiFacade.getInstance(project).findClass(
                            sqlImport,
                            method.resolveScope,
                        ) ?: return,
                    )
                importList?.add(importStatement)
                val psiFile = method.containingFile
                val document = documentManager.getDocument(psiFile)
                if (document != null) {
                    documentManager.doPostponedOperationsAndUnblockDocument(document)
                }
            }
        }

        // Jump to method
        val newDaoFile = method.containingFile
        val newDocument = documentManager.getDocument(newDaoFile)
        if (newDocument != null) {
            documentManager.doPostponedOperationsAndUnblockDocument(newDocument)
        }
        jumpToDaoMethod(project, psiDaoMethod.sqlFile?.name ?: return, newDaoFile.virtualFile)
    }

    private fun generateSqlFileWithContent(content: String) {
        // First generate the empty SQL file using existing functionality
        psiDaoMethod.generateSqlFile()

        // Then update its content
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                // Re-fetch the SQL file after generation
                val newPsiDaoMethod = PsiDaoMethod(project, method)
                val sqlFile = newPsiDaoMethod.sqlFile ?: return@runWriteCommandAction
                val psiFile = project.findFile(sqlFile) ?: return@runWriteCommandAction

                val documentManager = PsiDocumentManager.getInstance(project)
                val document = documentManager.getDocument(psiFile) ?: return@runWriteCommandAction

                // Replace the default content with the actual SQL content
                document.setText(content)
                documentManager.commitDocument(document)

                // Format Sql
                formatSql(psiFile)
            }
        }
    }

    private fun deleteSqlFile(virtualFile: VirtualFile) {
        // Close the file if it's open in the editor
        val editorManager = FileEditorManager.getInstance(project)
        if (editorManager.isFileOpen(virtualFile)) {
            editorManager.closeFile(virtualFile)
        }

        // Delete the file
        virtualFile.delete(this)
    }

    private fun formatSql(sqlFile: PsiFile) {
        val method = psiDaoMethod.psiMethod
        val project = method.project
        val injectionFormatter = InjectionSqlFormatter(project)
        injectionFormatter.format(sqlFile) { text ->
            processDocumentText(text)
        }
    }

    private fun formatInjectionSql(annotation: PsiAnnotation) {
        val method = psiDaoMethod.psiMethod
        val sqlAnnotation = DomaAnnotationType.Sql.getPsiAnnotation(method) ?: return
        val valueExpression = getSqlAnnotationLiteralExpression(sqlAnnotation) ?: return
        val injectedSql = InjectionSqlUtil.initInjectionElement(method.containingFile, project, valueExpression) ?: return

        val host = InjectionSqlUtil.getLiteralExpressionHost(injectedSql.containingFile) ?: return
        val originalText = host.value?.toString() ?: return

        val injectionFormatter = InjectionSqlFormatter(annotation.project)
        val formattingTask = FormattingTask(host, originalText, false)
        injectionFormatter.convertExpressionToTextBlock(formattingTask.expression)
        injectionFormatter.processFormattingTask(formattingTask) { text ->
            processDocumentText(text)
        }
    }

    private fun processDocumentText(originalText: String): String {
        val withoutTrailingSpaces = removeTrailingSpaces(originalText)
        return ensureProperFileEnding(withoutTrailingSpaces)
    }

    private fun removeTrailingSpaces(text: String): String {
        val trailingSpacesRegex = Regex(" +(\r?\n)")
        return text.replace(trailingSpacesRegex, "$1")
    }

    private fun ensureProperFileEnding(text: String): String = text.trimEnd() + StringUtil.LINE_SEPARATE
}
