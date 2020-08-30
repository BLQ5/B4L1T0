/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.model

import com.android.tools.idea.wizard.model.WizardModel
import com.intellij.openapi.module.Module
import io.flutter.npw.template.Template
import io.flutter.npw.template.WizardParameterData
import java.io.File

/**
 * A model responsible for instantiating a [Template] into the current project representing a Flutter project.
 * See com.android.tools.idea.npw.model.RenderTemplateModel
 */
class RenderTemplateModel private constructor(
  projectData: FlutterProjectData,
  private val commandName: String,
  private val shouldOpenFiles: Boolean,
  val createdFiles: MutableList<File> = arrayListOf()
) : WizardModel(), FlutterProjectData by projectData {
  var module: Module? = null
  lateinit var wizardParameterData: WizardParameterData
  var newTemplate: Template = Template.None
    set(value) {
      field = value
      wizardParameterData = WizardParameterData(
        packageName.get(),
        module == null,
        value.parameters
      )
    }

  override fun handleFinished() {
    TODO("Not yet implemented")
  }

  companion object {
    fun fromProjectModel(projectModel: NewFlutterProjectModel): RenderTemplateModel {
      return RenderTemplateModel(projectModel, "", true)
    }
  }
}
