/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.wizard.template.projects

import com.android.tools.idea.npw.model.NewProjectModel
import com.android.tools.idea.wizard.template.WizardUiContext
import com.android.tools.idea.wizard.template.camelCaseToUnderlines
import com.android.tools.idea.wizard.template.extractClassName
import icons.FlutterIcons
import io.flutter.FlutterBundle
import io.flutter.module.FlutterProjectType
import io.flutter.npw.recipes.createFlutterModule
import io.flutter.npw.recipes.createFlutterProject
import io.flutter.npw.template.BooleanParameter
import io.flutter.npw.template.CheckBoxWidget
import io.flutter.npw.template.Constraint
import io.flutter.npw.template.ModuleTemplateData
import io.flutter.npw.template.PackageNameWidget
import io.flutter.npw.template.SdkSelectorWidget
import io.flutter.npw.template.StringParameter
import io.flutter.npw.template.TemplateData
import io.flutter.npw.template.TextFieldWidget
import io.flutter.npw.template.booleanParameter
import io.flutter.npw.template.stringParameter
import io.flutter.npw.template.template
import java.io.File

fun flutterProjectApp() = flutterProjectTemplate(FlutterProjectType.APP)
fun flutterProjectModule() = flutterProjectTemplate(FlutterProjectType.MODULE)
fun flutterProjectPackage() = flutterProjectTemplate(FlutterProjectType.PACKAGE)
fun flutterProjectPlugin() = flutterProjectTemplate(FlutterProjectType.PLUGIN)

fun flutterModuleApp() = flutterModuleTemplate(FlutterProjectType.APP)
fun flutterModuleModule() = flutterModuleTemplate(FlutterProjectType.MODULE)
fun flutterModulePackage() = flutterModuleTemplate(FlutterProjectType.PACKAGE)
fun flutterModulePlugin() = flutterModuleTemplate(FlutterProjectType.PLUGIN)
fun flutterModuleImport() = flutterModuleTemplate(FlutterProjectType.IMPORT)

private fun flutterProjectTemplate(type: FlutterProjectType) = template {
  name = FlutterBundle.message("studio.npw.template.name", type.title)
  description = FlutterBundle.message("studio.npw.template.description", type.title.toLowerCase(), type.aux)
  icon = FlutterIcons.Flutter
  uiContexts = listOf(WizardUiContext.NewProject)

  lateinit var projectName: StringParameter
  val baseClassName: StringParameter = stringParameter {
    name = "Class name"
    constraints = listOf(Constraint.NONEMPTY, Constraint.CLASS, Constraint.UNIQUE)
    suggest = { extractClassName(projectName.value) }
    default = "Flutter${type.arg.capitalize()}"
    visible = { false }
  }

  projectName = stringParameter {
    name = FlutterBundle.message("studio.npw.project.name")
    help = FlutterBundle.message("studio.npw.project.hint", type.title.toLowerCase())
    constraints = listOf(Constraint.NONEMPTY, Constraint.PROJECT)
    suggest = { camelCaseToUnderlines(baseClassName.value) }
    default = "flutter_${type.arg}"
  }

  val sdkSelector: StringParameter = stringParameter {
    name = FlutterBundle.message("studio.npw.flutter.sdk.name")
    help = FlutterBundle.message("studio.npw.flutter.sdk.hint")
    constraints = listOf(Constraint.NONEMPTY, Constraint.SDK)
    //suggest = { baseClassName.value }
    default = ""
  }

  val projectLocation: StringParameter = stringParameter {
    name = FlutterBundle.message("studio.npw.save.location.name")
    help = FlutterBundle.message("studio.npw.save.location.hint")
    constraints = listOf(Constraint.NONEMPTY)
    //suggest = { baseClassName.value }
    default = ""
  }

  val packageName: StringParameter = stringParameter {
    name = FlutterBundle.message("studio.npw.package.name")
    help = FlutterBundle.message("studio.npw.package.hint")
    constraints = listOf(Constraint.NONEMPTY)
    suggest = { NewProjectModel.getSuggestedProjectPackage() }
    default = NewProjectModel.getSuggestedProjectPackage()
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.MODULE || type == FlutterProjectType.APP }
  }

  val isOffline: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.create.offline.name")
    help = FlutterBundle.message("studio.npw.create.offline.hint", type.title.toLowerCase())
    default = false
  }

  val useKotlin: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.use.kotlin.name")
    help = FlutterBundle.message("studio.npw.use.kotlin.hint")
    default = true
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.APP }
  }

  val useSwift: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.use.swift.name")
    help = FlutterBundle.message("studio.npw.use.swift.hint")
    default = true
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.APP }
  }

  val useLegacyLibraries: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.use.androidx.name")
    help = FlutterBundle.message("studio.npw.use.androidx.hint")
    default = false
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.MODULE || type == FlutterProjectType.APP }
  }

  widgets(
    TextFieldWidget(projectName),
    SdkSelectorWidget(sdkSelector),
    TextFieldWidget(projectLocation),
    TextFieldWidget(packageName),
    CheckBoxWidget(useKotlin),
    CheckBoxWidget(useSwift),
    CheckBoxWidget(useLegacyLibraries),
    CheckBoxWidget(isOffline)
  )

  thumb {
    File("template_basic_activity.png")
  }

  recipe = { data: TemplateData ->
    createFlutterProject(
      data as ModuleTemplateData, projectName.value, sdkSelector.value, projectLocation.value, packageName.value,
      isOffline.value, useKotlin.value, useSwift.value, useLegacyLibraries.value, type)
  }
}

