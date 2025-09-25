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
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.psi.SqlCustomElExpr
import org.domaframework.doma.intellij.psi.SqlElClass
import org.domaframework.doma.intellij.psi.SqlElIdExpr
import org.domaframework.doma.intellij.psi.SqlTypes
import org.jetbrains.kotlin.idea.core.util.toPsiFile

class SqlReferenceTestCase : DomaSqlTest() {
    private val testPackage = "reference"
    private val testDaoName = "ReferenceTestDao"

    private val forItemResolve = "SqlElIdExprImpl(EL_ID_EXPR)"
    private val forItemFieldAccessResolve = "SqlElFieldAccessExprImpl(EL_FIELD_ACCESS_EXPR)"
    private val daoParameterResolve = "PsiParameter"
    private val fieldResolve = "PsiField"
    private val methodResolve = "PsiMethod"
    private val classResolve = "PsiClass"
    private val psiTypeResolve = "PsiType"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile("$testPackage/$testDaoName.java")
    }

    fun testReferenceDaoMethodParameter() {
        val sqlFileName = "referenceDaoParameter"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTest(
            sqlFileName,
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
        val sqlFileName = "referenceEntityProperty"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTest(
            sqlFileName,
            mapOf(
                "detail" to listOf("$daoParameterResolve:detail"),
                "getFirstEmployee" to listOf("$methodResolve:getFirstEmployee"),
                "getCustomNumber" to listOf("$methodResolve:getCustomNumber"),
                "projectCategory" to listOf("$fieldResolve:projectCategory"),
                "projectNumber" to listOf("$fieldResolve:projectNumber"),
                "projects" to listOf("$daoParameterResolve:projects"),
                "project" to listOf(forItemResolve),
                "id" to listOf(null),
                "null" to listOf(null),
                "employeeId" to listOf("$fieldResolve:employeeId"),
                "isNotBlank" to listOf("$methodResolve:isNotBlank"),
            ),
        )
    }

    fun testReferenceStaticField() {
        val sqlFileName = "referenceStaticField"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTest(
            sqlFileName,
            mapOf(
                "doma.example.entity.ProjectDetail" to listOf("$classResolve:ProjectDetail"),
                "doma.example.entity.Project" to listOf("$classResolve:Project"),
                "projectCategory" to listOf("$fieldResolve:projectCategory"),
                "getTermNumber" to listOf("$methodResolve:getTermNumber"),
                "getFirstEmployee" to listOf("$methodResolve:getFirstEmployee"),
                "getCustomNumber" to listOf("$methodResolve:getCustomNumber"),
                "detail" to listOf("$daoParameterResolve:detail"),
                "employeeId" to listOf("$fieldResolve:employeeId"),
                "toString" to listOf("$methodResolve:toString"),
            ),
        )
    }

    fun testReferenceListFieldMethod() {
        val sqlFileName = "referenceListFieldMethod"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTest(
            sqlFileName,
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
        val sqlFileName = "referenceForItem"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTest(
            sqlFileName,
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

    fun testReferenceCustomFunction() {
        addDomaCompileConfig()
        val sqlFileName = "referenceCustomFunction"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTest(
            sqlFileName,
            mapOf(
                "detail" to listOf("$daoParameterResolve:detail"),
                "projectDetailId" to listOf("$fieldResolve:projectDetailId"),
                "userId" to listOf("$methodResolve:userId"),
            ),
        )
    }

    fun testReferenceMethodParameter() {
        addDomaCompileConfig()
        val sqlFileName = "referenceMethodParameter"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTest(
            sqlFileName,
            mapOf(
                "doma.example.entity.Project" to listOf("$classResolve:Project"),
                "getEmployee" to listOf("$methodResolve:getEmployee"),
                "employee" to listOf("$daoParameterResolve:employee"),
                "project" to listOf("$daoParameterResolve:project"),
                "managerId" to listOf("$fieldResolve:managerId"),
                "employeeParam" to listOf("$methodResolve:employeeParam"),
                "employeeName" to listOf("$fieldResolve:employeeName"),
                "processText" to listOf("$methodResolve:processText"),
                "getSubEmployee" to listOf("$methodResolve:getSubEmployee"),
                "managerId" to listOf("$fieldResolve:managerId"),
                "cost" to listOf("$fieldResolve:cost"),
                "projectId" to listOf("$fieldResolve:projectId"),
                "optionalIds" to listOf("$fieldResolve:optionalIds"),
                "getProjectNumber" to listOf("$methodResolve:getProjectNumber"),
                "get" to listOf("$methodResolve:get"),
                "0" to listOf(null),
                "formatName" to listOf("$methodResolve:formatName"),
                "str" to listOf("$daoParameterResolve:str"),
                "intValue" to listOf("$daoParameterResolve:intValue"),
                "floatValue" to listOf("$daoParameterResolve:floatValue"),
                "\"suffix\"" to listOf(null),
                "\"test\"" to listOf(null),
                "charSeq" to listOf("$daoParameterResolve:charSeq"),
                "toString" to listOf("$methodResolve:toString"),
                "subProject" to listOf("$daoParameterResolve:subProject"),
                "number" to listOf("$fieldResolve:number"),
                "roundUpTimePart" to listOf("$methodResolve:roundUpTimePart"),
                "localDate" to listOf("$daoParameterResolve:localDate"),
                "localDateTime" to listOf("$daoParameterResolve:localDateTime"),
                "suffix" to listOf("$methodResolve:suffix"),
                "isGuest" to listOf("$methodResolve:isGuest"),
                "isGuestInProject" to listOf("$methodResolve:isGuestInProject"),
                "columns" to listOf("$daoParameterResolve:columns"),
                "item" to listOf(forItemResolve),
                "params" to listOf("$methodResolve:params"),
                "currentYear" to listOf("$methodResolve:currentYear"),
                "item_has_next" to listOf(forItemResolve),
            ),
        )
    }

    /**
     * Test reference resolution for overloaded instance methods.
     */
    fun testDocumentOverloadInstanceMethod1() {
        val sqlFileName = "documentOverloadInstanceMethod1"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "employee" to listOf("$daoParameterResolve:employee"),
                "employeeParam" to listOf("$psiTypeResolve:String, $psiTypeResolve:Integer"),
                "employeeName" to listOf("$fieldResolve:employeeName"),
                "managerId" to listOf("$fieldResolve:managerId"),
            ),
        )
    }

    fun testDocumentOverloadInstanceMethod2() {
        val sqlFileName = "documentOverloadInstanceMethod2"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "employee" to listOf("$daoParameterResolve:employee"),
                "employeeParam" to listOf("$psiTypeResolve:int, $psiTypeResolve:Float"),
                "employeeName" to listOf("$fieldResolve:employeeName"),
                "managerId" to listOf("$fieldResolve:managerId"),
                "floatVal" to listOf("$daoParameterResolve:floatVal"),
            ),
        )
    }

    fun testDocumentOverloadStaticMethod1() {
        val sqlFileName = "documentOverloadStaticMethod1"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "doma.example.entity.Project" to listOf("$classResolve:Project"),
                "getEmployee" to listOf("$psiTypeResolve:int"),
                "employee" to listOf("$daoParameterResolve:employee"),
                "managerId" to listOf("$fieldResolve:managerId"),
            ),
        )
    }

    fun testDocumentOverloadStaticMethod2() {
        val sqlFileName = "documentOverloadStaticMethod2"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "doma.example.entity.Project" to listOf("$classResolve:Project"),
                "getEmployee" to listOf("$psiTypeResolve:Employee"),
                "employee" to listOf("$daoParameterResolve:employee"),
            ),
        )
    }

    fun testDocumentOverloadCustomFunction1() {
        addDomaCompileConfig()
        val sqlFileName = "documentOverloadCustomFunction1"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "isGuest" to listOf("$psiTypeResolve:Employee"),
                "employee" to listOf("$daoParameterResolve:employee"),
            ),
        )
    }

    fun testDocumentOverloadCustomFunction2() {
        addDomaCompileConfig()
        val sqlFileName = "documentOverloadCustomFunction2"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "isGuest" to listOf("$psiTypeResolve:Project"),
                "project" to listOf("$daoParameterResolve:project"),
            ),
        )
    }

    fun testDocumentOverloadBuiltInFunction1() {
        val sqlFileName = "documentOverloadBuiltInFunction1"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "roundDownTimePart" to listOf("$psiTypeResolve:Date"),
                "date" to listOf("$daoParameterResolve:date"),
            ),
        )
    }

    fun testDocumentOverloadBuiltInFunction2() {
        val sqlFileName = "documentOverloadBuiltInFunction2"
        addSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        referenceTestDocument(
            sqlFileName,
            mapOf(
                "roundDownTimePart" to listOf("$psiTypeResolve:LocalDateTime"),
                "localDateTime" to listOf("$daoParameterResolve:localDateTime"),
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
            assertTrue(expectedResults?.contains(resolveResult?.toString()) == true)
        }
    }

    /**
     * Test whether the referenced method matches, using the documentation generated by reference resolution.
     */
    private fun referenceTestDocument(
        sqlFileName: String,
        resolveExpects: Map<String, List<String?>>,
    ) {
        val sqlFile = findSqlFile("$testPackage/$testDaoName/$sqlFileName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.configureFromExistingVirtualFile(sqlFile)
        sqlFile.toPsiFile(project)?.let {
            resolveDocumentInTestFile(
                it,
                resolveExpects,
            )
        }
    }

    private fun resolveDocumentInTestFile(
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
            val resolveResultText =
                (resolveResult as? PsiMethod)
                    ?.parameterList
                    ?.parameters
                    ?.map { it.type }
                    ?.joinToString()
                    ?: resolveResult.toString()
            val expectedResults = resolveExpects[reference.text]
            println("Reference: ${reference.text}, Resolve: $resolveResultText Expects: $expectedResults")
            assertTrue(expectedResults?.contains(resolveResultText) == true)
        }
    }

    private fun isLiteral(element: PsiElement): Boolean =
        element.elementType == SqlTypes.EL_STRING ||
            element.elementType == SqlTypes.EL_CHAR ||
            element.elementType == SqlTypes.EL_NUMBER ||
            element.elementType == SqlTypes.EL_NULL ||
            element.elementType == SqlTypes.BOOLEAN
}
