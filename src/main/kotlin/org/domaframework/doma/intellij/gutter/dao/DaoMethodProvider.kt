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
package org.domaframework.doma.intellij.gutter.dao

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiNameIdentifierOwner
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.PluginLoggerUtil
import org.domaframework.doma.intellij.common.dao.getDaoClass
import org.domaframework.doma.intellij.common.dao.jumpSqlFromDao
import org.domaframework.doma.intellij.common.isJavaOrKotlinFileType
import org.domaframework.doma.intellij.common.psi.PsiDaoMethod
import org.domaframework.doma.intellij.setting.SqlIcon
import java.awt.event.MouseEvent
import javax.swing.Icon

/**
 * Line marker provider for DAO method
 */
class DaoMethodProvider : RelatedItemLineMarkerProvider() {
    override fun getIcon(): Icon = SqlIcon.FILE

    override fun collectNavigationMarkers(
        e: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
    ) {
        if (!isTargetElement(e)) return

        if (e.parent is PsiNameIdentifierOwner) {
            val owner = e as PsiNameIdentifierOwner
            val method = e as? PsiMethod ?: return
            val identifier = owner.nameIdentifier ?: return
            val psiDaoMethod = PsiDaoMethod(e.project, method)
            if (!psiDaoMethod.isUseSqlFileMethod()) return
            val target = psiDaoMethod.sqlFile ?: return

            val marker =
                RelatedItemLineMarkerInfo(
                    identifier,
                    identifier.textRange,
                    icon,
                    { MessageBundle.message("jump.to.sql.tooltip.title") },
                    getHandler(e.project, target),
                    GutterIconRenderer.Alignment.RIGHT,
                ) {
                    ArrayList<GotoRelatedItem>()
                }
            result.add(marker)
        }
    }

    private fun isTargetElement(e: PsiElement): Boolean {
        if (!isJavaOrKotlinFileType(e.containingFile) && !isFunction(e)) {
            return false
        }
        if (getDaoClass(e.containingFile) == null) return false
        return e is PsiMethod
    }

    /**
     * whether the element is a function
     * @return true if it is a function element
     */
    private fun isFunction(element: PsiElement): Boolean = element is PsiMethod

    private fun getHandler(
        project: Project,
        file: VirtualFile,
    ): GutterIconNavigationHandler<PsiElement> =
        GutterIconNavigationHandler { _: MouseEvent?, _: PsiElement? ->
            val startTime = System.nanoTime()
            jumpSqlFromDao(project, file)
            PluginLoggerUtil.countLogging(
                this::class.java.simpleName,
                "JumpToSqlByGutter",
                "Gutter",
                startTime,
            )
        }
}
