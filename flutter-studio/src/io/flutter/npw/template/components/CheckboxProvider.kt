/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template.components

import com.android.tools.idea.observable.AbstractProperty
import com.android.tools.idea.observable.ui.SelectedProperty
import io.flutter.npw.template.BooleanParameter
import io.flutter.npw.template.Parameter
import javax.swing.JCheckBox

/**
 * Provides a checkbox well suited for handling [BooleanParameter].
 */
class CheckboxProvider(parameter: BooleanParameter) : ParameterComponentProvider<JCheckBox>(parameter) {
  override fun createComponent(parameter: Parameter<*>): JCheckBox = JCheckBox(parameter.name)
  override fun createProperty(component: JCheckBox): AbstractProperty<*>? = SelectedProperty(component)
}
