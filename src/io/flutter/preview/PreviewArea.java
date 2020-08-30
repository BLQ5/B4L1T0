/*
 * Copyright 2018 The Chromium Authors. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package io.flutter.preview;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import io.flutter.dart.FlutterDartAnalysisServer;
import io.flutter.editor.*;
import io.flutter.inspector.DiagnosticsNode;
import io.flutter.inspector.InspectorService;
import io.flutter.inspector.InspectorStateService;
import net.miginfocom.swing.MigLayout;
import org.dartlang.analysis.server.protocol.FlutterOutline;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreviewArea {
  public static int BORDER_WIDTH = 0;
  public static final String NOTHING_TO_SHOW = "Nothing to show. Run the application to see a preview of the app.";

  private static final Color labelColor = new JBColor(new Color(0x333333), new Color(0xcccccc));
  public static String NOT_RENDERABLE = "Unable to take a screenshot of the running application.";

  private final Listener myListener;

  private final DefaultActionGroup toolbarGroup = new DefaultActionGroup();
  private final ActionToolbar windowToolbar;

  private final SimpleToolWindowPanel window;

  private final JLayeredPane layeredPanel = new JLayeredPane();
  private final JPanel primaryLayer;

  private final JPanel handleLayer = new JPanel(null);
  private final PreviewViewModel preview;

  private boolean isBeingRendered = false;

  private final Map<FlutterOutline, JComponent> outlineToComponent = new HashMap<>();
  private final List<SelectionEditPolicy> selectionComponents = new ArrayList<>();

  private PreviewViewModel previewViewModel;
  public PreviewArea(Project project, Listener listener) {
    final WidgetViewModelDataBase previewData = new WidgetViewModelDataBase(
      null,
      null,
      FlutterDartAnalysisServer.getInstance(project),
      InspectorStateService.getInstance(project),
      EditorPositionService.getInstance(project),
      null
    );

    preview = new PreviewViewModel(new WidgetViewModelData(null, previewData));

    primaryLayer = new PreviewViewModelPanel(preview);
    this.myListener = listener;

    windowToolbar = ActionManager.getInstance().createActionToolbar("PreviewArea", toolbarGroup, true);

    window = new SimpleToolWindowPanel(true, true);
    window.setToolbar(windowToolbar.getComponent());

    primaryLayer.setLayout(new BorderLayout());
    clear(NOTHING_TO_SHOW);

    // Layers must be transparent.
    handleLayer.setOpaque(false);

    window.setContent(layeredPanel);
    layeredPanel.add(primaryLayer, Integer.valueOf(0));
    layeredPanel.add(handleLayer, Integer.valueOf(1));

    // Layers must cover the whole root panel.
    layeredPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        final Dimension renderSize = getRenderSize();
        listener.resized(renderSize.width, renderSize.height);
      }
    });
  }

  /**
   * Return the Swing component of the area.
   */
  public JComponent getComponent() {
    return window;
  }

  public void clear(String message) {
    final JPanel panel = new JPanel();
    panel.setLayout(new MigLayout("", "[grow, center]", "[grow][][grow 200]"));

    panel.add(new JBLabel(message, SwingConstants.CENTER), "cell 0 1");
    clear(panel);
  }

  public void clear(JComponent component) {
    setToolbarTitle(null);

    outlineToComponent.clear();

    primaryLayer.removeAll();
    primaryLayer.setLayout(new BorderLayout());
    primaryLayer.add(component, BorderLayout.CENTER);

    handleLayer.removeAll();

    window.revalidate();
    window.repaint();
  }

  /**
   * A new outline was received, and we started rendering.
   * Until rendering is finished, the area is inconsistent with the new outline.
   * It should not ignore incoming events and should not send its events to the listener.
   */
  public void renderingStarted() {
    isBeingRendered = true;
  }



  /**
   * Rendering finished, the new outline and rendering information is available.
   * Show the rendered outlines.
   */
  public void show(DiagnosticsNode node) {
    isBeingRendered = false;


    primaryLayer.removeAll();
    primaryLayer.setLayout(null);

    /* NOT LOADED Yet.
    if (rootOutline == null) {
      clear(NO_WIDGET_MESSAGE);
      return;
    }

     */

    if (node != null) {
      final String description = node.getDescription();
      setToolbarTitle(description);
    }
    else {
      setToolbarTitle(null);
    }

    outlineToComponent.clear();

    window.revalidate();
    window.repaint();
  }

  public void select(@NotNull List<FlutterOutline> outlines) {
    if (isBeingRendered) {
      return;
    }

    for (SelectionEditPolicy policy : selectionComponents) {
      policy.deactivate();
    }
    selectionComponents.clear();

    for (FlutterOutline outline : outlines) {
      final JComponent widget = outlineToComponent.get(outline);
      if (widget != null) {
        final SelectionEditPolicy selectionPolicy = new SelectionEditPolicy(handleLayer, widget);
        selectionComponents.add(selectionPolicy);
        selectionPolicy.activate();
      }
    }

    primaryLayer.repaint();
  }


  private void setToolbarTitle(String text) {
    toolbarGroup.removeAll();
    toolbarGroup.add(new TitleAction(text == null ? "Preview" : ("Preview: " + text)));
    windowToolbar.updateActionsImmediately();
  }

  public Dimension getRenderSize() {
    final int width = layeredPanel.getWidth();
    final int height = layeredPanel.getHeight();
    for (Component child : layeredPanel.getComponents()) {
      child.setBounds(0, 0, width, height);
    }

    final int renderWidth = width - 2 * BORDER_WIDTH;
    final int renderHeight = height - 2 * BORDER_WIDTH;
    return new Dimension(renderWidth, renderHeight);
  }

  interface Listener {
    void clicked(FlutterOutline outline);

    void doubleClicked(FlutterOutline outline);

    void resized(int width, int height);
  }
}

class TitleAction extends AnAction implements CustomComponentAction {
  TitleAction(String text) {
    super(text);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
  }

  @Override
  public JComponent createCustomComponent(Presentation presentation) {
    final JPanel panel = new JPanel(new BorderLayout());

    // Add left border to make the title look similar to the tool window title.
    panel.setBorder(BorderFactory.createEmptyBorder(0, JBUI.scale(3), 0, 0));

    final String text = getTemplatePresentation().getText();
    panel.add(new JBLabel(text != null ? text : "", UIUtil.ComponentStyle.SMALL));

    return panel;
  }
}

class DropShadowBorder extends AbstractBorder {
  @SuppressWarnings("UseJBColor") private static final Color borderColor = new Color(0x7F000000, true);

  public DropShadowBorder() {
  }

  public Insets getBorderInsets(Component component) {
    //noinspection UseDPIAwareInsets
    return new Insets(0, 0, 1, 1);
  }

  public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    g.setColor(borderColor);
    final int x1 = x + 1;
    final int y1 = y + 1;
    final int x2 = x + width - 1;
    final int y2 = y + height - 1;
    g.drawLine(x1, y2, x2, y2);
    g.drawLine(x2, y1, x2, y2 - 1);
  }

}
