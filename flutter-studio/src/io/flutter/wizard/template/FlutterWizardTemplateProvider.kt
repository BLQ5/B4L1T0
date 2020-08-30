/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.wizard.template

import io.flutter.npw.template.Template
import io.flutter.wizard.template.projects.flutterProjectApp
import io.flutter.wizard.template.projects.flutterProjectModule
import io.flutter.wizard.template.projects.flutterProjectPackage
import io.flutter.wizard.template.projects.flutterProjectPlugin

/**
 * This would be the implementation of the Android Wizard Template plugin extension point, if it were re-usable.
 */
class FlutterWizardTemplateProvider {
  fun getTemplates(): List<Template> = listOf(
    flutterProjectApp(),
    flutterProjectModule(),
    flutterProjectPackage(),
    flutterProjectPlugin()
  )
}
