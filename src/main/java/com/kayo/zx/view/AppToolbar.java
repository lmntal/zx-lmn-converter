package com.kayo.zx.view;

import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.kayo.zx.controller.DiagramController;

public class AppToolbar extends JToolBar {
  public AppToolbar(DiagramController controller) {
    setFloatable(false);

    ButtonGroup toolGroup = new ButtonGroup();
    JToggleButton selectBtn = new JToggleButton("Select");
    selectBtn.setActionCommand(DiagramController.Tool.SELECT.name());
    selectBtn.setSelected(true);
    JToggleButton zSpiderBtn = new JToggleButton("Add Z");
    zSpiderBtn.setActionCommand(DiagramController.Tool.ADD_Z_SPIDER.name());
    JToggleButton xSpiderBtn = new JToggleButton("Add X");
    xSpiderBtn.setActionCommand(DiagramController.Tool.ADD_X_SPIDER.name());
    JToggleButton edgeBtn = new JToggleButton("Add Edge");
    edgeBtn.setActionCommand(DiagramController.Tool.ADD_EDGE.name());
    JToggleButton hEdgeBtn = new JToggleButton("Add Hadamard Edge");
    hEdgeBtn.setActionCommand(DiagramController.Tool.ADD_HADAMARD_EDGE.name());

    toolGroup.add(selectBtn);
    toolGroup.add(zSpiderBtn);
    toolGroup.add(xSpiderBtn);
    toolGroup.add(edgeBtn);
    toolGroup.add(hEdgeBtn);

    ActionListener toolListener = e -> controller.setTool(DiagramController.Tool.valueOf(e.getActionCommand()));
    selectBtn.addActionListener(toolListener);
    zSpiderBtn.addActionListener(toolListener);
    xSpiderBtn.addActionListener(toolListener);
    edgeBtn.addActionListener(toolListener);
    hEdgeBtn.addActionListener(toolListener);

    add(selectBtn);
    add(zSpiderBtn);
    add(xSpiderBtn);
    add(edgeBtn);
    add(hEdgeBtn);
    addSeparator();

    JCheckBox showHadamardGateBox = new JCheckBox("Show Hadamard Gate");
    showHadamardGateBox.addActionListener(e -> controller.setShowHadamardGate(showHadamardGateBox.isSelected()));
    add(showHadamardGateBox);
    addSeparator();

    JButton convertBtn = new JButton("Convert to LMNtal");
    convertBtn.addActionListener(e -> controller.convertToLMNtal());
    add(convertBtn);

    JButton clearBtn = new JButton("Clear Graph");
    clearBtn.addActionListener(e -> controller.clearGraph());
    add(clearBtn);
  }
}
