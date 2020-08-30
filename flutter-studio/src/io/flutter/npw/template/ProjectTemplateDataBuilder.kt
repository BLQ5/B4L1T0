/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

import com.android.tools.idea.wizard.template.PackageName
import java.io.File

// Builder for ProjectTemplateData.
class ProjectTemplateDataBuilder(val isNewProject: Boolean) {
  var projectName: String? = null
  var packageName: PackageName? = null
  var sdkPath: File? = null
  var projectPath: File? = null
  var useKotlin: Boolean? = true
  var useSwift: Boolean? = true
  var androidXSupport: Boolean? = false
  var isOffline: Boolean? = false

  fun build() = ProjectTemplateData(
    projectName!!,
    sdkPath!!,
    projectPath!!,
    packageName!!,
    useKotlin!!,
    useSwift!!,
    androidXSupport!!,
    isOffline!!,
    isNewProject
  )
}
