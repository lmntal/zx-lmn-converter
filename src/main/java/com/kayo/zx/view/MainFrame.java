package com.kayo.zx.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.kayo.zx.controller.AppController;

public class MainFrame extends JFrame {
  public MainFrame() {
    setTitle("ZX <-> LMNtal Converter");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1600, 1000);
    setLocationRelativeTo(null);
  }

  public void setupLayout(AppController controller) {
    setLayout(new BorderLayout(5, 5));

    JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    leftSplit.setLeftComponent(controller.getSidebarPanel());
    leftSplit.setRightComponent(controller.getEditorPanel());
    leftSplit.setDividerLocation(220);

    mainSplit.setLeftComponent(leftSplit);
    mainSplit.setRightComponent(controller.getOutputPanel());
    mainSplit.setDividerLocation(1200);

    add(controller.getToolbar(), BorderLayout.NORTH);
    add(mainSplit, BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton exportButton = new JButton("ファイルにエクスポート (.lmn)");
    exportButton.addActionListener(e -> controller.exportToFile());
    bottomPanel.add(exportButton);
    add(bottomPanel, BorderLayout.SOUTH);
  }
}