private fun flutterModuleTemplate(type: FlutterProjectType) = template {
  name = FlutterBundle.message("studio.npw.template.name", type.title.capitalize())
  description = FlutterBundle.message("studio.npw.template.description", type.title, type.aux)
  icon = FlutterIcons.Flutter
  uiContexts = listOf(WizardUiContext.NewModule)

  lateinit var projectName: StringParameter
  val baseClassName: StringParameter = stringParameter {
    name = "Class name"
    constraints = listOf(Constraint.NONEMPTY, Constraint.CLASS, Constraint.UNIQUE)
    suggest = { extractClassName(projectName.value) }
    default = "Flutter${type.title.capitalize()}"
    visible = { false }
  }

  projectName = stringParameter {
    name = FlutterBundle.message("studio.npw.project.name")
    help = FlutterBundle.message("studio.npw.project.hint", type.title)
    constraints = listOf(Constraint.NONEMPTY, Constraint.PROJECT)
    suggest = { camelCaseToUnderlines(baseClassName.value) }
    default = "flutter_${type.title}"
  }

  val sdkSelector: StringParameter = stringParameter {
    name = FlutterBundle.message("studio.npw.flutter.sdk.name")
    help = FlutterBundle.message("studio.npw.flutter.sdk.hint")
    constraints = listOf(Constraint.NONEMPTY, Constraint.SDK)
    //suggest = { baseClassName.value }
    default = ""
  }

  val projectLocation: StringParameter = stringParameter {
    name = FlutterBundle.message("studio.npw.save.location.name")
    help = FlutterBundle.message("studio.npw.save.location.hint")
    constraints = listOf(Constraint.NONEMPTY)
    //suggest = { baseClassName.value }
    default = ""
  }

  val packageName: StringParameter = stringParameter {
    name = FlutterBundle.message("studio.npw.package.name")
    help = FlutterBundle.message("studio.npw.package.hint")
    constraints = listOf(Constraint.NONEMPTY)
    suggest = { NewProjectModel.getSuggestedProjectPackage() }
    default = NewProjectModel.getSuggestedProjectPackage()
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.MODULE || type == FlutterProjectType.APP }
  }

  val isOffline: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.create.offline.name")
    help = FlutterBundle.message("studio.npw.create.offline.hint", type.title)
    default = false
  }

  val useKotlin: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.use.kotlin.name")
    help = FlutterBundle.message("studio.npw.use.kotlin.hint")
    default = true
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.APP }
  }

  val useSwift: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.use.swift.name")
    help = FlutterBundle.message("studio.npw.use.swift.hint")
    default = true
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.APP }
  }

  val useLegacyLibraries: BooleanParameter = booleanParameter {
    name = FlutterBundle.message("studio.npw.use.androidx.name")
    help = FlutterBundle.message("studio.npw.use.androidx.hint")
    default = false
    visible = { type == FlutterProjectType.PLUGIN || type == FlutterProjectType.MODULE || type == FlutterProjectType.APP }
  }

  widgets(
    TextFieldWidget(projectName),
    SdkSelectorWidget(sdkSelector),
    TextFieldWidget(projectLocation),
    PackageNameWidget(packageName),
    CheckBoxWidget(useKotlin),
    CheckBoxWidget(useSwift),
    CheckBoxWidget(useLegacyLibraries),
    CheckBoxWidget(isOffline)
  )

  thumb {
    File("template_basic_activity.png")
  }

  recipe = { data: TemplateData ->
    createFlutterModule(
      data as ModuleTemplateData, projectName.value, sdkSelector.value, projectLocation.value, packageName.value,
      isOffline.value, useKotlin.value, useSwift.value, useLegacyLibraries.value, type)
  }
}
