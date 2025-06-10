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
import org.domaframework.doma.intellij.inspection.dao.inspector.DaoMethodReturnTypeVariableInspection

/**
 * Test class for annotation return type check inspection.
 */
class AnnotationReturnTypeCheckInspectionTest : DomaSqlTest() {
    private val testDaoNames =
        listOf(
            "UpdateReturnTypeDao",
            "BatchReturnTypeDao",
            "MultiInsertReturnTypeDao",
            "SqlProcessorReturnTypeDao",
            "ProcedureReturnTypeDao",
        )
    private val daoPackage = "inspection"

    override fun setUp() {
        super.setUp()
        testDaoNames.forEach { daoName ->
            addDaoJavaFile("inspection/$daoName.java")
        }
        // Entity classes
        addEntityJavaFile("Packet.java")
        addEntityJavaFile("Pckt.java")
        myFixture.enableInspections(DaoMethodReturnTypeVariableInspection())
    }

    fun testUpdateAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.UpdateReturnTypeDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testBatchAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.BatchReturnTypeDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testMultiInsertAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.MultiInsertReturnTypeDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testSqlProcessorAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.SqlProcessorReturnTypeDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testProcedureAnnotationReturnTypeCheckProcessor() {
        val dao = findDaoClass("$daoPackage.ProcedureReturnTypeDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }
}
