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
package org.domaframework.doma.intellij.setting.state

import org.domaframework.doma.intellij.DomaSqlTest
import org.domaframework.doma.intellij.setting.SettingComponent

class DomaToolsCustomFunctionSettingsTest : DomaSqlTest() {
    private val packageName = "doma.example.expression"

    override fun setUp() {
        super.setUp()
    }

    fun testCustomFunctionClassNamesPersistence() {
        val settings = DomaToolsCustomFunctionSettings.getInstance(project)
        val testClassNames =
            mutableListOf(
                "$packageName.TestExpressionFunctions",
                "$packageName.TestNotExpressionFunctions",
            )
        settings.getState().customFunctionClassNames = testClassNames.toMutableList()

        val loaded = DomaToolsCustomFunctionSettings.State()
        loaded.customFunctionClassNames = testClassNames.toMutableList()
        settings.loadState(loaded)

        assertEquals(testClassNames, settings.getState().customFunctionClassNames)
    }

    fun testApplyAndReset() {
        val settings = DomaToolsCustomFunctionSettings.getInstance(project)
        val component = SettingComponent()
        val initialClassNames = mutableListOf("$packageName.TestExpressionFunctions")
        val newClassNames = mutableListOf("$packageName.TestExpressionFunctions", "$packageName.TestNotExpressionFunctions")

        // init
        settings.getState().customFunctionClassNames = initialClassNames.toMutableList()
        settings.reset(component)
        assertEquals(initialClassNames, component.customFunctionClassNames)

        // apply
        component.customFunctionClassNames = newClassNames.toMutableList()
        settings.apply(component)
        assertEquals(newClassNames, settings.getState().customFunctionClassNames)

        // reset
        settings.getState().customFunctionClassNames = initialClassNames.toMutableList()
        settings.reset(component)
        assertEquals(initialClassNames, component.customFunctionClassNames)
    }

    fun testIsModified() {
        val settings = DomaToolsCustomFunctionSettings.getInstance(project)
        val component = SettingComponent()
        val classNames = mutableListOf("$packageName.TestExpressionFunctions")
        settings.getState().customFunctionClassNames = classNames.toMutableList()
        component.customFunctionClassNames = classNames.toMutableList()
        assertFalse(settings.isModified(component))

        val addClassNames = mutableListOf("$packageName.TestExpressionFunctions", "$packageName.TestNotExpressionFunctions")
        component.customFunctionClassNames = addClassNames
        assertTrue(settings.isModified(component))
    }
}
