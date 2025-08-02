package com.lmntal.zx.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

public class OutputPanel extends JPanel {
  private final JTextArea graphOutputArea;
  private final JTextArea ruleOutputArea;

  public OutputPanel() {
    setLayout(new BorderLayout());

    graphOutputArea = createTextArea();
    ruleOutputArea = createTextArea();

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        createTitledPanel("LMNtal graph", graphOutputArea),
        createTitledPanel("LMNtal rule", ruleOutputArea));
    splitPane.setResizeWeight(0.5);

    add(splitPane, BorderLayout.CENTER);
  }

  private JTextArea createTextArea() {
    JTextArea textArea = new JTextArea();
    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    textArea.setEditable(false);
    return textArea;
  }

  private JPanel createTitledPanel(String title, JComponent component) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    panel.add(new JScrollPane(component), BorderLayout.CENTER);
    return panel;
  }

  public JTextArea getGraphOutputArea() {
    return graphOutputArea;
  }

  public JTextArea getRuleOutputArea() {
    return ruleOutputArea;
  }
}
