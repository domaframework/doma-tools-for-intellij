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
package org.domaframework.doma.intellij.gutteraction.dao

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.GutterMark
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.setting.SqlIcon

/**
 * Dao gutter icon display, action execution test
 */
class DaoGutterActionTest : DomaSqlTest() {
    private val packageName = "gutteraction"

    override fun setUp() {
        super.setUp()

        addDaoJavaFile(
            "$packageName/SelectGutterTestDao.java",
            "$packageName/InsertGutterTestDao.java",
            "$packageName/UpdateGutterTestDao.java",
            "$packageName/DeleteGutterTestDao.java",
            "$packageName/BatchInsertGutterTestDao.java",
            "$packageName/BatchUpdateGutterTestDao.java",
            "$packageName/BatchDeleteGutterTestDao.java",
            "$packageName/ScriptGutterTestDao.java",
            "$packageName/SqlProcessorGutterTestDao.java",
        )
        addResourceEmptySqlFile(
            "$packageName/SelectGutterTestDao/existsSQLFile1.sql",
            "$packageName/InsertGutterTestDao/existsSQLFile1.sql",
            "$packageName/UpdateGutterTestDao/existsSQLFile1.sql",
            "$packageName/DeleteGutterTestDao/existsSQLFile1.sql",
            "$packageName/BatchInsertGutterTestDao/existsSQLFile1.sql",
            "$packageName/BatchUpdateGutterTestDao/existsSQLFile1.sql",
            "$packageName/BatchDeleteGutterTestDao/existsSQLFile1.sql",
            "$packageName/ScriptGutterTestDao/existsSQLFile1.script",
            "$packageName/SqlProcessorGutterTestDao/existsSQLFile1.sql",
        )
        addResourceEmptySqlFile(
            "$packageName/SelectGutterTestDao/existsSQLFile2.sql",
            "$packageName/InsertGutterTestDao/existsSQLFile2.sql",
            "$packageName/UpdateGutterTestDao/existsSQLFile2.sql",
            "$packageName/DeleteGutterTestDao/existsSQLFile2.sql",
            "$packageName/BatchInsertGutterTestDao/existsSQLFile2.sql",
            "$packageName/BatchUpdateGutterTestDao/existsSQLFile2.sql",
            "$packageName/BatchDeleteGutterTestDao/existsSQLFile2.sql",
            "$packageName/ScriptGutterTestDao/existsSQLFile2.script",
            "$packageName/SqlProcessorGutterTestDao/existsSQLFile2.sql",
        )
        addResourceEmptySqlFile(
            "$packageName/BatchInsertGutterTestDao/existsSQLFile3.sql",
            "$packageName/BatchUpdateGutterTestDao/existsSQLFile3.sql",
            "$packageName/BatchDeleteGutterTestDao/existsSQLFile3.sql",
        )
    }

    fun testSelectDisplayGutter() {
        val daoName = "$packageName.SelectGutterTestDao"
        val total = 2
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    fun testInsertDisplayGutter() {
        val daoName = "$packageName.InsertGutterTestDao"
        val total = 2
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    fun testUpdateDisplayGutter() {
        val daoName = "$packageName.UpdateGutterTestDao"
        val total = 2
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    fun testDeleteDisplayGutter() {
        val daoName = "$packageName.DeleteGutterTestDao"
        val total = 1
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    fun testBatchInsertDisplayGutter() {
        val daoName = "$packageName.BatchInsertGutterTestDao"
        val total = 3
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    fun testBatchUpdateDisplayGutter() {
        val daoName = "$packageName.BatchUpdateGutterTestDao"
        val total = 3
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    fun testBatchDeleteDisplayGutter() {
        val daoName = "$packageName.BatchDeleteGutterTestDao"
        val total = 2
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    fun testScriptDisplayGutter() {
        val daoName = "$packageName.ScriptGutterTestDao"
        val total = 2
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.script", targetGutter)
    }

    fun testSqlProcessorDisplayGutter() {
        val daoName = "$packageName.SqlProcessorGutterTestDao"
        val total = 2
        val targetGutter = gutterIconsDisplayedTest(daoName, total)
        gutterIconNavigation("existsSQLFile1.sql", targetGutter)
    }

    private fun gutterIconsDisplayedTest(
        daoName: String,
        total: Int,
    ): LineMarkerInfo<*>? {
        val dao = findDaoClass(daoName)
        val targetElementNames = listOf("existsSQLFile1", "existsSQLFile2", "existsSQLFile3")

        myFixture.configureFromExistingVirtualFile(dao.containingFile.virtualFile)
        myFixture.doHighlighting()

        val gutters: List<GutterMark> = myFixture.findAllGutters()
        var found = 0
        var firstGutter: LineMarkerInfo<out PsiElement>? = null

        for (mark in gutters) {
            if (MessageBundle.message("jump.to.sql.tooltip.title") == mark.tooltipText &&
                mark is LineMarkerInfo.LineMarkerGutterIconRenderer<*>
            ) {
                val markTargetName = mark.lineMarkerInfo.element?.text
                assertTrue(
                    "Unexpected gutter icon found $markTargetName",
                    targetElementNames.contains(markTargetName),
                )

                found++
                assertEquals(
                    SqlIcon.FILE,
                    mark.icon,
                )
                if (firstGutter == null) firstGutter = mark.lineMarkerInfo
            }
        }
        assertTrue("Expected gutter icon was not found found:$found total:$total", found == total)

        return firstGutter
    }

    private fun gutterIconNavigation(
        sqlName: String,
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
        assertTrue("Ope File is Not $sqlName", editor.any { it.file.name == sqlName })
    }
}
