package com.kayo.zx.view;

import com.kayo.zx.controller.DiagramController;
import com.kayo.zx.model.EdgeType;
import com.kayo.zx.model.SpiderType;

import javax.swing.*;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

public class AppToolbar extends JToolBar {
  private final List<DiagramController> controllers = new ArrayList<>();

  public AppToolbar() {
    setFloatable(false);

    add(new JLabel(" Spider: "));
    JComboBox<SpiderType> spiderTypeSelector = new JComboBox<>(SpiderType.values());
    setFixedWidth(spiderTypeSelector, 80);
    spiderTypeSelector.addActionListener(e -> {
      SpiderType selected = (SpiderType) spiderTypeSelector.getSelectedItem();
      if (selected != null) {
        setSpiderTypeForAll(selected);
      }
    });
    add(spiderTypeSelector);

    addSeparator();

    add(new JLabel(" Edge: "));
    JComboBox<EdgeType> edgeTypeSelector = new JComboBox<>(EdgeType.values());
    setFixedWidth(edgeTypeSelector, 110);
    edgeTypeSelector.addActionListener(e -> {
      EdgeType selected = (EdgeType) edgeTypeSelector.getSelectedItem();
      if (selected != null) {
        setEdgeTypeForAll(selected);
      }
    });
    add(edgeTypeSelector);

    addSeparator();

    JCheckBox showHadamardGateBox = new JCheckBox("Show H Gate");
    showHadamardGateBox.addActionListener(e -> setShowHadamardGateForAll(showHadamardGateBox.isSelected()));
    add(showHadamardGateBox);
  }

  private void setFixedWidth(JComponent component, int width) {
    Dimension dim = component.getPreferredSize();
    dim.width = width;
    component.setPreferredSize(dim);
    component.setMaximumSize(dim);
    component.setMinimumSize(dim);
  }

  public void setControllers(DiagramController... diagramControllers) {
    controllers.clear();
    controllers.addAll(List.of(diagramControllers));
  }

  private void setSpiderTypeForAll(SpiderType type) {
    for (DiagramController controller : controllers) {
      controller.setSpiderType(type);
    }
  }

  private void setEdgeTypeForAll(EdgeType type) {
    for (DiagramController controller : controllers) {
      controller.setEdgeType(type);
    }
  }

  private void setShowHadamardGateForAll(boolean show) {
    for (DiagramController controller : controllers) {
      controller.setShowHadamardGate(show);
    }
    SwingUtilities.getWindowAncestor(this).repaint();
  }
}
