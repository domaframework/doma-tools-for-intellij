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
package org.domaframework.doma.intellij.parsar

import com.intellij.testFramework.ParsingTestCase
import org.domaframework.doma.intellij.setting.SqlParserDefinition

/**
 * SQL parser test processing as custom language
 */
class DomaParsingTestCase : ParsingTestCase("", "sql", SqlParserDefinition()) {
    fun testSQLParser() {
        doTest(true)
    }

    override fun getTestDataPath(): String = "src/test/testData/sql/parser"

    override fun includeRanges(): Boolean = true
}
