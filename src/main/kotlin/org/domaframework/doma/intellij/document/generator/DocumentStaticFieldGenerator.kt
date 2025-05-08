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
package org.domaframework.doma.intellij.document.generator

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.common.psi.PsiParentClass
import org.domaframework.doma.intellij.common.psi.PsiStaticElement
import org.domaframework.doma.intellij.common.util.ForDirectiveUtil
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.extension.psi.psiClassType
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElStaticFieldAccessExpr

class DocumentStaticFieldGenerator(
    val originalElement: PsiElement,
    val project: Project,
    val result: MutableList<String?>,
    val staticFieldAccessExpr: SqlElStaticFieldAccessExpr,
    val file: PsiFile,
) : DocumentGenerator() {
    override fun generateDocument() {
        val fieldAccessBlocks = staticFieldAccessExpr.accessElements
        val staticElement = PsiStaticElement(fieldAccessBlocks, file)
        val referenceClass = staticElement.getRefClazz() ?: return
        if (PsiTreeUtil.getParentOfType(originalElement, SqlElClass::class.java) != null) {
            val clazzType = PsiParentClass(referenceClass.psiClassType)
            result.add("${generateTypeLink(clazzType)} ${originalElement.text}")
            return
        }

        ForDirectiveUtil.getFieldAccessLastPropertyClassType(
            fieldAccessBlocks.filter { it.textOffset <= originalElement.textOffset },
            project,
            PsiParentClass(referenceClass.psiClassType),
            complete = { lastType ->
                result.add("${generateTypeLink(lastType)} ${originalElement.text}")
            },
        )
    }
}
