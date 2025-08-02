package com.kayo.zx.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.kayo.zx.controller.AppController;
import com.kayo.zx.model.RuleType;
import com.kayo.zx.model.ZXRule;

public class RuleEditorPanel extends JPanel {
  private final DrawingPanel lhsPanel;
  private final DrawingPanel rhsPanel;
  private final JComboBox<RuleType> ruleTypeSelector;
  private ZXRule currentRule;

  public RuleEditorPanel(AppController controller) {
    this.lhsPanel = new DrawingPanel();
    this.rhsPanel = new DrawingPanel();

    setLayout(new BorderLayout(5, 5));
    setBorder(BorderFactory.createTitledBorder("Rule editor"));

    ruleTypeSelector = new JComboBox<>(RuleType.values());
    ruleTypeSelector.addActionListener(e -> {
      if (currentRule != null) {
        currentRule.setType((RuleType) ruleTypeSelector.getSelectedItem());
      }
    });

    JButton convertButton = new JButton("Convert/Save Rule");
    convertButton.addActionListener(e -> controller.convertAndSaveCurrentRule());

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    bottomPanel.add(new JLabel("Rule Type:"));
    bottomPanel.add(ruleTypeSelector);
    bottomPanel.add(Box.createHorizontalStrut(20));
    bottomPanel.add(convertButton);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lhsPanel, rhsPanel);
    splitPane.setResizeWeight(0.5);
    splitPane.setBorder(null);

    add(splitPane, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);
  }

  public void setRule(ZXRule rule) {
    this.currentRule = rule;
    lhsPanel.setGraph(rule.getLhs());
    rhsPanel.setGraph(rule.getRhs());
    ruleTypeSelector.setSelectedItem(rule.getType());
    lhsPanel.repaint();
    rhsPanel.repaint();
  }

  public DrawingPanel getLhsPanel() {
    return lhsPanel;
  }

  public DrawingPanel getRhsPanel() {
    return rhsPanel;
  }

  public JComboBox<RuleType> getRuleTypeSelector() {
    return ruleTypeSelector;
  }
}
