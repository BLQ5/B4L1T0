/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

/**
 * Constraints that can be applied to a parameter which helps the UI add a validator etc for user input.
 * These are typically combined into a set of constraints via an EnumSet.
 */
enum class Constraint {
  /**
   * This value must be unique. This constraint usually only makes sense when other constraints are specified, such as [LAYOUT],
   * which means that the parameter should designate a name that does not represent an existing layout resource name.
   */
  UNIQUE,
  /**
   * This value must already exist. This constraint usually only makes sense when other constraints are specified, such as [LAYOUT],
   * which means that the parameter should designate a name that already exists as a resource name.
   */
  EXISTS,
  /** The associated value must not be empty. */
  NONEMPTY,
  /** The associated value should represent a valid class name. */
  CLASS,
  /** The associated value should represent a valid package name. */
  PACKAGE,
  /** The associated value should represent a valid module name. */
  MODULE,
  /** The associated value should represent a valid string resource name. */
  STRING,
  /** The associated value should represent a valid path to a Flutter SDK. */
  SDK,
  /** The associated value should represent a valid Dart package name. */
  PROJECT;
}

/**
 * This is a parameter which is a part of [Template].
 *
 * Each parameter will be rendered to its own field when rendering UI from [Template], albeit possibly disabled or hidden.
 * A user should provide [value]s to all parameters via interacting with UI.
 * Later this data is passed to the [Recipe] and used to render actual template files.
 */
sealed class Parameter<T> {
  /** Name of the parameter. Should be unique. */
  abstract val name: String
  open val help: String? = null
  abstract val defaultValue: T
  abstract var value: T
  // should be updated only by [Parameters]
  internal lateinit var wizardParameterData: WizardParameterData

  /**
   * Tells if the [Parameter] should be shown in UI.
   *
   * We do not show parameters which are not visible in UI, but use them (fill with data and send to the [Recipe]).
   * @see enabled
   */
  val isVisibleAndEnabled: Boolean
    get() = enabled && visible

  /**
   * Returns false if the [Parameter] should be completely ignored (sometimes will be rendered in gray in UI).
   */
  abstract val enabled: Boolean
  abstract val visible: Boolean
}

/**
 * A wrapper which overrides some [Parameter] methods with given lambdas.
 *
 * Actual parameters should inherit this, not [Parameter].
 */
sealed class DslParameter<T>(
  private val _visible: WizardParameterData.() -> Boolean = { true },
  private val _enabled: WizardParameterData.() -> Boolean = { true }
): Parameter<T>() {
  override val enabled get() = wizardParameterData._enabled()
  override val visible get()= wizardParameterData._visible()
}

/**
 * String parameter. Rendered as a text field in UI.
 */
data class StringParameter(
  override val name: String,
  override val help: String? = null,
  private val _visible: WizardParameterData.() -> Boolean = { true },
  private val _enabled: WizardParameterData.() -> Boolean = { true },
  override val defaultValue: String,
  val constraints: List<Constraint>,
  private val _suggest: WizardParameterData.() -> String? = { null }
) : DslParameter<String>(_visible, _enabled) {
  override var value: String = defaultValue

  /**
   * Value suggested by the Studio. If it was evaluated to null, then [defaultValue] is used.
   * Often calculated using different parameters, e.g "activity_super" layout name generated from "SuperActivity".
   */
  fun suggest() = wizardParameterData._suggest()
}

/**
 * Boolean parameter. Rendered as a checkbox in UI.
 */
data class BooleanParameter(
  override val name: String,
  override val help: String? = null,
  private val _visible: WizardParameterData.() -> Boolean = { true },
  private val _enabled: WizardParameterData.() -> Boolean = { true },
  override val defaultValue: Boolean
) : DslParameter<Boolean>(_visible, _enabled) {
  override var value: Boolean = defaultValue
}
