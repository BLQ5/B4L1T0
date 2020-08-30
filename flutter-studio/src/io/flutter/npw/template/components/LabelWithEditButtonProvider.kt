/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template.components

import com.android.tools.adtui.LabelWithEditButton
import com.android.tools.idea.observable.AbstractProperty
import com.android.tools.idea.observable.ui.TextProperty
import io.flutter.npw.template.Parameter
import io.flutter.npw.template.StringParameter

/**
 * Provides a [LabelWithEditButton] for more advanced [StringParameter] that only users who know what they're doing should modify.
 */
class LabelWithEditButtonProvider(parameter: StringParameter) : ParameterComponentProvider<LabelWithEditButton>(parameter) {
  override fun createComponent(parameter: Parameter<*>): LabelWithEditButton = LabelWithEditButton()
  override fun createProperty(component: LabelWithEditButton): AbstractProperty<*> = TextProperty(component).also {
    it.set((param() as StringParameter).value)
  }
}
