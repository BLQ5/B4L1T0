/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template.components

import com.android.tools.idea.observable.AbstractProperty
import com.android.tools.idea.observable.ui.TextProperty
import io.flutter.npw.template.Parameter
import javax.swing.JTextField

/**
 * Provides a textfield well suited for handling [StringParameter].
 */
class TextFieldProvider(parameter: Parameter<String>) : ParameterComponentProvider<JTextField>(parameter) {
  override fun createComponent(parameter: Parameter<*>): JTextField = JTextField()
  override fun createProperty(component: JTextField): AbstractProperty<*>? = TextProperty(component)
}
