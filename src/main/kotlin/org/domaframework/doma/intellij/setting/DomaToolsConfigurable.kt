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
package org.domaframework.doma.intellij.setting

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import org.domaframework.doma.intellij.common.helper.ActiveProjectHelper
import org.domaframework.doma.intellij.setting.state.DomaToolsCustomFunctionSettings
import org.domaframework.doma.intellij.setting.state.DomaToolsFormatEnableSettings
import javax.swing.JComponent

class DomaToolsConfigurable : Configurable {
    private var mySettingsComponent: SettingComponent? = SettingComponent()
    private val project = ActiveProjectHelper.activeProject

    private var formatSettings: DomaToolsFormatEnableSettings? = null
    private var customFunctionsSettings: DomaToolsCustomFunctionSettings? = null

    init {
        project?.let {
            customFunctionsSettings = DomaToolsCustomFunctionSettings.getInstance(it)
            formatSettings = DomaToolsFormatEnableSettings.getInstance(it)
        }
    }

    override fun getDisplayName(): String = "Doma Tools"

    override fun createComponent(): JComponent? = mySettingsComponent?.panel

    override fun isModified(): Boolean {
        val enableFormatModified = formatSettings?.isModified(mySettingsComponent) != false
        val customFunctionClassNamesModified =
            customFunctionsSettings?.isModified(mySettingsComponent) != false

        return enableFormatModified || customFunctionClassNamesModified
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        formatSettings?.apply(mySettingsComponent)
        customFunctionsSettings?.apply(mySettingsComponent)
    }

    override fun reset() {
        formatSettings?.reset(mySettingsComponent)
        customFunctionsSettings?.reset(mySettingsComponent)
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
