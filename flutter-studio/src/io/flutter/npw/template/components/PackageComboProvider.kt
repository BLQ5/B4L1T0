/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.template.components

import com.android.tools.idea.observable.AbstractProperty
import com.android.tools.idea.observable.ui.TextProperty
import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaCodeFragment
import com.intellij.ui.EditorComboBox
import com.intellij.ui.JavaReferenceEditorUtil
import com.intellij.ui.RecentsManager
import io.flutter.npw.template.Parameter
import io.flutter.npw.template.StringParameter

/**
 * Provides an editable combobox which allows the user to specify a package name (or pull from a list of recently used packages).
 */
class PackageComboProvider(private val project: Project,
                           parameter: StringParameter,
                           private val initialPackage: String,
                           private val recentsKey: String) : ParameterComponentProvider<EditorComboBox>(parameter) {
  override fun createComponent(parameter: Parameter<*>): EditorComboBox {
    val doc = JavaReferenceEditorUtil.createDocument(
      initialPackage, project, false, JavaCodeFragment.VisibilityChecker.PROJECT_SCOPE_VISIBLE)!!
    val classComboBox = EditorComboBox(doc, project, StdFileTypes.JAVA)

    // Make sure our suggested package is in the recents list and at the top
    RecentsManager.getInstance(project).registerRecentEntry(recentsKey, initialPackage)
    val recents = RecentsManager.getInstance(project).getRecentEntries(recentsKey)!!
    // We just added at least one entry!

    classComboBox.setHistory(recents.toTypedArray())
    return classComboBox
  }

  override fun createProperty(component: EditorComboBox): AbstractProperty<*>? = TextProperty(component)

  override fun accept(component: EditorComboBox) {
    RecentsManager.getInstance(project).registerRecentEntry(recentsKey, component.text)
  }
}
