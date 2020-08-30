/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

interface ParameterBuilder<T> {
  var name: String?
  var help: String?
  var visible: WizardParameterData.() -> Boolean
  var enabled: WizardParameterData.() -> Boolean
  var default: T?

  fun build(): Parameter<T>

  fun validate() {
    checkNotNull(name) { "Parameter must have a name." }
    checkNotNull(default as Any?) { "Parameter must have a default value." }
  }
}

fun stringParameter(block: StringParameterBuilder.() -> Unit): StringParameter = StringParameterBuilder().apply(block).build()

fun booleanParameter(block: BooleanParameterBuilder.() -> Unit): BooleanParameter = BooleanParameterBuilder().apply(block).build()

@TemplateDsl
data class BooleanParameterBuilder(
  override var name: String? = null,
  override var help: String? = null,
  override var visible: WizardParameterData.() -> Boolean = { true },
  override var enabled: WizardParameterData.() -> Boolean = { true },
  override var default: Boolean? = null
) : ParameterBuilder<Boolean> {
  override fun build(): BooleanParameter {
    validate()
    return BooleanParameter(name!!, help, visible, enabled, default!!)
  }
}

@TemplateDsl
data class StringParameterBuilder(
  override var name: String? = null,
  override var help: String? = null,
  override var visible: WizardParameterData.() -> Boolean = { true },
  override var enabled: WizardParameterData.() -> Boolean = { true },
  override var default: String? = null,
  var constraints: List<Constraint> = listOf(),
  var suggest: WizardParameterData.() -> String? = { null }
) : ParameterBuilder<String> {
  override fun build(): StringParameter {
    validate()
    return StringParameter(name!!, help, visible, enabled, default!!, constraints, suggest)
  }
}
