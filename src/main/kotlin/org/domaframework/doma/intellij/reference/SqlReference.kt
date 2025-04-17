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
package org.domaframework.doma.intellij.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.extension.psi.findParameter
import org.domaframework.doma.intellij.extension.psi.getDomaAnnotationType
import org.domaframework.doma.intellij.extension.psi.getIterableClazz
import org.domaframework.doma.intellij.extension.psi.methodParameters
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

class SqlReference(
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    val file: PsiFile? = element.containingFile

    override fun resolve(): PsiElement? {
        if (file == null || !isSupportFileType(file)) return null
        val startTime = System.nanoTime()
        val variableName = element.text

        val staticDirective = getStaticDirective(element, variableName, startTime)
        if (staticDirective != null) {
            return staticDirective
        }

        val targetElement = getBlockCommentElements(element)
        if (targetElement.isEmpty()) return null

        val daoMethod = findDaoMethod(file) ?: return null

        return when (element.textOffset) {
            targetElement.first().textOffset ->
                getReferenceDaoMethodParameter(
                    daoMethod,
                    element,
                    startTime,
                )

            else -> getReferenceEntity(daoMethod, targetElement, startTime)
        }

        return null
    }

    override fun getVariants(): Array<Any> = emptyArray()

    private fun getStaticDirective(
        staticDirection: PsiElement?,
        elementName: String,
        startTime: Long,
    ): PsiElement? {
        if (staticDirection == null) return null
        val file: PsiFile = file ?: return null
        // Jump to class definition
        if (staticDirection is SqlElClass ||
            staticDirection.parent is SqlElClass
        ) {
            val psiStaticElement = PsiStaticElement(staticDirection.text, file)
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "ReferenceStaticClass",
                "Reference",
                startTime,
            )
            return psiStaticElement.getRefClazz()
        }

        // Jump from field or method to definition (assuming the top element is static)
        val staticAccessParent = PsiTreeUtil.getParentOfType(staticDirection, SqlElStaticFieldAccessExpr::class.java)
        if (staticAccessParent != null) {
            val firstChildText =
                staticAccessParent.children
                    .firstOrNull()
                    ?.text ?: ""
            val psiStaticElement =
                PsiStaticElement(
                    firstChildText,
                    file,
                )
            val javaClazz = psiStaticElement.getRefClazz() ?: return null
            val psiParentClass = PsiParentClass(PsiTypesUtil.getClassType(javaClazz))
            psiParentClass.findField(elementName)?.let {
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "ReferenceStaticField",
                    "Reference",
                    startTime,
                )
                return it
            }
            psiParentClass.findMethod(elementName)?.let {
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "ReferenceStaticMethod",
                    "Reference",
                    startTime,
                )
                return it
            }
        }
        return null
    }

    private fun getBlockCommentElements(element: PsiElement): List<PsiElement> {
        val nodeElm =
            if (element.parent is SqlElFieldAccessExpr) {
                PsiTreeUtil
                    .getChildrenOfType(
                        element.parent,
                        SqlElIdExpr::class.java,
                    )?.filter { it.textOffset <= element.textOffset }
            } else {
                listOf(element)
            }
        return nodeElm
            ?.toList()
            ?.sortedBy { it.textOffset } ?: emptyList()
    }

    private fun getReferenceDaoMethodParameter(
        daoMethod: PsiMethod,
        it: PsiElement,
        startTime: Long,
    ): PsiElement? {
        daoMethod
            .let { method ->
                method.methodParameters.firstOrNull { param ->
                    param.name == it.text
                }
            }?.originalElement
            ?.let { originalElm ->
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "ReferenceDaoMethodParameter",
                    "Reference",
                    startTime,
                )
                return originalElm
            } ?: return null
    }

    private fun getReferenceEntity(
        daoMethod: PsiMethod,
        targetElement: List<PsiElement>,
        startTime: Long,
    ): PsiElement? {
        val topParam = daoMethod.findParameter(targetElement.first().text) ?: return null
        val parentClass = topParam.getIterableClazz(daoMethod.getDomaAnnotationType())
        val bindEntity =
            getBindProperty(
                targetElement.toList(),
                parentClass,
            )

        PluginLoggerUtil.countLogging(
            this::class.java.simpleName,
            "ReferenceEntityProperty",
            "Reference",
            startTime,
        )
        return bindEntity
    }

    private fun getBindProperty(
        elementBlock: List<PsiElement>,
        topElementClass: PsiParentClass,
    ): PsiElement? {
        // If the argument is List, get the element type
        var parentClass = topElementClass
        val accessElms = elementBlock.drop(1)
        var isExistProperty: Boolean

        fun getBindVariableIfLastIndex(
            index: Int,
            type: PsiType,
            originalElement: PsiElement,
        ): PsiElement? {
            isExistProperty = true
            if (index >= accessElms.size - 1) {
                return originalElement
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
                    val reference = getBindVariableIfLastIndex(index, it.type, it.originalElement)
                    if (reference != null) return reference
                }
            if (isExistProperty) continue
            parentClass
                .findMethod(elm.text)
                ?.let {
                    val returnType = it.returnType ?: return null
                    val reference =
                        getBindVariableIfLastIndex(index, returnType, it.originalElement)
                    if (reference != null) return reference
                }
            if (!isExistProperty) return null
        }
        return null
    }
}
