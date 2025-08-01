package com.kayo.zx.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import com.kayo.zx.controller.AppController;
import com.kayo.zx.model.NamedGraph;
import com.kayo.zx.model.ZXRule;

public class SidebarPanel extends JPanel {
  private final JList<NamedGraph> graphList;
  private final JList<ZXRule> ruleList;

  public SidebarPanel() {
    setLayout(new BorderLayout());
    graphList = new JList<>();
    ruleList = new JList<>();
  }

  public void setController(AppController controller) {
    graphList.setModel(controller.getGraphListModel());
    graphList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    graphList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && graphList.getSelectedValue() != null) {
        controller.selectGraph(graphList.getSelectedValue());
      }
    });

    ruleList.setModel(controller.getRuleListModel());
    ruleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ruleList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && ruleList.getSelectedValue() != null) {
        controller.selectRule(ruleList.getSelectedValue());
      }
    });

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        createTitledPanel("Graphs", graphList),
        createTitledPanel("Rules", ruleList));
    splitPane.setResizeWeight(0.5);

    add(splitPane, BorderLayout.CENTER);
    add(createButtonPanel(controller), BorderLayout.SOUTH);
  }

  private JPanel createTitledPanel(String title, JComponent component) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    panel.add(new JScrollPane(component), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createButtonPanel(AppController controller) {
    JPanel panel = new JPanel(new GridLayout(1, 3, 2, 2));
    JButton newGraphBtn = new JButton("New Graph");
    newGraphBtn.addActionListener(e -> controller.createNewGraph());
    JButton newRuleBtn = new JButton("New Rule");
    newRuleBtn.addActionListener(e -> controller.createNewRule());
    JButton deleteBtn = new JButton("Delete");
    deleteBtn.addActionListener(e -> controller.deleteCurrentSelection());

    panel.add(newGraphBtn);
    panel.add(newRuleBtn);
    panel.add(deleteBtn);
    return panel;
  }

  public JList<NamedGraph> getGraphList() {
    return graphList;
  }

  public JList<ZXRule> getRuleList() {
    return ruleList;
  }
}
