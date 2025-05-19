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

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import org.domaframework.doma.intellij.setting.SettingComponent

@State(
    name = "DomaToolsCustomFunctionSettings",
    storages = [Storage("doma_tools_settings.xml")],
)
@Service(Service.Level.PROJECT)
class DomaToolsCustomFunctionSettings :
    PersistentStateComponent<DomaToolsCustomFunctionSettings.State>,
    DomaToolsSettings {
    class State {
        var customFunctionClassNames: MutableList<String> = mutableListOf()
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    override fun isModified(component: SettingComponent?): Boolean = component?.customFunctionClassNames != state.customFunctionClassNames

    override fun apply(component: SettingComponent?) {
        state.customFunctionClassNames.clear()
        component?.customFunctionClassNames?.let { state.customFunctionClassNames.addAll(it) }
    }

    override fun reset(component: SettingComponent?) {
        component?.let {
            it.customFunctionClassNames = state.customFunctionClassNames
        }
    }

    companion object {
        fun getInstance(project: Project): DomaToolsCustomFunctionSettings = project.getService(DomaToolsCustomFunctionSettings::class.java)
    }
}
