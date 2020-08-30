/*
 * Copyright 2020 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.npw.project

import com.android.tools.adtui.ASGallery
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.layout.panel
import com.intellij.ui.components.*
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.GrowPolicy
import com.intellij.ui.layout.noGrowY
import com.intellij.uiDesigner.core.Spacer
import javax.swing.JLabel
import javax.swing.JScrollPane

class ChooseFlutterProjectPanelUi<T>(val gallery: ASGallery<T>) {
  lateinit var documentationLink: HyperlinkLabel

  var templateName: String = "(Template Name)"
  set(value) { nameLabel.text = value; field = value }

  var templateDescription: String = "(Template Description Text)"
  set(value) { descriptionLabel.text = value; field = value }

  lateinit var nameLabel: JLabel
  lateinit var descriptionLabel: JLabel
  lateinit var scrollPanel: CellBuilder<JScrollPane>

  val panel = panel {
    row {
      cell(isFullWidth = true) {
        scrollPane(gallery).also { scrollPanel = it }
        scrollPanel.noGrowY()
      }
    }
    row {
      Spacer()()
    }
    row {
      component(Label(templateName).also { nameLabel = it })
    }
    row {
      component(Label(templateDescription).also { descriptionLabel = it })
      component(HyperlinkLabel("See documentation").also { documentationLink = it })
    }
  }
}
