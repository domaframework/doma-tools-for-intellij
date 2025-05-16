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

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import org.domaframework.doma.intellij.setting.SettingComponent

@Service(Service.Level.PROJECT)
@State(
    name = "DomaToolsFormatEnableSettings",
    reloadable = true,
    storages = [Storage("doma_tools_settings.xml")],
)
class DomaToolsFormatEnableSettings :
    PersistentStateComponent<DomaToolsFormatEnableSettings.State>,
    DomaToolsSettings {
    class State : BaseState() {
        var isEnableSqlFormat = false
    }

    var myState: State = State()

    override fun getState(): State = myState

    override fun loadState(state: DomaToolsFormatEnableSettings.State) {
        myState = state
    }

    override fun isModified(component: SettingComponent?): Boolean = myState.isEnableSqlFormat != component?.enableFormat

    override fun apply(component: SettingComponent?) {
        state.isEnableSqlFormat = component?.enableFormat == true
    }

    override fun reset(component: SettingComponent?) {
        component?.enableFormat = state.isEnableSqlFormat
    }

    companion object {
        fun getInstance(project: Project): DomaToolsFormatEnableSettings =
            project.getService(
                DomaToolsFormatEnableSettings::class.java,
            )
    }
}
