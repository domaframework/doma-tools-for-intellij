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
import com.intellij.util.ui.UIUtil.setEnabledRecursively
import org.domaframework.doma.intellij.common.helper.ActiveProjectHelper
import org.domaframework.doma.intellij.setting.state.DomaToolsFormatEnableSettings
import javax.swing.JComponent

class DomaToolsConfigurable : Configurable {
    private var mySettingsComponent: SettingComponent? = SettingComponent()

    private var formatSettings: DomaToolsFormatEnableSettings? = null

    override fun getDisplayName(): String = "Doma Tools"

    override fun createComponent(): JComponent? {
        val project = ActiveProjectHelper.getCurrentActiveProject()
        val panel = mySettingsComponent?.panel ?: return null
        if (project == null) {
            setEnabledRecursively(panel, false)
            return null
        }
        formatSettings = DomaToolsFormatEnableSettings.getInstance(project)
        return panel
    }

    override fun isModified(): Boolean {
        val enableFormatModified = formatSettings?.isModified(mySettingsComponent) != false
        return enableFormatModified
    }

    @Throws(ConfigurationException::class)
    override fun apply() {
        formatSettings?.apply(mySettingsComponent)
    }

    override fun reset() {
        formatSettings?.reset(mySettingsComponent)
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}
