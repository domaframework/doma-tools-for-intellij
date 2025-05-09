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
import org.domaframework.doma.intellij.inspection.sql.inspector.SqlBindVariableValidInspector

/**
 * A test that inspects whether a bind variable's parameters are defined.
 */
class ParameterDefinedTest : DomaSqlTest() {
    private val testDaoName = "EmployeeSummaryDao"

    override fun setUp() {
        super.setUp()
        addDaoJavaFile(
            "$testDaoName.java",
        )
        addSqlFile(
            "$testDaoName/bindVariableForEntityAndNonEntityParentClass.sql",
            "$testDaoName/bindVariableForNonEntityClass.sql",
            "$testDaoName/accessStaticProperty.sql",
            "$testDaoName/batchAnnotationResolvesClassInList.sql",
            "$testDaoName/resolveDaoArgumentOfListType.sql",
            "$testDaoName/bindVariableInFunctionParameters.sql",
            "$testDaoName/callStaticPropertyPackageName.sql",
            "$testDaoName/bindVariableForItemHasNextAndIndex.sql",
            "$testDaoName/optionalDaoParameterFieldAccess.sql",
        )
        myFixture.enableInspections(SqlBindVariableValidInspector())
    }

    /**
     * Entity class instance field, method reference test
     * + Non-Entity parent class field, method reference test
     */
    fun testBindVariableForEntityAndNonEntityParentClass() {
        val sqlFile = findSqlFile("$testDaoName/bindVariableForEntityAndNonEntityParentClass.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableForNonEntityClass() {
        val sqlFile = findSqlFile("$testDaoName/bindVariableForNonEntityClass.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableForItemHasNextAndIndex() {
        val sqlFile = findSqlFile("$testDaoName/bindVariableForItemHasNextAndIndex.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testOptionalDaoParameterFieldAccess() {
        val sqlFile =
            findSqlFile("$testDaoName/optionalDaoParameterFieldAccess.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testAccessStaticProperty() {
        val sqlFile = findSqlFile("$testDaoName/accessStaticProperty.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testCallStaticPropertyPackageName() {
        val sqlFile = findSqlFile("$testDaoName/callStaticPropertyPackageName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBatchAnnotationResolvesClassInList() {
        val sqlFile =
            findSqlFile("$testDaoName/batchAnnotationResolvesClassInList.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testResolveDaoArgumentOfListType() {
        val sqlFile =
            findSqlFile("$testDaoName/resolveDaoArgumentOfListType.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableInFunctionParameters() {
        val sqlFile =
            findSqlFile("$testDaoName/bindVariableInFunctionParameters.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }
}
