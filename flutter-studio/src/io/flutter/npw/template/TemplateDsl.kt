/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

import com.android.tools.idea.wizard.template.Thumb
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.findResource
import java.io.File
import javax.swing.Icon

// See com.android.tools.idea.wizard.template.TemplateDSL
internal data class TemplateImpl(
  override val name: String,
  override val description: String,
  override val documentationUrl: String?,
  override val icon: Icon,
  override val widgets: Collection<Widget<*>>,
  private val _thumb: () -> Thumb,
  override val recipe: Recipe,
  override val uiContexts: Collection<WizardUiContext>,
  override val constraints: Collection<TemplateConstraint>
) : Template {
  override fun thumb(): Thumb = _thumb()
}

@DslMarker
annotation class TemplateDsl

fun template(block: TemplateBuilder.() -> Unit): Template = TemplateBuilder().apply(block).build()

@TemplateDsl
class TemplateBuilder {
  var name: String? = null
  var description: String? = null
  var documentationUrl: String? = null
  var icon: Icon? = null
  var thumb: () -> Thumb = { Thumb.NoThumb }
  var recipe: Recipe? = null
  var widgets = listOf<Widget<*>>()
  var uiContexts = listOf<WizardUiContext>()
  var constraints = listOf<TemplateConstraint>()

  fun widgets(vararg widgets: Widget<*>) {
    this.widgets = widgets.toList()
  }

  @TemplateDsl
  class ThumbBuilder

  /** A wrapper for collection of [Thumb]s with an optional [get]ter. Implementations usually use [Parameter.value] to choose [Thumb]. */
  fun thumb(block: ThumbBuilder.() -> File) {
    thumb = { Thumb { findResource(this.javaClass, ThumbBuilder().block()) } }
  }

  internal fun build(): Template {
    checkNotNull(name) { "Template must have a name." }
    checkNotNull(description) { "Template must have a description." }
    checkNotNull(recipe) { "Template must have a recipe to run." }
    checkNotNull(icon) { "Template must have an icon." }

    return TemplateImpl(
      name!!,
      description!!,
      documentationUrl,
      icon!!,
      widgets,
      thumb,
      recipe!!,
      uiContexts,
      constraints
    )
  }
}
