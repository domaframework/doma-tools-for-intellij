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
package org.domaframework.doma.intellij.document

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.extension.expr.accessElements
import org.domaframework.doma.intellij.psi.SqlBlockComment
import org.domaframework.doma.intellij.psi.SqlElFieldAccessExpr
import org.domaframework.doma.intellij.psi.SqlElForDirective
import org.domaframework.doma.intellij.psi.SqlElIdExpr

class SqlSymbolDocumentTestCase : DomaSqlTest() {
    val testPackage = "document"
    val testDaoName = "DocumentTestDao"
    val myDocumentationProvider: ForItemElementDocumentationProvider = ForItemElementDocumentationProvider()

    override fun setUp() {
        super.setUp()
        addDaoJavaFile("$testPackage/$testDaoName.java")
    }

    fun testDocumentForItemDaoParam() {
        addSqlFile("$testPackage/$testDaoName/documentForItemDaoParam.sql")
        val sqlName = "documentForItemDaoParam"
        val result: String? = null

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemDeclaration() {
        addSqlFile("$testPackage/$testDaoName/documentForItemDeclaration.sql")
        val sqlName = "documentForItemDeclaration"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://java.util.List\">" +
                "List</a><<a href=\"psi_element://java.lang.Integer\">Integer</a>>> employeeIds"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemOptionalForItem() {
        addSqlFile("$testPackage/$testDaoName/documentForItemOptionalForItem.sql")
        val sqlName = "documentForItemOptionalForItem"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://doma.example.entity.Project\">Project</a>> optionalProjects"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemOptionalForItemProperty() {
        addSqlFile("$testPackage/$testDaoName/documentForItemOptionalProperty.sql")
        val sqlName = "documentForItemOptionalProperty"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://java.lang.Integer\">Integer</a>> optionalIds"

        documentationFindTextTest(sqlName, "optionalIds", result)
    }

    fun testDocumentForItemElement() {
        addSqlFile("$testPackage/$testDaoName/documentForItemElement.sql")
        val sqlName = "documentForItemElement"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://java.util.List\">" +
                "List</a><<a href=\"psi_element://java.lang.Integer\">Integer</a>>> employeeIds"

        documentationFindTextTest(sqlName, "employeeIds", result)
    }

    fun testDocumentForItemElementInBindVariable() {
        addSqlFile("$testPackage/$testDaoName/documentForItemElementInBindVariable.sql")
        val sqlName = "documentForItemElementInBindVariable"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><" +
                "<a href=\"psi_element://java.lang.Integer\">Integer</a>> ids"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemElementInIfDirective() {
        addSqlFile("$testPackage/$testDaoName/documentForItemElementInIfDirective.sql")
        val sqlName = "documentForItemElementInIfDirective"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><" +
                "<a href=\"psi_element://java.lang.Integer\">Integer</a>> ids"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemElementByFieldAccess() {
        addSqlFile("$testPackage/$testDaoName/documentForItemElementByFieldAccess.sql")
        val sqlName = "documentForItemElementByFieldAccess"
        val result =
            "<a href=\"psi_element://doma.example.entity.Project\">Project</a> project"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemFirstElement() {
        addSqlFile("$testPackage/$testDaoName/documentForItemFirstElement.sql")
        val sqlName = "documentForItemFirstElement"
        val result =
            "<a href=\"psi_element://doma.example.entity.Principal.Permission\">Permission</a> item"

        documentationFindTextTest(sqlName, "item", result)
    }

    fun testDocumentForItemHasNext() {
        addSqlFile("$testPackage/$testDaoName/documentForItemHasNext.sql")
        val sqlName = "documentForItemHasNext"
        val result =
            "<a href=\"psi_element://boolean\">boolean</a> item_has_next"

        documentationFindTextTest(sqlName, "item_has_next", result)
    }

    fun testDocumentForItemIndex() {
        addSqlFile("$testPackage/$testDaoName/documentForItemIndex.sql")
        val sqlName = "documentForItemIndex"
        val result =
            "<a href=\"psi_element://int\">int</a> item_index"

        documentationFindTextTest(sqlName, "item_index", result)
    }

    fun testDocumentForItemStaticProperty() {
        addSqlFile("$testPackage/$testDaoName/documentForItemStaticProperty.sql")
        val sqlName = "documentForItemStaticProperty"
        val result =
            "<a href=\"psi_element://doma.example.entity.Project\">Project</a> project"

        documentationFindTextTest(sqlName, "project", result)
    }

    fun testDocumentForItemInvalidPrimary() {
        addSqlFile("$testPackage/$testDaoName/documentForItemInvalidPrimary.sql")
        val sqlName = "documentForItemInvalidPrimary"
        val result = "<a href=\"psi_element://doma.example.entity.Principal\">Principal</a> item"

        documentationFindTextTest(sqlName, "item", result)
    }

    private fun documentationTest(
        sqlName: String,
        result: String?,
    ) {
        val sqlFile = findSqlFile("$testPackage/$testDaoName/$sqlName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        val originalElement: PsiElement = myFixture.elementAtCaret
        val resultDocument = myDocumentationProvider.generateDoc(originalElement, originalElement)
        assertEquals("Documentation should contain expected text", result, resultDocument)
    }

    private fun documentationFindTextTest(
        sqlName: String,
        originalElementName: String,
        result: String?,
    ) {
        val sqlFile = findSqlFile("$testPackage/$testDaoName/$sqlName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        val originalElement: PsiElement? =
            myFixture.findElementByText(originalElementName, SqlElIdExpr::class.java)
                ?: fundForDirectiveDeclarationElement(sqlFile, originalElementName)
        assertNotNull("Not Found Element [$originalElementName]", originalElement)
        if (originalElement == null) return

        val resultDocument = myDocumentationProvider.generateDoc(originalElement, originalElement)
        assertEquals("Documentation should contain expected text", result, resultDocument)
    }

    private fun fundForDirectiveDeclarationElement(
        sqlFile: VirtualFile,
        searchElementName: String,
    ): PsiElement? {
        myFixture.configureFromExistingVirtualFile(sqlFile)
        val topElement = myFixture.findElementByText(searchElementName, PsiElement::class.java)
        val forDirectiveBlock =
            topElement.children
                .firstOrNull { it is SqlBlockComment && it.text.contains(searchElementName) }

        val forDirective =
            forDirectiveBlock?.children?.find { it is SqlElForDirective } as? SqlElForDirective
                ?: return null
        val fieldAccessExpr =
            forDirective.elExprList[1] as? SqlElFieldAccessExpr
                ?: return forDirective.elExprList.firstOrNull { it.text == searchElementName }

        return fieldAccessExpr.accessElements.firstOrNull { it?.text == searchElementName }
    }
}
