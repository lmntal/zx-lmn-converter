package com.kayo.zx.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.kayo.zx.controller.DiagramController;
import com.kayo.zx.model.ZXGraph;

public class MainFrame extends JFrame {
  private final DrawingPanel drawingPanel;
  private final JTextArea lmntalOutput;
  private final DiagramController controller;

  public MainFrame() {
    setTitle("ZX <-> LMNtal Converter");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setLocationRelativeTo(null);

    ZXGraph graph = new ZXGraph();
    this.drawingPanel = new DrawingPanel(graph);
    this.controller = new DiagramController(graph, drawingPanel);
    this.drawingPanel.setController(controller);

    AppToolbar toolbar = new AppToolbar(controller);

    lmntalOutput = new JTextArea("// LMNtal Output will appear here\n");
    lmntalOutput.setEditable(false);
    lmntalOutput.setFont(new Font("Monospaced", Font.PLAIN, 14));
    JScrollPane outputScrollPane = new JScrollPane(lmntalOutput);
    outputScrollPane.setPreferredSize(new Dimension(300, 800));

    setLayout(new BorderLayout());
    add(toolbar, BorderLayout.NORTH);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, drawingPanel, outputScrollPane);
    splitPane.setDividerLocation(850);
    add(splitPane, BorderLayout.CENTER);

    controller.setLmntalOutputArea(lmntalOutput);
  }
}
