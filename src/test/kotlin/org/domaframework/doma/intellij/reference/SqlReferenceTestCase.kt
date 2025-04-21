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

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.psi.SqlCustomElExpr
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class SqlReferenceTestCase : DomaSqlTest() {
    val testPackage = "reference"
    val testDaoName = "ReferenceTestDao"

    val forItemResolve = "SqlElIdExprImpl(EL_ID_EXPR)"
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
    }

    fun testReferenceDaoMethodParameter() {
        referenceTest(
            "referenceDaoParameter",
            mapOf(
                "reportId" to "$daoParameterResolve:reportId",
                "tableName" to "$daoParameterResolve:tableName",
                "columns" to "$daoParameterResolve:columns",
                "column" to forItemResolve,
                "notExistParam" to null,
            ),
        )
    }

    fun testReferenceEntityProperty() {
        referenceTest(
            "referenceEntityProperty",
            mapOf(
                "detail" to "$daoParameterResolve:detail",
                "getFirstEmployee" to "$methodResolve:getFirstEmployee",
                "projectNumber" to "$fieldResolve:projectNumber",
                "projects" to "$daoParameterResolve:projects",
                "project" to forItemResolve,
                "id" to forItemResolve,
                "employeeId" to "$fieldResolve:employeeId",
            ),
        )
    }

    fun testReferenceStaticField() {
        referenceTest(
            "referenceStaticField",
            mapOf(
                "doma.example.entity.ProjectDetail" to "$classResolve:ProjectDetail",
                "doma.example.entity.Project" to "$classResolve:Project",
                "projectCategory" to "$fieldResolve:projectCategory",
                "getTermNumber" to "$methodResolve:getTermNumber",
                "getFirstEmployee" to "$methodResolve:getFirstEmployee",
                "employeeId" to "$fieldResolve:employeeId",
            ),
        )
    }

    private fun referenceTest(
        sqlFileName: String,
        resolveExpects: Map<String, String?>,
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
     * resolveExpects: A map of expected results for each reference. Type:resolveName
     */
    private fun resolveReferencesInTestFile(
        sqlFile: PsiFile,
        resolveExpects: Map<String, String?>,
    ) {
        val references = PsiTreeUtil.collectElementsOfType(sqlFile, SqlCustomElExpr::class.java)
        for (reference in references) {
            val resolveResult = reference.references.firstOrNull()?.resolve()
            val result = resolveExpects[reference.text]
            assertEquals(result, resolveResult?.toString())
        }
    }
}
