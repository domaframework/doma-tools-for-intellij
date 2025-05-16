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

import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.UIUtil.setEnabledRecursively
import org.domaframework.doma.intellij.bundle.MessageBundle
import org.domaframework.doma.intellij.common.helper.ActiveProjectHelper
import org.domaframework.doma.intellij.common.helper.ExpressionFunctionsHelper
import org.domaframework.doma.intellij.extension.getJavaClazz
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class SettingComponent {
    val panel: JPanel?
    private val enableFormatCheckBox = JBCheckBox()

    private val customFunctionClassListModel = javax.swing.DefaultListModel<String>()
    private val customFunctionClassList =
        JBList(customFunctionClassListModel).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            emptyText.text = MessageBundle.message("config.custom.functions.dialog.example")
        }

    init {
        val customFunctionPanel =
            ToolbarDecorator
                .createDecorator(customFunctionClassList)
                .setAddAction { actionButton ->
                    val project = ActiveProjectHelper.activeProject
                    if (project != null) {
                        val expressionFunction = ExpressionFunctionsHelper
                        val expressionFunctionInterface =
                            expressionFunction.setExpressionFunctionsInterface(project)
                        val dialog =
                            com.intellij.openapi.ui.InputValidatorEx { inputString ->
                                if (inputString.isNullOrBlank()) {
                                    MessageBundle.message("config.custom.functions.dialog.error.blank")
                                } else if (expressionFunctionInterface != null) {
                                    val expressionClazz = project.getJavaClazz(inputString)
                                    if (expressionClazz == null ||
                                        !expressionFunction.isInheritor(expressionClazz)
                                    ) {
                                        MessageBundle.message("config.custom.functions.dialog.error.invalidClass")
                                    } else {
                                        null
                                    }
                                } else {
                                    null
                                }
                            }
                        val input =
                            com.intellij.openapi.ui.Messages.showInputDialog(
                                "${MessageBundle.message("config.custom.functions.dialog.message")}\n" +
                                    MessageBundle.message("config.custom.functions.dialog.example"),
                                MessageBundle.message("config.custom.functions.dialog.title"),
                                null,
                                "",
                                dialog,
                            )

                        if (!input.isNullOrBlank()) {
                            customFunctionClassListModel.addElement(input.trim())
                        }
                    }
                }.setRemoveAction { actionButton ->
                    val idx = customFunctionClassList.selectedIndex
                    if (idx >= 0) customFunctionClassListModel.remove(idx)
                }.disableUpDownActions()
                .createPanel()

        // Disable customFunctionPanel if no active project is available
        if (ActiveProjectHelper.activeProject == null) {
            setEnabledRecursively(customFunctionPanel, false)
        }

        this.panel =
            FormBuilder
                .createFormBuilder()
                .addLabeledComponent(
                    JBLabel(MessageBundle.message("config.enable.sql.format")),
                    enableFormatCheckBox,
                    1,
                    false,
                ).addLabeledComponent(
                    JBLabel(MessageBundle.message("config.custom.functions.title")),
                    customFunctionPanel,
                    1,
                    false,
                ).addComponentFillVertically(JPanel(), 0)
                .panel
    }

    var enableFormat: Boolean
        get() = enableFormatCheckBox.isSelected
        set(enabled) {
            enableFormatCheckBox.isSelected = enabled
        }

    var customFunctionClassNames: List<String>
        get() =
            (0 until customFunctionClassListModel.size()).map {
                customFunctionClassListModel.getElementAt(it)
            }
        set(value) {
            customFunctionClassListModel.clear()
            value.forEach { customFunctionClassListModel.addElement(it) }
        }
}
