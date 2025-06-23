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
import org.domaframework.doma.intellij.inspection.dao.inspector.DaoMethodParamTypeInspection

/**
 * Test class for annotation parameter type check inspection.
 */
class AnnotationParamTypeCheckInspectionTest : DomaSqlTest() {
    private val testDaoNames =
        listOf(
            "InsertUpdateDeleteParamTestDao",
            "BatchInsertUpdateDeleteParamTestDao",
            "MultiInsertParamTestDao",
            "ProcedureParamTestDao",
            "ScriptParamTestDao",
            "SqlProcessorParamTestDao",
            "SelectParamTestDao",
        )
    private val daoPackage = "inspection/paramtype"

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(DaoMethodParamTypeInspection())
        addEntityJavaFile("Pckt.java")
        addEntityJavaFile("Packet.java")
        addOtherJavaFile("collector", "HogeCollector.java")
        addOtherJavaFile("function", "HogeFunction.java")
        testDaoNames.forEach { daoName ->
            addDaoJavaFile("$daoPackage/$daoName.java")
        }
    }

    fun testSelectParam() {
        val dao = findDaoClass("$daoPackage.SelectParamTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testInsertUpdateDeleteParam() {
        val dao = findDaoClass("$daoPackage.InsertUpdateDeleteParamTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testBatchInsertUpdateDeleteParam() {
        val dao = findDaoClass("$daoPackage.BatchInsertUpdateDeleteParamTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testMultiInsertParam() {
        val dao = findDaoClass("$daoPackage.MultiInsertParamTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testProcedureParam() {
        val dao = findDaoClass("$daoPackage.ProcedureParamTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testScriptParam() {
        val dao = findDaoClass("$daoPackage.ScriptParamTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }

    fun testSqlProcessorParam() {
        val dao = findDaoClass("$daoPackage.SqlProcessorParamTestDao")
        myFixture.testHighlighting(false, false, false, dao.containingFile.virtualFile)
    }
}
