package com.kayo.zx.view;

import com.kayo.zx.controller.DiagramController;
import com.kayo.zx.controller.DiagramController.Tool;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AppToolbar extends JToolBar {
  private final List<DiagramController> controllers = new ArrayList<>();

  public AppToolbar() {
    setFloatable(false);

    ButtonGroup toolGroup = new ButtonGroup();
    JToggleButton zSpiderBtn = new JToggleButton("Add Z Spider");
    zSpiderBtn.setActionCommand(Tool.ADD_Z_SPIDER.name());
    zSpiderBtn.setSelected(true);

    JToggleButton xSpiderBtn = new JToggleButton("Add X Spider");
    xSpiderBtn.setActionCommand(Tool.ADD_X_SPIDER.name());

    JToggleButton edgeBtn = new JToggleButton("Add Edge");
    edgeBtn.setActionCommand(Tool.ADD_EDGE.name());

    JToggleButton hEdgeBtn = new JToggleButton("Add H Edge");
    hEdgeBtn.setActionCommand(Tool.ADD_HADAMARD_EDGE.name());

    toolGroup.add(zSpiderBtn);
    toolGroup.add(xSpiderBtn);
    toolGroup.add(edgeBtn);
    toolGroup.add(hEdgeBtn);

    zSpiderBtn.addActionListener(e -> setToolForAll(Tool.valueOf(e.getActionCommand())));
    xSpiderBtn.addActionListener(e -> setToolForAll(Tool.valueOf(e.getActionCommand())));
    edgeBtn.addActionListener(e -> setToolForAll(Tool.valueOf(e.getActionCommand())));
    hEdgeBtn.addActionListener(e -> setToolForAll(Tool.valueOf(e.getActionCommand())));

    add(zSpiderBtn);
    add(xSpiderBtn);
    add(edgeBtn);
    add(hEdgeBtn);
    addSeparator();

    JCheckBox showHadamardGateBox = new JCheckBox("Show H Gate");
    showHadamardGateBox.addActionListener(e -> setShowHadamardGateForAll(showHadamardGateBox.isSelected()));
    add(showHadamardGateBox);
  }

  public void setControllers(DiagramController... diagramControllers) {
    controllers.clear();
    controllers.addAll(List.of(diagramControllers));
  }

  private void setToolForAll(Tool tool) {
    for (DiagramController controller : controllers) {
      controller.setTool(tool);
    }
  }

  private void setShowHadamardGateForAll(boolean show) {
    for (DiagramController controller : controllers) {
      controller.setShowHadamardGate(show);
    }
    SwingUtilities.getWindowAncestor(this).repaint();
  }
}
