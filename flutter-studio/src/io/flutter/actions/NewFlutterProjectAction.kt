/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.actions

import com.android.tools.idea.ui.wizard.StudioWizardDialogBuilder
import com.android.tools.idea.wizard.model.ModelWizard
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.wm.impl.welcomeScreen.NewWelcomeScreen
import com.intellij.ui.LayeredIcon
import com.intellij.ui.OffsetIcon
import icons.FlutterIcons
import io.flutter.FlutterBundle
import io.flutter.npw.project.ChooseFlutterProjectStep
import io.flutter.npw.model.NewFlutterProjectModel
import javax.swing.Icon

// See com.android.tools.idea.actions.AndroidNewProjectAction
class NewFlutterProjectAction @JvmOverloads constructor(text: String = FlutterBundle.message(
  "studio.project.create.menu.action")) : AnAction(text), DumbAware {
  override fun update(e: AnActionEvent) {
    if (NewWelcomeScreen.isNewWelcomeScreen(e)) {
      e.presentation.icon = getFlutterDecoratedIcon()
      e.presentation.text = FlutterBundle.message("studio.project.create.welcome.action")
    }
  }

  override fun actionPerformed(e: AnActionEvent) {
    val wizard = ModelWizard.Builder().addStep(ChooseFlutterProjectStep(NewFlutterProjectModel())).build()!!
    StudioWizardDialogBuilder(wizard, FlutterBundle.message("studio.project.create.welcome.action")).build().show()
  }

  private fun getFlutterDecoratedIcon(): Icon? {
    val icon = AllIcons.Welcome.CreateNewProject
    val badgeIcon: Icon = OffsetIcon(0, FlutterIcons.Flutter_badge).scale(0.666f)
    val decorated = LayeredIcon(2)
    decorated.setIcon(badgeIcon, 0, 7, 7)
    decorated.setIcon(icon, 1, 0, 0)
    return decorated
  }
}