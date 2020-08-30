/*
 * Copyright 2017 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.module;

import io.flutter.FlutterBundle;

public enum FlutterProjectType {
  APP(FlutterBundle.message("flutter.module.create.settings.type.application"), "app", ""),
  PLUGIN(FlutterBundle.message("flutter.module.create.settings.type.plugin"), "plugin", " with an example app"),
  PACKAGE(FlutterBundle.message("flutter.module.create.settings.type.package"), "package", " of re-usable Dart code"),
  MODULE(FlutterBundle.message("flutter.module.create.settings.type.module"), "module", " for add-to-app projects"),
  IMPORT(FlutterBundle.message("flutter.module.create.settings.type.import_module"), "module", "");

  final public String title;
  final public String arg;
  final public String aux;

  FlutterProjectType(String title, String arg, String aux) {
    this.title = title;
    this.arg = arg;
    this.aux = aux;
  }

  public String toString() {
    return title;
  }
}
