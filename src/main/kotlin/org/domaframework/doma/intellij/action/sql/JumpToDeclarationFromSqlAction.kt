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
package org.domaframework.doma.intellij.action.sql

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz
import org.domaframework.doma.intellij.extension.psi.isNotWhiteSpace
import org.domaframework.doma.intellij.extension.psi.methodParameters
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElPrimaryExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

/**
 * Action to jump from SQL bind variable to Dao method argument and Entity class
 */
class JumpToDeclarationFromSqlAction : AnAction() {
    private var element: PsiElement? = null
    private var currentFile: PsiFile? = null
    private var logActionFileType = "Sql"

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val caretOffset = editor.caretModel.primaryCaret.selectionStart

        currentFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        currentFile?.let { element = it.findElementAt(caretOffset) ?: return }
        if (element == null) return

        val project = element?.project ?: return
        if (isJavaOrKotlinFileType(currentFile ?: return)) {
            val injectedLanguageManager =
                InjectedLanguageManager.getInstance(project)
            val literal = PsiTreeUtil.getParentOfType(element, PsiLiteralExpression::class.java)
            currentFile =
                injectedLanguageManager
                    .getInjectedPsiFiles(literal!!)
                    ?.firstOrNull()
                    ?.first as? PsiFile
            element = currentFile?.findElementAt(countInjectionOffset(literal, caretOffset))
        }
        findDaoMethod(currentFile ?: return) ?: return

        val staticDirection = element?.parent
        val staticDirective = getStaticDirective(staticDirection, element!!.text)
        if (staticDirective != null) {
            e.presentation.isEnabledAndVisible = true
            return
        }

