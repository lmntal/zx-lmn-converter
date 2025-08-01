package com.kayo.zx.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.kayo.zx.controller.AppController;

public class EditorPanel extends JPanel {
  private final DrawingPanel graphEditorPanel;
  private final RuleEditorPanel ruleEditorPanel;

  public EditorPanel(AppController controller) {
    this.graphEditorPanel = new DrawingPanel();
    this.ruleEditorPanel = new RuleEditorPanel(controller);

    setLayout(new BorderLayout());

    JPanel graphEditorContainer = new JPanel(new BorderLayout());
    graphEditorContainer.setBorder(BorderFactory.createTitledBorder("Graph Editor"));
    JButton convertGraphButton = new JButton("Convert Graph");
    convertGraphButton.addActionListener(e -> controller.convertAndSaveCurrentGraph());
    JPanel graphButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    graphButtonPanel.add(convertGraphButton);
    graphEditorContainer.add(graphEditorPanel, BorderLayout.CENTER);
    graphEditorContainer.add(graphButtonPanel, BorderLayout.SOUTH);

    JScrollPane ruleScrollPane = new JScrollPane(ruleEditorPanel);
    ruleScrollPane.setBorder(null);

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        graphEditorContainer,
        ruleScrollPane);
    splitPane.setResizeWeight(0.5);
    splitPane.setBorder(null);

    add(splitPane, BorderLayout.CENTER);
  }

  public DrawingPanel getGraphEditorPanel() {
    return graphEditorPanel;
  }

  public RuleEditorPanel getRuleEditorPanel() {
    return ruleEditorPanel;
  }
}
