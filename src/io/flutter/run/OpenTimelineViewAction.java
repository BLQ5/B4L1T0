/*
 * Copyright 2018 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.run;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.Computable;
import com.jetbrains.lang.dart.ide.runner.ObservatoryConnector;
import icons.FlutterIcons;
import io.flutter.FlutterInitializer;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ComponentNotRegistered")
public class OpenTimelineViewAction extends DumbAwareAction {
  private final @NotNull ObservatoryConnector myConnector;
  private final Computable<Boolean> myIsApplicable;

  public OpenTimelineViewAction(@NotNull final ObservatoryConnector connector, @NotNull final Computable<Boolean> isApplicable) {
    super("Open Timeline View", "Open Timeline View", FlutterIcons.OpenTimeline);
    myConnector = connector;
    myIsApplicable = isApplicable;
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(myIsApplicable.compute());
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    FlutterInitializer.sendAnalyticsAction(this);

    final String url = myConnector.getBrowserUrl();
    if (url != null) {
      BrowserLauncher.getInstance().browse(url + "/#/timeline-dashboard", null);
    }
  }
}
