/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

/**
 * Represents a UI element.
 *
 * Each template has a collection of widgets which is rendered when user selects a template.
 */
interface Widget<T>

/**
 * Represents an UI element which has underlying [parameter].
 *
 * Usually parameters and widgets have 1-1 relationship but there are exceptions:
 *  * Some [Parameter]s may have no widget because they pass data implicitly and don't need user input.
 *  * Some [Widget]s may have no underlying [Parameter] (e.g. [Separator])
 */
sealed class ParameterWidget<T>(val parameter: Parameter<T>): Widget<T>

/**
 * An ordinary text field.
 */
data class TextFieldWidget(val p: StringParameter): ParameterWidget<String>(p)

/**
 * An ordinary text label.
 */
data class LabelWidget(val text: String): Widget<String>

/**
 * [Widget] for selecting package.
 *
 * Looks like combination of [TextFieldWidget] and [EnumWidget] (use can both write and choose from drop down menu).
 */
data class PackageNameWidget(val p: StringParameter): ParameterWidget<String>(p)

/**
 * [Widget] for selecting Flutter SDK.
 *
 * Looks like combination of [TextFieldWidget] and [EnumWidget] (use can both write and choose from drop down menu).
 */
data class SdkSelectorWidget(val p: StringParameter): ParameterWidget<String>(p)

/**
 * An ordinary checkbox.
 */
data class CheckBoxWidget(val p: BooleanParameter): ParameterWidget<Boolean>(p)

/**
 * Horizontal separator. Has no functionality.
 */
object Separator : Widget<Nothing>
