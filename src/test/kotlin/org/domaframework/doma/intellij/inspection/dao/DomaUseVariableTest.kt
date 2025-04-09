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
import org.domaframework.doma.intellij.inspection.dao.inspector.DaoMethodVariableInspector

/**
 * Test class to verify whether Dao method arguments are used
 */
class DomaUseVariableTest : DomaSqlTest() {
    private val testDaoName = "DaoMethodVariableInspectionTestDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$testDaoName.java",
        )
        addSqlFile(
            "$testDaoName/biFunctionDoesNotCauseError.sql",
            "$testDaoName/selectOptionDoesNotCauseError.sql",
            "$testDaoName/collectDoesNotCauseError.sql",
            "$testDaoName/collectDoesCauseError.sql",
            "$testDaoName/noErrorWhenUsedInFunctionParameters.sql",
        )
        myFixture.enableInspections(DaoMethodVariableInspector())
    }

    /**
     * Test to verify if Dao method arguments are used
     */
    fun testDaoMethodArgumentsUsed() {
        val dao = findDaoClass(testDaoName)
        myFixture.testHighlighting(
            false,
            false,
            false,
            dao.containingFile.virtualFile,
        )
    }
}
