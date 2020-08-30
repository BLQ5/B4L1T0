/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

/**
 * Data used for rendering template UI, provided by the wizards
 *
 * Useful when rendering UI according to templates (e.g. visibility may depend on [isNewModule]).
 * It is not passed to the template renderer, in contrast to [TemplateData].
 *
 * Note: it updates [Parameter.wizardParameterData] for all [consumers].
 */
data class WizardParameterData(
  val packageName: String,
  val isNewModule: Boolean,
  private val consumers: Collection<Parameter<*>>
) {
  init {
    consumers.forEach { it.wizardParameterData = this }
  }
}
