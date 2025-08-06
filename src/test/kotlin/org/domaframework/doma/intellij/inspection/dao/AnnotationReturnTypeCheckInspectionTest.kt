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
package org.domaframework.doma.intellij.inspection.dao

import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.inspection.dao.inspector.DaoMethodReturnTypeInspection

/**
 * Test class for annotation return type check inspection.
 */
class AnnotationReturnTypeCheckInspectionTest : DomaSqlTest() {
    private val testDaoNames =
        listOf(
            "SelectReturnTypeTestDao",
            "UpdateReturnTypeTestDao",
            "BatchReturnTypeTestDao",
            "MultiInsertReturnTypeTestDao",
            "SqlProcessorReturnTypeTestDao",
            "ProcedureReturnTypeTestDao",
            "FunctionReturnTypeTestDao",
            "FactoryReturnTypeTestDao",
            "DataTypeReturnTypeTestDao",
        )
    private val daoPackage = "inspection/returntype"

    override fun setUp() {
        super.setUp()
        testDaoNames.forEach { daoName ->
            addDaoJavaFile("$daoPackage/$daoName.java")
        }
        addEntityJavaFile("Packet.java")
        addEntityJavaFile("Pckt.java")
        addOtherJavaFile("domain", "Hiredate.java")
        addOtherJavaFile("domain", "Salary.java")
        addOtherJavaFile("collector", "HogeCollector.java")
        addOtherJavaFile("function", "HogeFunction.java")
        addOtherJavaFile("function", "HogeBiFunction.java")
        myFixture.enableInspections(DaoMethodReturnTypeInspection())
    }

    fun testSelectReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.SelectReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testUpdateReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.UpdateReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testBatchReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.BatchReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testMultiInsertReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.MultiInsertReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testSqlProcessorReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.SqlProcessorReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testProcedureReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.ProcedureReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testFunctionReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.FunctionReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testFactoryReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.FactoryReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testDataTypeReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.DataTypeReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }
}
