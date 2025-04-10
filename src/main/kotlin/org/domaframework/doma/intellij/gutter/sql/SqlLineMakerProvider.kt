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
package org.domaframework.doma.intellij.gutter.sql

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.findDaoFile
import org.domaframework.doma.intellij.common.dao.findDaoMethod
import org.domaframework.doma.intellij.common.dao.jumpToDaoMethod
import org.domaframework.doma.intellij.common.isInjectionSqlFile
import org.domaframework.doma.intellij.common.isSupportFileType
import org.jetbrains.kotlin.idea.core.util.toPsiFile
import java.awt.event.MouseEvent
import javax.swing.Icon

/**
 * Line marker provider for SQL file
 */
class SqlLineMakerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        e: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        val project = e.project
        val file = e.containingFile ?: return
        if (!isSupportFileType(file) || isInjectionSqlFile(file)) return
        // Display only on the first line
        if (e.originalElement?.parent?.originalElement !is PsiFile ||
            e.textRange.startOffset != file.textRange.startOffset
        ) {
            return
        }

        val identifier = e.firstChild ?: e
        val daoFile =
            findDaoFile(project, file)?.let {
                if (findDaoMethod(e.containingFile) == null) return
                it
            } ?: return

        val marker =
            RelatedItemLineMarkerInfo(
                identifier,
                identifier.textRange,
                getIcon(daoFile.toPsiFile(project)),
                getToolTipTitle(daoFile.toPsiFile(project)),
                getHandler(daoFile, identifier, file.virtualFile.nameWithoutExtension),
                GutterIconRenderer.Alignment.RIGHT,
            ) {
                ArrayList<GotoRelatedItem>()
            }
        result.add(marker)
    }

    private fun getToolTipTitle(targetSqlFile: PsiFile?): ((Any) -> String) =
        if (targetSqlFile != null) {
            { MessageBundle.message("jump.to.dao.tooltip.title") }
        } else {
            { "" }
        }

    private fun getIcon(targetSqlFile: PsiFile?): Icon {
        if (targetSqlFile != null) {
            return AllIcons.FileTypes.Java
        }
        return AllIcons.General.Error
    }

    private fun getHandler(
        targetDaoFile: VirtualFile?,
        element: PsiElement,
        methodName: String,
    ): GutterIconNavigationHandler<PsiElement> {
        if (targetDaoFile != null) {
            return GutterIconNavigationHandler { _: MouseEvent?, _: PsiElement? ->
                val startTime = System.nanoTime()
                jumpToDaoMethod(element.project, methodName, targetDaoFile)
                PluginLoggerUtil.countLogging(
                    this::class.java.simpleName,
                    "JumpToDaoMethodByGutter",
                    "Gutter",
                    startTime,
                )
            }
        }
        return GutterIconNavigationHandler { _: MouseEvent?, _: PsiElement? -> }
    }
}