        val targetElement = getBlockCommentElements(element ?: return)
        if (targetElement.isNotEmpty()) e.presentation.isEnabledAndVisible = true
    }

    private fun countInjectionOffset(
        literal: PsiLiteralExpression,
        caretOffset: Int,
    ): Int {
        val quoteCount = countLeadingDoubleQuotes(literal.text)
        val literalOffset = caretOffset - literal.textOffset
        val indentCount =
            literal.text.substring(0, literalOffset)
        val indentRegex = Regex("(?<=\\n)[ \\t]+")
        val indentSpacesCount = indentRegex.findAll(indentCount).sumOf { it.value.length }
        return literalOffset - quoteCount - indentSpacesCount
    }

    private fun countLeadingDoubleQuotes(s: String): Int {
        var count = 0
        for (i in 0..<s.length) {
            if (s[i] == '"') {
                count++
            } else {
                break
            }
        }
        return count
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent) {
        val startTime = System.nanoTime()
        val staticDirection = element?.parent
        val staticDirective = getStaticDirective(staticDirection, element!!.text)
        if (staticDirective != null) {
            BindVariableElement(staticDirective).jumpToEntity()
            PluginLoggerUtil.countLoggingByAction(
                this::class.java.simpleName,
                "JumpToStaticBy$logActionFileType",
                e.inputEvent,
                startTime,
            )
            return
        }
        if (element == null) return
        // TODO Since the update also checks whether the action is to be executed,
        //  delete it if it is unnecessary.
        if (isNotBindVariable(element!!)) return

        val targetElement = getBlockCommentElements(element!!)
        if (targetElement.isEmpty()) return

        val daoMethod = currentFile?.let { file -> findDaoMethod(file) } ?: return
        when (element!!.textOffset) {
            targetElement.first().textOffset ->
                jumpToDaoMethodParameter(
                    daoMethod,
                    element!!,
                    logActionFileType,
                    e,
                    startTime,
                )

            else -> jumpToEntity(daoMethod, targetElement, logActionFileType, e, startTime)
        }
    }

    private fun getStaticDirective(
        staticDirection: PsiElement?,
        elementName: String,
    ): PsiElement? {
        if (staticDirection == null) return null

        // Jump to class definition
        if (staticDirection is SqlElClass) {
            val psiStaticElement = PsiStaticElement(staticDirection.text, currentFile!!)
            return psiStaticElement.getRefClazz()
        }

        // Jump from field or method to definition (assuming the top element is static)
        val staticAccessParent = staticDirection.parent
        if (staticDirection is SqlElStaticFieldAccessExpr ||
            staticAccessParent is SqlElStaticFieldAccessExpr
        ) {
            val psiStaticElement =
                PsiStaticElement(
                    staticAccessParent.children
                        .firstOrNull()
                        ?.text ?: "",
                    currentFile!!,
                )
            val javaClazz = psiStaticElement.getRefClazz() ?: return null
            val psiParentClass = PsiParentClass(PsiTypesUtil.getClassType(javaClazz))
            psiParentClass.findField(elementName)?.let {
                return it
            }
            psiParentClass.findMethod(elementName)?.let {
                return it
            }
        }
        return null
    }

    /**
     * If you execute an action from a blank space,
     * it will trace back to just below the drive, so play it.
     */
    private fun isNotBindVariable(it: PsiElement) =
        (
            it.parent.elementType is IFileElementType &&
                it.elementType != SqlTypes.EL_IDENTIFIER &&
                it !is SqlElPrimaryExpr &&
                !it.isNotWhiteSpace()
        )

    private fun jumpToDaoMethodParameter(
        daoMethod: PsiMethod,
        it: PsiElement,
        logActionFileType: String,
        e: AnActionEvent,
        startTime: Long,
    ) {
        daoMethod
            .let { method ->
                method.methodParameters.firstOrNull { param ->
                    param.name == it.text
                }
            }?.originalElement
            ?.let { originalElm ->
                BindVariableElement(originalElm).jumpToDao()
                PluginLoggerUtil.countLoggingByAction(
                    this::class.java.simpleName,
                    "JumpToDaoMethodParameterBy$logActionFileType",
                    e.inputEvent,
                    startTime,
                )
                return
            }
    }

    private fun jumpToEntity(
        daoMethod: PsiMethod,
        targetElement: List<PsiElement>,
        logActionFileType: String,
        e: AnActionEvent,
        startTime: Long,
    ) {
        val topParam = daoMethod.findParameter(targetElement.first().text) ?: return
        val parentClass = topParam.getIterableClazz(daoMethod.getDomaAnnotationType())
        val bindEntity =
            getBindProperty(
                targetElement.toList(),
                parentClass,
            )
        bindEntity?.jumpToEntity()
        PluginLoggerUtil.countLoggingByAction(
            this::class.java.simpleName,
            "JumpToBindVariableBy$logActionFileType",
            e.inputEvent,
            startTime,
        )
    }

    private fun getBlockCommentElements(element: PsiElement): List<PsiElement> {
        val nodeElm =
            PsiTreeUtil
                .getChildrenOfType(element.parent, PsiElement::class.java)
                ?.filter {
                    (
                        it.elementType == SqlTypes.EL_IDENTIFIER ||
                            it is SqlElPrimaryExpr
                    ) &&
                        it.textOffset <= element.textOffset
                }?.toList()
                ?.sortedBy { it.textOffset } ?: emptyList()
        return nodeElm
    }

    /**
     * Follow the element at the cursor position and get the class of the nearest parent
     */
    private fun getBindProperty(
        elementBlock: List<PsiElement>,
        topElementClass: PsiParentClass,
    ): BindVariableElement? {
        // If the argument is List, get the element type
        var parentClass = topElementClass
        val accessElms = elementBlock.drop(1)
        var isExistProperty: Boolean

        fun getBindVariableIfLastIndex(
            index: Int,
            type: PsiType,
            originalElement: PsiElement,
        ): BindVariableElement? {
            isExistProperty = true
            if (index >= accessElms.size - 1) {
                return BindVariableElement(originalElement)
            }
            parentClass = PsiParentClass(type)
            return null
        }

        for (index in accessElms.indices) {
            isExistProperty = false
            val elm = accessElms[index]

            parentClass
                .findField(elm.text)
                ?.let {
                    val bindVal = getBindVariableIfLastIndex(index, it.type, it.originalElement)
                    if (bindVal != null) return bindVal
                }
            if (isExistProperty) continue
            parentClass
                .findMethod(elm.text)
                ?.let {
                    if (it.returnType == null) return null
                    val bindVal =
                        getBindVariableIfLastIndex(index, it.returnType!!, it.originalElement)
                    if (bindVal != null) return bindVal
                }
            if (!isExistProperty) return null
        }
        return null
    }
}
