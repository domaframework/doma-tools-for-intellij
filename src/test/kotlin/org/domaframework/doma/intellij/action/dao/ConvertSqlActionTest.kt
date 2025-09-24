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

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import org.domaframework.doma.intellij.DomaSqlTest
import kotlin.reflect.KClass

abstract class ConvertSqlActionTest : DomaSqlTest() {
    protected fun doConvertAction(
        daoName: String,
        convertFamilyName: String,
        sqlConversionPackage: String,
        convertActionName: String,
    ) {
        addDaoJavaFile("$sqlConversionPackage/$daoName.java")

        val daoClass = findDaoClass("$sqlConversionPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)
        val intention = myFixture.findSingleIntention(convertActionName)

        assertNotNull(
            "$convertActionName intention should be available",
            intention,
        )
        assertEquals(convertActionName, intention.text)
        assertEquals(convertFamilyName, intention.familyName)

        myFixture.launchAction(intention)
        myFixture.checkResultByFile("java/doma/example/dao/$sqlConversionPackage/$daoName.after.java")
    }

    protected fun <T : AbstractConvertSqlFileToAnnotationAction> doConvertActionTest(
        daoName: String,
        sqlToAnnotationPackage: String,
        convertFamilyName: String,
        convertActionClass: KClass<T>,
    ) {
        addDaoJavaFile("$sqlToAnnotationPackage/$daoName.java")
        val daoClass = findDaoClass("$sqlToAnnotationPackage.$daoName")
        myFixture.configureFromExistingVirtualFile(daoClass.containingFile.virtualFile)

        val intentions = myFixture.availableIntentions
        val convertIntention = intentions.find { convertActionClass.java.isInstance(it) }

        assertNull("$convertFamilyName intention should NOT be available without @Sql annotation", convertIntention)
    }

    protected fun doTestSqlFormat(
        daoName: String,
        sqlFileName: String,
        sqlConversionPackage: String,
        isScript: Boolean = false,
    ) {
        val openedEditor = FileEditorManager.getInstance(project).selectedEditors
        val extension = if (isScript) "script" else "sql"
        val openSqlFile = openedEditor.find { it.file.name == sqlFileName.substringAfter("/").plus(".$extension") }

        if (openSqlFile != null) {
            fail("SQL file $sqlFileName.$extension should be opened after conversion")
            return
        }
        // If the generated `PsiFile` has an associated `Document`, explicitly reload it to ensure memory–disk consistency.
        // If not reloaded, the test may produce: *Unexpected memory–disk conflict in tests for*.
        val fdm = FileDocumentManager.getInstance()
        fdm.saveAllDocuments()
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val newSqlFile = findSqlFile("$sqlConversionPackage/$daoName/$sqlFileName.$extension")
        if (newSqlFile == null) {
            fail("Not Found $sqlFileName.$extension")
            return
        }
        fdm.getDocument(newSqlFile)?.let { fdm.reloadFromDisk(it) }
        myFixture.configureFromExistingVirtualFile(newSqlFile)
        myFixture.checkResultByFile("resources/META-INF/doma/example/dao/$sqlConversionPackage/$daoName/$sqlFileName.after.$extension")
    }
}
