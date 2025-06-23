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
        addOtherJavaFile("collector", "HogeCollector.java")
        myFixture.enableInspections(DaoMethodReturnTypeInspection())
    }

    fun testSelectReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.SelectReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testUpdateAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.UpdateReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testBatchAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.BatchReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testMultiInsertAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.MultiInsertReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testSqlProcessorAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.SqlProcessorReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testProcedureAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.ProcedureReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testFunctionAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.FunctionReturnTypeTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }
}
