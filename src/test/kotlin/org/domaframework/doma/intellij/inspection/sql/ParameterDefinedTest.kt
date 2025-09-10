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
class ParameterDefinedTest : DomaSqlTest() {
    private val testDaoName = "inspection/ParamDefinedTestDao"

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

    /**
     * Entity class instance field, method reference test
     * + Non-Entity parent class field, method reference test
     */
    fun testBindVariableForEntityAndNonEntityParentClass() {
        addSqlFile("$testDaoName/bindVariableForEntityAndNonEntityParentClass.sql")
        val sqlFile = findSqlFile("$testDaoName/bindVariableForEntityAndNonEntityParentClass.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableForNonEntityClass() {
        addSqlFile("$testDaoName/bindVariableForNonEntityClass.sql")
        val sqlFile = findSqlFile("$testDaoName/bindVariableForNonEntityClass.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableForItemHasNextAndIndex() {
        addSqlFile("$testDaoName/bindVariableForItemHasNextAndIndex.sql")
        val sqlFile = findSqlFile("$testDaoName/bindVariableForItemHasNextAndIndex.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testOptionalDaoParameterFieldAccess() {
        addSqlFile("$testDaoName/optionalDaoParameterFieldAccess.sql")
        val sqlFile =
            findSqlFile("$testDaoName/optionalDaoParameterFieldAccess.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testAccessStaticProperty() {
        addSqlFile("$testDaoName/accessStaticProperty.sql")
        val sqlFile = findSqlFile("$testDaoName/accessStaticProperty.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testCallStaticPropertyPackageName() {
        addSqlFile("$testDaoName/callStaticPropertyPackageName.sql")
        val sqlFile = findSqlFile("$testDaoName/callStaticPropertyPackageName.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBatchAnnotationResolvesClassInList() {
        addSqlFile("$testDaoName/batchAnnotationResolvesClassInList.sql")
        val sqlFile =
            findSqlFile("$testDaoName/batchAnnotationResolvesClassInList.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testResolveDaoArgumentOfListType() {
        addSqlFile("$testDaoName/resolveDaoArgumentOfListType.sql")
        val sqlFile =
            findSqlFile("$testDaoName/resolveDaoArgumentOfListType.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testBindVariableInFunctionParameters() {
        addSqlFile("$testDaoName/bindVariableInFunctionParameters.sql")
        val sqlFile =
            findSqlFile("$testDaoName/bindVariableInFunctionParameters.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testImplementCustomFunctions() {
        addSqlFile("$testDaoName/implementCustomFunctions.sql")
        addDomaCompileConfig()
        val sqlFile =
            findSqlFile("$testDaoName/implementCustomFunctions.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testInvalidImplementCustomFunctions() {
        addSqlFile("$testDaoName/invalidImplementCustomFunctions.sql")
        addResourceCompileFile("invalid.doma.compile.config")
        val sqlFile =
            findSqlFile("$testDaoName/invalidImplementCustomFunctions.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testEmptyPropertyCustomFunctions() {
        addSqlFile("$testDaoName/emptyPropertyCustomFunctions.sql")
        addResourceCompileFile("empty.property.doma.compile.config")
        val sqlFile =
            findSqlFile("$testDaoName/emptyPropertyCustomFunctions.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }

    fun testEmptyImplementCustomFunctions() {
        addSqlFile("$testDaoName/emptyImplementCustomFunctions.sql")
        addResourceCompileFile("empty.doma.compile.config")
        val sqlFile =
            findSqlFile("$testDaoName/emptyImplementCustomFunctions.sql")
        assertNotNull("Not Found SQL File", sqlFile)
        if (sqlFile == null) return

        myFixture.testHighlighting(false, false, false, sqlFile)
    }
}
