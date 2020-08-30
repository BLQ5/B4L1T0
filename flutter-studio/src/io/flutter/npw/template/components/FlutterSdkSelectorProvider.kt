/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template.components

import com.android.tools.idea.observable.ui.TextProperty
import com.intellij.ide.util.newProjectWizard.AbstractProjectWizard
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ui.ComboboxWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.util.ui.UIUtil
import io.flutter.actions.InstallSdkAction
import io.flutter.npw.template.Parameter
import io.flutter.npw.template.StringParameter
import java.awt.LayoutManager
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JTextPane

class FlutterSdkSelectorProvider(parameter: StringParameter) : ParameterComponentProvider<JPanel>(parameter) {
  override fun createComponent(parameter: Parameter<*>): JPanel {
    TODO()
  }

  override fun createProperty(component: JPanel): TextProperty {
    TODO()
  }
}

class SdkSelectorPanel(layout: LayoutManager?) : JPanel(layout) {
  val combobox: ComboboxWithBrowseButton = ComboboxWithBrowseButton()
  val model: InstallSdkActionModel = InstallSdkActionModel(this)
  val action: InstallSdkAction = InstallSdkAction(model)
  val versionContent = JBLabel()
  val errorIcon = JLabel()
  val errorText = JTextPane()
  val errorPane = JBScrollPane(errorText)
  val linkLabel = LinkLabel<Any>()
  val progressBar = JProgressBar()
  val progressText = JTextPane()
  val cancelButton = JLabel()
  lateinit var context: WizardContext
}

class InstallSdkActionModel(val selector : SdkSelectorPanel) : InstallSdkAction.Model {
  lateinit var listener: InstallSdkAction.CancelActionListener

  override fun addCancelActionListener(action: InstallSdkAction.CancelActionListener) {
    listener = action
  }

  override fun getInstallActionLink(): LinkLabel<*> {
    return selector.linkLabel
  }

  override fun setErrorDetails(details: String?) {
    val makeVisible = details != null
    if (makeVisible) {
      selector.errorText.text = details
    }
    selector.errorIcon.isVisible = makeVisible
    selector.errorPane.isVisible = makeVisible
  }

  override fun getSdkComboBox(): ComboboxWithBrowseButton {
    return selector.combobox
  }

  override fun requestNextStep() {
    val wizard = selector.context.getWizard() as AbstractProjectWizard
    if (wizard != null) {
      UIUtil.invokeAndWaitIfNeeded((Runnable { wizard.doNextAction() } as Runnable)!!)
    }
  }

  override fun validate(): Boolean {
    TODO("Not yet implemented")
  }

  override fun getCancelProgressButton(): JLabel {
    TODO("Not yet implemented")
  }

  override fun setSdkPath(path: String?) {
    TODO("Not yet implemented")
  }

  override fun getProgressText(): JTextPane {
    TODO("Not yet implemented")
  }

  override fun getProgressBar(): JProgressBar {
    TODO("Not yet implemented")
  }
}