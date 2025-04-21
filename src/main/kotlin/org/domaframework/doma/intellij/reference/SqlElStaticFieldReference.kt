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
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.isSupportFileType
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlTypes

class SqlElStaticFieldReference(
    element: PsiElement,
) : PsiReferenceBase<PsiElement>(element) {
    private val cachedResolve: CachedValue<PsiElement?> by lazy {
        CachedValuesManager.getManager(element.project).createCachedValue {
            val result = doResolve()
            CachedValueProvider.Result(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    val file: PsiFile? = element.containingFile

    override fun resolve(): PsiElement? = cachedResolve.value

    private fun doResolve(): PsiElement? {
        if (file == null || !isSupportFileType(file)) return null
        val startTime = System.nanoTime()
        return superResolveLogic(startTime)
    }

    private fun superResolveLogic(startTime: Long): PsiElement? {
        val variableName = element.text
        val staticDirective = getStaticDirective(element, variableName, startTime)
        if (staticDirective != null) {
            return staticDirective
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

        // Jump from field or method to definition (assuming the top element is static)
        val staticAccessParent =
            PsiTreeUtil.getParentOfType(staticDirection, SqlElStaticFieldAccessExpr::class.java)
        if (staticAccessParent == null) return null

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
        var parentClass = PsiParentClass(PsiTypesUtil.getClassType(javaClazz))

        val targetElements = getBlockCommentElements(element)
        if (targetElements.isEmpty()) return null

        val topElm = targetElements.firstOrNull() as? PsiElement ?: return null
        val prevSibling = topElm.prevSibling ?: return null
        if (prevSibling.elementType != SqlTypes.AT_SIGN) return null
        var index = 1
        for (staticFieldAccess in targetElements) {
            if (index >= targetElements.size) {
                parentClass.findField(elementName)?.let {
                    PluginLoggerUtil.countLogging(
                        this::class.java.simpleName,
                        "ReferenceStaticField",
                        "Reference",
                        startTime,
                    )
                    return it
                }
                parentClass.findMethod(elementName)?.let {
                    PluginLoggerUtil.countLogging(
                        this::class.java.simpleName,
                        "ReferenceStaticMethod",
                        "Reference",
                        startTime,
                    )
                    return it
                }
            }

            val newParentType =
                parentClass.findField(staticFieldAccess.text)?.type
                    ?: parentClass.findMethod(staticFieldAccess.text)?.returnType
            if (newParentType == null) return null

            parentClass = PsiParentClass(newParentType)
            index++
        }
        return null
    }

    private fun getBlockCommentElements(element: PsiElement): List<PsiElement> {
        val fieldAccessExpr = PsiTreeUtil.getParentOfType(element, SqlElStaticFieldAccessExpr::class.java)
        val nodeElm =
            if (fieldAccessExpr != null) {
                PsiTreeUtil
                    .getChildrenOfType(
                        fieldAccessExpr,
                        SqlElIdExpr::class.java,
                    )?.filter { it.textOffset <= element.textOffset }
            } else {
                listOf(element)
            }
        return nodeElm
            ?.toList()
            ?.sortedBy { it.textOffset } ?: emptyList()
    }
}
