/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

import com.android.tools.idea.wizard.template.RecipeExecutor
import com.android.tools.idea.wizard.template.Thumb
import com.android.tools.idea.wizard.template.WizardUiContext
import icons.FlutterIcons
import javax.swing.Icon

typealias Recipe = RecipeExecutor.(TemplateData) -> Unit

enum class TemplateConstraint {
  AndroidX,
  Kotlin,
  Swift
}

interface Template {
  /**
   * When a [Template] is chosen by the user, the [widgets] are used by the Wizards to build the user UI.
   *
   * Usually, it displays an input for [Parameter].
   */
  val widgets: Collection<Widget<*>>

  /**
   * Usually, a user provides [Parameter.value]s by interaction with the UI [widgets].
   **/
  val parameters: Collection<Parameter<*>> get() = widgets.filterIsInstance<ParameterWidget<*>>().map { it.parameter }

  /**
   * A template name which is also used as identified.
   */
  val name: String
  val description: String

  /**
   * Address of an external website with more details about the template.
   */
  val documentationUrl: String?
  val icon: Icon

  /** Recipe used to generate this [Template] output. It will be called after the user provides values for all [Parameter]s. */
  val recipe: Recipe

  /** The template will be shown only in given context. Should include all possible contexts by default. */
  val uiContexts: Collection<WizardUiContext>

  /** Conditions under which the template may be rendered. For example, some templates only support AndroidX */
  val constraints: Collection<TemplateConstraint>

  /** Returns a thumbnail which are drawn in the UI. It will be called every time when any parameter is updated. */
  fun thumb(): Thumb

  companion object None: Template {
    override val widgets: Collection<Widget<*>> = listOf()
    override val uiContexts: Collection<WizardUiContext> = listOf()
    override val constraints: Collection<TemplateConstraint> = listOf()
    override val recipe: Recipe get() = throw UnsupportedOperationException()
    override val name: String = "None"
    override val description: String = "Creates a new empty project"
    override val documentationUrl: String? = null
    override val icon: Icon = FlutterIcons.Flutter_64
    override fun thumb() = Thumb.NoThumb
  }
}
