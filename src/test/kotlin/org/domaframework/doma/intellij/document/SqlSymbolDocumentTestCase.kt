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
        addSqlFile("$testPackage/$testDaoName/documentForItemDaoParam.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemDeclaration.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemElement.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemElementInBindVariable.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemElementInIfDirective.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemElementByFieldAccess.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemFirstElement.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemStaticProperty.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemHasNext.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemIndex.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemOptionalForItem.sql")
        addSqlFile("$testPackage/$testDaoName/documentForItemOptionalProperty.sql")
    }

    fun testDocumentForItemDaoParam() {
        val sqlName = "documentForItemDaoParam"
        val result: String? = null

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemDeclaration() {
        val sqlName = "documentForItemDeclaration"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://java.util.List\">" +
                "List</a><<a href=\"psi_element://java.lang.Integer\">Integer</a>>> employeeIds"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemOptionalForItem() {
        val sqlName = "documentForItemOptionalForItem"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://doma.example.entity.Project\">Project</a>> optionalProjects"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemOptionalForItemProperty() {
        val sqlName = "documentForItemOptionalProperty"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://java.lang.Integer\">Integer</a>> optionalIds"

        documentationFindTextTest(sqlName, "optionalIds", result)
    }

    fun testDocumentForItemElement() {
        val sqlName = "documentForItemElement"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><<a href=\"psi_element://java.util.List\">" +
                "List</a><<a href=\"psi_element://java.lang.Integer\">Integer</a>>> employeeIds"

        documentationFindTextTest(sqlName, "employeeIds", result)
    }

    fun testDocumentForItemElementInBindVariable() {
        val sqlName = "documentForItemElementInBindVariable"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><" +
                "<a href=\"psi_element://java.lang.Integer\">Integer</a>> ids"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemElementInIfDirective() {
        val sqlName = "documentForItemElementInIfDirective"
        val result =
            "<a href=\"psi_element://java.util.List\">List</a><" +
                "<a href=\"psi_element://java.lang.Integer\">Integer</a>> ids"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemElementByFieldAccess() {
        val sqlName = "documentForItemElementByFieldAccess"
        val result =
            "<a href=\"psi_element://doma.example.entity.Project\">Project</a> project"

        documentationTest(sqlName, result)
    }

    fun testDocumentForItemFirstElement() {
        val sqlName = "documentForItemFirstElement"
        val result =
            "<a href=\"psi_element://doma.example.entity.Principal.Permission\">Permission</a> item"

        documentationFindTextTest(sqlName, "item", result)
    }

    fun testDocumentForItemHasNext() {
        val sqlName = "documentForItemHasNext"
        val result =
            "<a href=\"psi_element://java.lang.Boolean\">Boolean</a> item_has_next"

        documentationFindTextTest(sqlName, "item_has_next", result)
    }

    fun testDocumentForItemIndex() {
        val sqlName = "documentForItemIndex"
        val result =
            "<a href=\"psi_element://java.lang.Integer\">Integer</a> item_index"

        documentationFindTextTest(sqlName, "item_index", result)
    }

    fun testDocumentForItemStaticProperty() {
        val sqlName = "documentForItemStaticProperty"
        val result =
            "<a href=\"psi_element://doma.example.entity.Project\">Project</a> project"

        documentationFindTextTest(sqlName, "project", result)
    }

    private fun documentationTest(
        sqlName: String,
        result: String?,
    ) {
        val sqlFile = findSqlFile("$testPackage/$testDaoName/$sqlName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        var originalElement: PsiElement = myFixture.elementAtCaret
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
        var originalElement: PsiElement? =
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
        val fieldAccessExpr = forDirective.elExprList[1] as? SqlElFieldAccessExpr
        if (fieldAccessExpr == null) {
            return forDirective.elExprList.firstOrNull { it.text == searchElementName }
        }

        return fieldAccessExpr.elExprList.firstOrNull { it.text == searchElementName }
    }
}
