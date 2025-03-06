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
package org.domaframework.doma.intellij.extension.psi

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * For processing inside Sql annotations, get it as an injected custom language
 */
fun PsiFile.initPsiFileAndElement(
    project: Project,
    caretOffset: Int,
): PsiFile? {
    val injectedLanguageManager = InjectedLanguageManager.getInstance(project)
    val element =
        injectedLanguageManager.findInjectedElementAt(this, caretOffset) ?: return null
    return element.containingFile
}
