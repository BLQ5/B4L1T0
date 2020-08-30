/*
 * Copyright 2019 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.editor;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import io.flutter.dart.FlutterDartAnalysisServer;
import io.flutter.inspector.DiagnosticsNode;
import io.flutter.inspector.InspectorService;
import io.flutter.inspector.InspectorStateService;
import io.flutter.run.daemon.FlutterApp;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class WidgetViewModelDataBase {
  public final @Nullable RangeHighlighter _highlighter;
  public final @Nullable Document document;
  public final FlutterDartAnalysisServer flutterDartAnalysisService;
  public final InspectorStateService inspectorStateService;
  public final EditorPositionService editorPositionService;
  public final @Nullable  EditorEx editor;

  public WidgetViewModelDataBase(
    RangeHighlighter highlighter,
    Document document,
    FlutterDartAnalysisServer flutterDartAnalysisService,
    InspectorStateService inspectorStateService,
    EditorPositionService editorPositionService,
    EditorEx editor
    ) {
    this._highlighter = highlighter;
    this.document = document;
    this.flutterDartAnalysisService = flutterDartAnalysisService;
    this.inspectorStateService = inspectorStateService;
    this.editorPositionService = editorPositionService;
    this.editor = editor;
  }
}
