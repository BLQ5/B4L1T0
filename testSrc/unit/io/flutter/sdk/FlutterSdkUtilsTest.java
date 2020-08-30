/*
 * Copyright 2017 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.sdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FlutterSdkUtilsTest {
  @Test
  public void parseFlutterSdkPath() {
    final String content = "# Generated by pub on 2017-07-07 12:58:30.541312.\n" +
                           "async:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/async-1.13.3/lib/\n" +
                           "charcode:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/charcode-1.1.1/lib/\n" +
                           "collection:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/collection-1.14.2/lib/\n" +
                           "flutter:file:///Users/devoncarew/projects/flutter/flutter/packages/flutter/lib/\n" +
                           "http:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/http-0.11.3+13/lib/\n" +
                           "http_parser:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/http_parser-3.1.1/lib/\n" +
                           "intl:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/intl-0.14.0/lib/\n" +
                           "meta:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/meta-1.0.5/lib/\n" +
                           "path:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/path-1.4.2/lib/\n" +
                           "sky_engine:../../flutter/flutter/bin/cache/pkg/sky_engine/lib/\n" +
                           "source_span:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/source_span-1.4.0/lib/\n" +
                           "stack_trace:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/stack_trace-1.7.4/lib/\n" +
                           "string_scanner:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/string_scanner-1.0.2/lib/\n" +
                           "typed_data:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/typed_data-1.1.3/lib/\n" +
                           "vector_math:file:///Users/devoncarew/.pub-cache/hosted/pub.dartlang.org/vector_math-2.0.5/lib/\n" +
                           "flutter_sunflower:lib/\n";

    final String result = FlutterSdkUtil.parseFlutterSdkPath(content);
    assertEquals("/Users/devoncarew/projects/flutter/flutter", result);
  }
}
