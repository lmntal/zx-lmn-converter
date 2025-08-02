package com.lmntal.zx.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import com.lmntal.zx.controller.AppController;
import com.lmntal.zx.model.NamedGraph;
import com.lmntal.zx.model.ZXRule;

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
        ruleList.clearSelection();
        controller.selectGraph(graphList.getSelectedValue());
      }
    });
    graphList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          int index = graphList.locationToIndex(e.getPoint());
          if (index != -1 && graphList.getCellBounds(index, index).contains(e.getPoint())) {
            graphList.setSelectedIndex(index);
            showGraphContextMenu(e, controller);
          }
        }
      }
    });

    ruleList.setModel(controller.getRuleListModel());
    ruleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    ruleList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && ruleList.getSelectedValue() != null) {
        graphList.clearSelection();
        controller.selectRule(ruleList.getSelectedValue());
      }
    });
    ruleList.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          int index = ruleList.locationToIndex(e.getPoint());
          if (index != -1 && ruleList.getCellBounds(index, index).contains(e.getPoint())) {
            ruleList.setSelectedIndex(index);
            showRuleContextMenu(e, controller);
          }
        }
      }
    });

    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        createTitledPanel("Graphs", graphList),
        createTitledPanel("Rules", ruleList));
    splitPane.setResizeWeight(0.5);

    add(splitPane, BorderLayout.CENTER);
    add(createButtonPanel(controller), BorderLayout.SOUTH);
  }

  private void showGraphContextMenu(MouseEvent e, AppController controller) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem deleteItem = new JMenuItem("Delete Graph");
    deleteItem.addActionListener(ae -> controller.deleteGraph(graphList.getSelectedValue()));
    menu.add(deleteItem);
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  private void showRuleContextMenu(MouseEvent e, AppController controller) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem deleteItem = new JMenuItem("Delete Rule");
    deleteItem.addActionListener(ae -> controller.deleteRule(ruleList.getSelectedValue()));
    menu.add(deleteItem);
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  private JPanel createTitledPanel(String title, JComponent component) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder(title));
    panel.add(new JScrollPane(component), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createButtonPanel(AppController controller) {
    JPanel panel = new JPanel(new GridLayout(1, 2, 2, 2));
    JButton newGraphBtn = new JButton("New Graph");
    newGraphBtn.addActionListener(e -> controller.createNewGraph());
    JButton newRuleBtn = new JButton("New Rule");
    newRuleBtn.addActionListener(e -> controller.createNewRule());

    panel.add(newGraphBtn);
    panel.add(newRuleBtn);
    return panel;
  }

  public JList<NamedGraph> getGraphList() {
    return graphList;
  }

  public JList<ZXRule> getRuleList() {
    return ruleList;
  }
}
