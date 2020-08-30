/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template.components

import com.android.tools.idea.npw.template.components.ComponentProvider
import com.android.tools.idea.ui.wizard.WizardUtils
import io.flutter.npw.template.Parameter
import javax.swing.JComponent

/**
 * A class responsible for converting a [Parameter] to a [JComponent]. Any parameter
 * that represents a value (most of them, except for e.g. SEPARATOR should
 * be sure to also create an appropriate Swing property to control the component.
 */
abstract class ParameterComponentProvider<T : JComponent> protected constructor(private val parameter: Parameter<*>) : ComponentProvider<T>() {
  override fun createComponent(): T = createComponent(parameter).apply {
    toolTipText = WizardUtils.toHtmlString(parameter.help ?: "")
  }

  protected abstract fun createComponent(parameter: Parameter<*>): T

  fun param() = parameter
}
