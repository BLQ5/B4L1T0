/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template.components

import com.android.tools.idea.npw.template.components.ComponentProvider
import javax.swing.JSeparator
import javax.swing.SwingConstants

/**
 * Provides a separator.
 */
class SeparatorProvider : ComponentProvider<JSeparator>() {
  override fun createComponent(): JSeparator = JSeparator(SwingConstants.HORIZONTAL)
}
