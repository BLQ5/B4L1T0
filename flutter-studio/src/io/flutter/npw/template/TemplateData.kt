/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template

import com.android.tools.idea.wizard.template.PackageName
import java.io.File

enum class Language(val string: String, val extension: String) {
  Java("Java", "java"),
  Kotlin("Kotlin", "kt"),
  Swift("Swift", "swift"),
  ObjC("Objective-C", "objc");

  override fun toString(): String = string

  companion object {
    /**
     * Finds a language matching the requested name. Returns specified 'defaultValue' if not found.
     */
    @JvmStatic
    fun fromName(name: String?, defaultValue: Language): Language =
      values().firstOrNull { it.string == name } ?: defaultValue
  }
}

sealed class TemplateData

data class ProjectTemplateData(
  val projectName: String,
  val sdkPath: File,
  val projectPath: File,
  val packageName: PackageName,
  val useKotlin: Boolean,
  val useSwift: Boolean,
  val androidXSupport: Boolean,
  val isOffline: Boolean,
  val isNewProject: Boolean
) : TemplateData()

data class ModuleTemplateData(
  val projectTemplateData: ProjectTemplateData,
  val name: String,
  val path: String
) : TemplateData()
