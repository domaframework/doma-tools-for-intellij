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
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.psi.SqlCustomElExpr
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class SqlReferenceTestCase : DomaSqlTest() {
    val testPackage = "reference"
    val testDaoName = "ReferenceTestDao"

    val forItemResolve = "SqlElIdExprImpl(EL_ID_EXPR)"
    val forItemFieldAccessResolve = "SqlElFieldAccessExprImpl(EL_FIELD_ACCESS_EXPR)"
    val daoParameterResolve = "PsiParameter"
    val fieldResolve = "PsiField"
    val methodResolve = "PsiMethod"
    val classResolve = "PsiClass"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile("$testPackage/$testDaoName.java")
        addSqlFile("$testPackage/$testDaoName/referenceDaoParameter.sql")
        addSqlFile("$testPackage/$testDaoName/referenceEntityProperty.sql")
        addSqlFile("$testPackage/$testDaoName/referenceStaticField.sql")
        addSqlFile("$testPackage/$testDaoName/referenceListFieldMethod.sql")
        addSqlFile("$testPackage/$testDaoName/referenceForItem.sql")
    }

    fun testReferenceDaoMethodParameter() {
        referenceTest(
            "referenceDaoParameter",
            mapOf(
                "reportId" to listOf("$daoParameterResolve:reportId"),
                "tableName" to listOf("$daoParameterResolve:tableName"),
                "columns" to listOf("$daoParameterResolve:columns"),
                "column" to listOf(forItemResolve),
                "notExistParam" to listOf(null),
            ),
        )
    }

    fun testReferenceEntityProperty() {
        referenceTest(
            "referenceEntityProperty",
            mapOf(
                "detail" to listOf("$daoParameterResolve:detail"),
                "getFirstEmployee" to listOf("$methodResolve:getFirstEmployee"),
                "projectNumber" to listOf("$fieldResolve:projectNumber"),
                "projects" to listOf("$daoParameterResolve:projects"),
                "project" to listOf(forItemResolve),
                "id" to listOf(null),
                "employeeId" to listOf("$fieldResolve:employeeId"),
            ),
        )
    }

    fun testReferenceStaticField() {
        referenceTest(
            "referenceStaticField",
            mapOf(
                "doma.example.entity.ProjectDetail" to listOf("$classResolve:ProjectDetail"),
                "doma.example.entity.Project" to listOf("$classResolve:Project"),
                "projectCategory" to listOf("$fieldResolve:projectCategory"),
                "getTermNumber" to listOf("$methodResolve:getTermNumber"),
                "getFirstEmployee" to listOf("$methodResolve:getFirstEmployee"),
                "employeeId" to listOf("$fieldResolve:employeeId"),
            ),
        )
    }

    fun testReferenceListFieldMethod() {
        referenceTest(
            "referenceListFieldMethod",
            mapOf(
                "doma.example.entity.Employee" to listOf("$classResolve:Employee"),
                "projects" to listOf("$fieldResolve:projects"),
                "get" to listOf("$methodResolve:get"),
                "employeesList" to listOf("$daoParameterResolve:employeesList"),
                "projectCategory" to listOf("$fieldResolve:projectCategory"),
                "projectNumber" to listOf("$fieldResolve:projectNumber"),
                "0" to listOf(null),
            ),
        )
    }

    fun testReferenceForItem() {
        referenceTest(
            "referenceForItem",
            mapOf(
                "employeesList" to listOf("$daoParameterResolve:employeesList"),
                "projects" to listOf("$fieldResolve:projects"),
                "get" to listOf("$methodResolve:get"),
                "employees" to listOf(forItemResolve),
                "employees_has_next" to listOf(forItemResolve),
                "employee_has_next" to listOf(forItemResolve),
                "employee" to listOf(forItemResolve),
                "project" to listOf(forItemResolve, forItemFieldAccessResolve),
                "projectCategory" to listOf("$fieldResolve:projectCategory"),
                "projectNumber" to listOf("$fieldResolve:projectNumber"),
                "projectId" to listOf("$fieldResolve:projectId"),
                "0" to listOf(null),
                "\"OR\"" to listOf(null),
                "\"AND\"" to listOf(null),
            ),
        )
    }

    private fun referenceTest(
        sqlFileName: String,
        resolveExpects: Map<String, List<String?>>,
    ) {
        val sqlFile = findSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        sqlFile.toPsiFile(project)?.let {
            resolveReferencesInTestFile(
                it,
                resolveExpects,
            )
        }
    }

    /**
     * Resolve references in the test file.
     * sqlFile: The SQL file to resolve references in.
     * resolveExpects: A map of expected results for each reference. Type: List of resolveName
     */
    private fun resolveReferencesInTestFile(
        sqlFile: PsiFile,
        resolveExpects: Map<String, List<String?>>,
    ) {
        val references =
            PsiTreeUtil.collectElementsOfType(sqlFile, SqlCustomElExpr::class.java).filter {
                !isLiteral(it) &&
                    !(
                        it is SqlElIdExpr &&
                            PsiTreeUtil.getParentOfType(
                                it,
                                SqlElClass::class.java,
                            ) != null
                    )
            }
        for (reference in references) {
            val resolveResult = reference.references.firstOrNull()?.resolve()
            val expectedResults = resolveExpects[reference.text]

            println(
                "Reference: ${reference.text}, Resolve StaticClassPackageSearchResult: ${resolveResult?.toString()}, Expected Results: $expectedResults",
            )
            assertTrue(expectedResults?.contains(resolveResult?.toString()) == true)
        }
    }

    private fun isLiteral(element: PsiElement): Boolean =
        element.elementType == SqlTypes.EL_STRING ||
            element.elementType == SqlTypes.EL_CHAR ||
            element.elementType == SqlTypes.EL_NUMBER ||
            element.elementType == SqlTypes.EL_NULL ||
            element.elementType == SqlTypes.BOOLEAN
}
