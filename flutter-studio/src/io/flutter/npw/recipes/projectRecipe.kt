/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.recipes

import com.android.tools.idea.wizard.template.RecipeExecutor
import io.flutter.module.FlutterProjectType
import io.flutter.npw.template.ModuleTemplateData

fun RecipeExecutor.createFlutterProject(
  moduleTemplateData: ModuleTemplateData,
  projectName: String,
  sdk: String,
  projectLocation: String,
  packageName: String,
  isOffline: Boolean,
  useKotlin: Boolean,
  useSwift: Boolean,
  useAndroidX: Boolean,
  type: FlutterProjectType) {

}
