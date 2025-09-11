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
package org.domaframework.doma.intellij.inspection.sql

import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlBindVariableInspection
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlFunctionCallInspection
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlLoopDirectiveTypeInspection

/**
 * A test that inspects whether a bind variable's parameters are defined.
 */
class MethodParameterDefinedTest : DomaSqlTest() {
    private val testDaoName = "inspection/FunctionCallValidationTestDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$testDaoName.java",
        )

        myFixture.enableInspections(
            SqlBindVariableInspection(),
            SqlLoopDirectiveTypeInspection(),
            SqlFunctionCallInspection(),
        )
    }

    fun testValidParameterValidation() {
        addSqlFile("$testDaoName/testValidParameter.sql")
        addDomaCompileConfig()
        val sqlFile = findSqlFile("$testDaoName/testValidParameter.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testInValidParameterCountValidation() {
        addSqlFile("$testDaoName/testInvalidParameterCount.sql")
        addDomaCompileConfig()
        val sqlFile = findSqlFile("$testDaoName/testInvalidParameterCount.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testInValidParameterTypeMatchValidation() {
        addSqlFile("$testDaoName/testInvalidParameterTypes.sql")
        addDomaCompileConfig()
        val sqlFile = findSqlFile("$testDaoName/testInvalidParameterTypes.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }
}
