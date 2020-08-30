/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.model

import com.android.tools.idea.gradle.project.AndroidNewProjectInitializationStartupActivity
import com.android.tools.idea.gradle.project.importing.GradleProjectImporter
import com.android.tools.idea.npw.model.MultiTemplateRenderer
import com.android.tools.idea.npw.model.NewProjectModel
import com.android.tools.idea.observable.core.BoolProperty
import com.android.tools.idea.observable.core.BoolValueProperty
import com.android.tools.idea.observable.core.OptionalValueProperty
import com.android.tools.idea.observable.core.StringProperty
import com.android.tools.idea.observable.core.StringValueProperty
import com.android.tools.idea.wizard.model.WizardModel
import com.intellij.ide.impl.OpenProjectTask
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import io.flutter.FlutterBundle
import io.flutter.npw.template.ProjectTemplateDataBuilder
import java.io.File
import java.util.Optional

private val logger: Logger get() = logger<NewProjectModel>()

interface FlutterProjectData {
  val projectName: StringProperty
  val packageName: StringProperty
  val projectLocation: StringProperty
  val sdkPath: StringProperty
  val isOffline: BoolProperty
  val isNewProject: Boolean
  val useKotlin: BoolProperty
  val useSwift: BoolProperty
  val androidXSupport: BoolProperty
  val project: OptionalValueProperty<Project>
  val multiTemplateRenderer: MultiTemplateRenderer
  val projectTemplateDataBuilder: ProjectTemplateDataBuilder
}

// See com.android.tools.idea.npw.model.NewProjectModel
class NewFlutterProjectModel : WizardModel(), FlutterProjectData {
  override val projectName = StringValueProperty()//FlutterBundle.message("module.wizard.app_name"))
  override val packageName = StringValueProperty()
  override val projectLocation = StringValueProperty()
  override val sdkPath = StringValueProperty()
  override val isNewProject = true
  override val isOffline = BoolValueProperty()
  override val useKotlin = BoolValueProperty()
  override val useSwift = BoolValueProperty()
  override val androidXSupport = BoolValueProperty()
  override val project = OptionalValueProperty<Project>()
  override val multiTemplateRenderer = MultiTemplateRenderer { renderer ->
    val projectName = projectName.get()
    val projectLocation = projectLocation.get()
    val projectBaseDirectory = File(projectLocation)

    // TODO Change this to do flutter create without creating a Project instance.
    project.set(Optional.of(GradleProjectImporter.getInstance().createProject(projectName, projectBaseDirectory)))
    GradleProjectImporter.configureNewProject(project.value)
    AndroidNewProjectInitializationStartupActivity.setProjectInitializer(project.value) {
      logger.info("Rendering a new project.")
      NonProjectFileWritingAccessProvider.disableChecksDuring {
        renderer(project.value) // flutter create <projectName>
      }
    }

    val openProjectTask = OpenProjectTask(
      project = project.value,
      isNewProject = false,  // We have already created a new project.
      forceOpenInNewFrame = true
    )
    ProjectManagerEx.getInstanceEx().openProject(projectBaseDirectory.toPath(), openProjectTask)
  }
  override val projectTemplateDataBuilder = ProjectTemplateDataBuilder(true)

  init {
    projectName.addConstraint(String::trim)
  }

  override fun handleFinished() {
    TODO("Not yet implemented")
  }
}