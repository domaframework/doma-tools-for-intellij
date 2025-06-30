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
package org.domaframework.doma.intellij.gutteraction.sql

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.bundle.MessageBundle

/**
 * SQL gutter icon display, action execution test
 */
class SqlGutterActionTest : DomaSqlTest() {
    private val packageName = "gutteraction"

    override fun setUp() {
        super.setUp()

        addDaoJavaFile("$packageName/JumpActionTestDao.java")
        addSqlFile(
            "$packageName/JumpActionTestDao/jumpToDaoFile.sql",
            "$packageName/JumpActionTestDao/notDisplayGutterWithNonExistentDaoMethod.sql",
        )
        addOtherPackageJavaFile("doma/java/dao", "SourceNameDao.java")
        addOtherPackageSqlFile("doma/java/dao", "SourceNameDao/jumpToDaoFile.sql")
    }

    fun testSqlDisplayGutter() {
        val sqlName = "$packageName/JumpActionTestDao/jumpToDaoFile.sql"
        val total = 1
        val targetGutter = gutterIconsDisplayedTest(findSqlFile(sqlName), total)
        gutterIconNavigation("JumpActionTestDao.java", targetGutter)
    }

    fun testSqlNonDisplayGutter() {
        val sqlName = "$packageName/JumpActionTestDao/notDisplayGutterWithNonExistentDaoMethod.sql"
        val total = 0
        val targetGutter = gutterIconsDisplayedTest(findSqlFile(sqlName), total)
        assertNull("Gutter is displayed", targetGutter)
    }

    fun testSourceDirectoryPackageGutter() {
        val packageName = "doma/java"
        val sqlName = "SourceNameDao/jumpToDaoFile.sql"
        val total = 1
        val targetGutter = gutterIconsDisplayedTest(findSqlFile(packageName, sqlName), total)
        gutterIconNavigation("SourceNameDao.java", targetGutter)
    }

    private fun gutterIconsDisplayedTest(
        sql: VirtualFile?,
        total: Int,
    ): LineMarkerInfo<*>? {
        if (sql == null) {
            fail("Not Found SQL File")
            return null
        }

        myFixture.configureFromExistingVirtualFile(sql)
        myFixture.doHighlighting()

        val gutters: List<GutterMark> = myFixture.findAllGutters()
        var found = 0
        var firstGutter: LineMarkerInfo<out PsiElement>? = null

        for (mark in gutters) {
            if (MessageBundle.message("jump.to.dao.tooltip.title") == mark.tooltipText &&
                mark is LineMarkerInfo.LineMarkerGutterIconRenderer<*>
            ) {
                found++
                if (firstGutter == null) firstGutter = mark.lineMarkerInfo
            }
        }
        assertTrue("Expected gutter icon was not found found:$found total:$total", found == total)

        return firstGutter
    }

    private fun gutterIconNavigation(
        daoName: String,
        gutterIcon: LineMarkerInfo<*>?,
    ) {
        assertNotNull("Gutter is null", gutterIcon)
        val targetMarkerInfo: RelatedItemLineMarkerInfo<*> = gutterIcon as RelatedItemLineMarkerInfo<*>

        assertNotNull("Expected RelatedItemLineMarkerInfo was not found", targetMarkerInfo)

        val navHandler = targetMarkerInfo.navigationHandler
        assertNotNull("Navigation handler is null", navHandler)

        val markerElement = targetMarkerInfo.element
        assertNotNull("Marker element is null", markerElement)

        val typedNavHandler =
            navHandler as GutterIconNavigationHandler<*>
        typedNavHandler.navigate(null, null)

        val editor = FileEditorManager.getInstance(project).selectedEditors
        assertTrue("Ope File is Not $daoName", editor.any { it.file.name == daoName })
    }
}
