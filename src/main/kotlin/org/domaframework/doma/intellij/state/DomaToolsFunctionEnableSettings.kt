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
package org.domaframework.doma.intellij.state

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.APP)
@State(
    name = "org.domaframework.doma",
    reloadable = true,
    storages = [Storage("DomaTools.xml")],
)
class DomaToolsFunctionEnableSettings : PersistentStateComponent<DomaToolsFunctionEnableSettings.State> {
    class State : BaseState() {
        var isEnableSqlFormat = false
    }

    var myState: State = State()

    override fun getState(): State = myState

    override fun loadState(state: DomaToolsFunctionEnableSettings.State) {
        myState = state
    }

    companion object {
        fun getInstance(): DomaToolsFunctionEnableSettings =
            ApplicationManager.getApplication().getService(DomaToolsFunctionEnableSettings::class.java)
    }
}
