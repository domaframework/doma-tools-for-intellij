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

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import org.domaframework.doma.intellij.bundle.MessageBundle
import javax.swing.JPanel

class SettingComponent {
    val panel: JPanel?
    private val enableFormatCheckBox = JBCheckBox()

    init {
        this.panel =
            FormBuilder
                .createFormBuilder()
                .addLabeledComponent(JBLabel(MessageBundle.message("config.enable.sql.format")), enableFormatCheckBox, 1, false)
                .addComponentFillVertically(JPanel(), 0)
                .panel
    }

    var enableFormat: Boolean
        get() = enableFormatCheckBox.isSelected
        set(enabled) {
            enableFormatCheckBox.isSelected = enabled
        }
}
