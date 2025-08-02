package com.lmntal.zx.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.lmntal.zx.model.NamedGraph;
import com.lmntal.zx.model.RuleType;
import com.lmntal.zx.model.Spider;
import com.lmntal.zx.model.SpiderType;
import com.lmntal.zx.model.ZXGraph;
import com.lmntal.zx.model.ZXRule;
import com.lmntal.zx.view.AppToolbar;
import com.lmntal.zx.view.EditorPanel;
import com.lmntal.zx.view.MainFrame;
import com.lmntal.zx.view.OutputPanel;
import com.lmntal.zx.view.SidebarPanel;

public class AppController {
  private final List<NamedGraph> graphs = new ArrayList<>();
  private final List<ZXRule> rules = new ArrayList<>();
  private final DefaultListModel<NamedGraph> graphListModel = new DefaultListModel<>();
  private final DefaultListModel<ZXRule> ruleListModel = new DefaultListModel<>();

  private NamedGraph currentGraphInEditor;
  private ZXRule currentRuleInEditor;
  private NamedGraph savedGraphState;
  private ZXRule savedRuleState;

  private final MainFrame mainFrame;
  private final SidebarPanel sidebarPanel;
  private final AppToolbar toolbar;
  private final EditorPanel editorPanel;
  private final OutputPanel outputPanel;

  public AppController() {
    this.editorPanel = new EditorPanel(this);
    this.sidebarPanel = new SidebarPanel();
    this.toolbar = new AppToolbar();
    this.outputPanel = new OutputPanel();
    this.mainFrame = new MainFrame();

    mainFrame.setupLayout(this);
    sidebarPanel.setController(this);

    NamedGraph initialGraph = new NamedGraph("graph_1");
    graphs.add(initialGraph);
    graphListModel.addElement(initialGraph);

    ZXRule initialRule = new ZXRule("rule_1");
    rules.add(initialRule);
    ruleListModel.addElement(initialRule);

    this.currentGraphInEditor = initialGraph;
    this.currentRuleInEditor = initialRule;
    this.savedGraphState = new NamedGraph(currentGraphInEditor);
    this.savedRuleState = new ZXRule(currentRuleInEditor);

    editorPanel.getGraphEditorPanel().setGraph(currentGraphInEditor);
    editorPanel.getRuleEditorPanel().setRule(currentRuleInEditor);

    DiagramController graphCtrl = new DiagramController(editorPanel.getGraphEditorPanel(), false);
    DiagramController lhsCtrl = new DiagramController(editorPanel.getRuleEditorPanel().getLhsPanel(), true);
    DiagramController rhsCtrl = new DiagramController(editorPanel.getRuleEditorPanel().getRhsPanel(), true);

    editorPanel.getGraphEditorPanel().setController(graphCtrl);
    editorPanel.getRuleEditorPanel().getLhsPanel().setController(lhsCtrl);
    editorPanel.getRuleEditorPanel().getRhsPanel().setController(rhsCtrl);
    toolbar.setControllers(graphCtrl, lhsCtrl, rhsCtrl);

    sidebarPanel.getGraphList().setSelectedIndex(0);
    sidebarPanel.getRuleList().setSelectedIndex(0);
    updateAllLMNtalOutput();
  }

  public void initialize() {
    mainFrame.setupLayout(this);
  }

  public void showFrame() {
    mainFrame.setVisible(true);
  }

  public void selectGraph(NamedGraph graphToSelect) {
    if (graphToSelect == null || graphToSelect.equals(currentGraphInEditor))
      return;
    if (!checkForAllUnsavedChanges()) {
      sidebarPanel.getGraphList().setSelectedValue(currentGraphInEditor, true);
      return;
    }
    this.currentGraphInEditor = graphToSelect;
    editorPanel.getGraphEditorPanel().setGraph(graphToSelect);
    savedGraphState = new NamedGraph(graphToSelect);
    outputPanel.getRuleOutputArea().setText("");
    convertAndSaveCurrentGraph();
    editorPanel.getGraphEditorPanel().repaint();
  }

  public void selectRule(ZXRule ruleToSelect) {
    if (ruleToSelect == null || ruleToSelect.equals(currentRuleInEditor))
      return;
    if (!checkForAllUnsavedChanges()) {
      sidebarPanel.getRuleList().setSelectedValue(currentRuleInEditor, true);
      return;
    }
    this.currentRuleInEditor = ruleToSelect;
    editorPanel.getRuleEditorPanel().setRule(ruleToSelect);
    savedRuleState = new ZXRule(ruleToSelect);
    outputPanel.getGraphOutputArea().setText("");
    convertAndSaveCurrentRule();
    editorPanel.getRuleEditorPanel().repaint();
  }

  private boolean checkForUnsavedGraphChanges() {
    ZXGraph currentGraphFromPanel = editorPanel.getGraphEditorPanel().getGraph();
    boolean graphChanged = !currentGraphFromPanel.isIdenticalTo(savedGraphState);
    if (graphChanged) {
      int result = showUnsavedDialog("Graph '" + savedGraphState.getName() + "'");
      if (result == JOptionPane.YES_OPTION) {
        convertAndSaveCurrentGraph();
      } else if (result == JOptionPane.CANCEL_OPTION) {
        return false;
      }
    }
    return true;
  }

  private boolean checkForUnsavedRuleChanges() {
    ZXRule currentRuleFromPanel = new ZXRule(currentRuleInEditor.getName());
    currentRuleFromPanel.getLhs().setData(editorPanel.getRuleEditorPanel().getLhsPanel().getGraph());
    currentRuleFromPanel.getRhs().setData(editorPanel.getRuleEditorPanel().getRhsPanel().getGraph());
    currentRuleFromPanel.setType((RuleType) editorPanel.getRuleEditorPanel().getRuleTypeSelector().getSelectedItem());
    boolean ruleChanged = !currentRuleFromPanel.isIdenticalTo(savedRuleState);

    if (ruleChanged) {
      int result = showUnsavedDialog("Rule '" + savedRuleState.getName() + "'");
      if (result == JOptionPane.YES_OPTION) {
        convertAndSaveCurrentRule();
      } else if (result == JOptionPane.CANCEL_OPTION) {
        return false;
      }
    }
    return true;
  }

  private boolean checkForAllUnsavedChanges() {
    if (!checkForUnsavedGraphChanges()) {
      return false;
    }
    return checkForUnsavedRuleChanges();
  }

  private int showUnsavedDialog(String itemName) {
    return JOptionPane.showConfirmDialog(mainFrame,
        itemName + " has unsaved changes.\nDo you want to save them?",
        "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
  }

  public void createNewGraph() {
    if (!checkForUnsavedGraphChanges())
      return;
    String name = JOptionPane.showInputDialog(mainFrame, "Enter new graph name:", "graph_" + (graphs.size() + 1));
    if (name != null && !name.trim().isEmpty()) {
      NamedGraph newGraph = new NamedGraph(name);
      graphs.add(newGraph);
      graphListModel.addElement(newGraph);
      sidebarPanel.getGraphList().setSelectedValue(newGraph, true);
    }
  }

  public void createNewRule() {
    if (!checkForUnsavedRuleChanges())
      return;
    String name = JOptionPane.showInputDialog(mainFrame, "Enter new rule name:", "rule_" + (rules.size() + 1));
    if (name != null && !name.trim().isEmpty()) {
      ZXRule newRule = new ZXRule(name);
      rules.add(newRule);
      ruleListModel.addElement(newRule);
      sidebarPanel.getRuleList().setSelectedValue(newRule, true);
    }
  }

  public void deleteGraph(NamedGraph graph) {
    if (graph == null)
      return;
    int choice = JOptionPane.showConfirmDialog(mainFrame, "Delete graph '" + graph.getName() + "'?", "Confirm",
        JOptionPane.YES_NO_OPTION);
    if (choice == JOptionPane.YES_OPTION) {
      int selectedIdx = graphListModel.indexOf(graph);
      graphs.remove(graph);
      graphListModel.removeElement(graph);

      if (!graphs.isEmpty()) {
        int newSelection = Math.max(0, selectedIdx - 1);
        sidebarPanel.getGraphList().setSelectedIndex(newSelection);
      } else {
        createNewGraph();
      }
    }
  }

  public void deleteRule(ZXRule rule) {
    if (rule == null)
      return;
    int choice = JOptionPane.showConfirmDialog(mainFrame, "Delete rule '" + rule.getName() + "'?", "Confirm",
        JOptionPane.YES_NO_OPTION);
    if (choice == JOptionPane.YES_OPTION) {
      int selectedIdx = ruleListModel.indexOf(rule);
      rules.remove(rule);
      ruleListModel.removeElement(rule);
      if (!rules.isEmpty()) {
        int newSelection = Math.max(0, selectedIdx - 1);
        sidebarPanel.getRuleList().setSelectedIndex(newSelection);
      } else {
        createNewRule();
      }
    }
  }

  public void convertAndSaveCurrentGraph() {
    if (currentGraphInEditor != null) {
      savedGraphState.setData(currentGraphInEditor);
      outputPanel.getGraphOutputArea().setText(currentGraphInEditor.toLMNtal() + ".");
      outputPanel.getGraphOutputArea().setCaretPosition(0);
    }
  }

  private boolean validateRule(ZXRule rule) {
    // === START: New Validation Logic ===
    // Validate uniqueness of variable labels within LHS and RHS
    List<String> lhsVariableLabels = rule.getLhs().getSpiders().stream()
        .map(Spider::getVariableLabel)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (new HashSet<>(lhsVariableLabels).size() < lhsVariableLabels.size()) {
      JOptionPane.showMessageDialog(mainFrame, "Variable labels must be unique on the left side of the rule.",
          "Rule Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    List<String> rhsVariableLabels = rule.getRhs().getSpiders().stream()
        .map(Spider::getVariableLabel)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (new HashSet<>(rhsVariableLabels).size() < rhsVariableLabels.size()) {
      JOptionPane.showMessageDialog(mainFrame, "Variable labels must be unique on the right side of the rule.",
          "Rule Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Validate uniqueness of boundary labels within LHS and RHS
    List<String> lhsBoundaryLabelList = rule.getLhs().getSpiders().stream()
        .filter(s -> s.getType() == SpiderType.BOUNDARY)
        .map(Spider::getLabel)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (new HashSet<>(lhsBoundaryLabelList).size() < lhsBoundaryLabelList.size()) {
      JOptionPane.showMessageDialog(mainFrame, "Boundary labels must be unique on the left side of the rule.",
          "Rule Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    List<String> rhsBoundaryLabelList = rule.getRhs().getSpiders().stream()
        .filter(s -> s.getType() == SpiderType.BOUNDARY)
        .map(Spider::getLabel)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    if (new HashSet<>(rhsBoundaryLabelList).size() < rhsBoundaryLabelList.size()) {
      JOptionPane.showMessageDialog(mainFrame, "Boundary labels must be unique on the right side of the rule.",
          "Rule Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }
    // === END: New Validation Logic ===

    // Boundary node validation
    Set<String> lhsBoundaryLabels = new HashSet<>(lhsBoundaryLabelList);
    Set<String> rhsBoundaryLabels = new HashSet<>(rhsBoundaryLabelList);

    if (!lhsBoundaryLabels.equals(rhsBoundaryLabels)) {
      JOptionPane.showMessageDialog(mainFrame, "Boundary node labels must match on both sides of the rule.",
          "Rule Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    // Variable validation
    Set<String> lhsVars = new HashSet<>();
    rule.getLhs().toLMNtal(lhsVars);
    Set<String> rhsVars = new HashSet<>();
    rule.getRhs().toLMNtal(rhsVars);

    Set<String> missingInLhs = new HashSet<>(rhsVars);
    missingInLhs.removeAll(lhsVars);
    if (!missingInLhs.isEmpty()) {
      JOptionPane.showMessageDialog(mainFrame,
          "Validation Error in rule '" + rule.getName() + "':\n" +
              "The following variables from the RHS are missing in the LHS:\n" +
              String.join(", ", missingInLhs),
          "Rule Validation Error", JOptionPane.ERROR_MESSAGE);
      return false;
    }

    if (rule.getType() == RuleType.EQUALS) {
      Set<String> missingInRhs = new HashSet<>(lhsVars);
      missingInRhs.removeAll(rhsVars);
      if (!missingInRhs.isEmpty()) {
        JOptionPane.showMessageDialog(mainFrame,
            "Validation Error in rule '" + rule.getName() + "' (reverse direction):\n" +
                "The following variables from the RHS are missing in the LHS:\n" +
                String.join(", ", missingInRhs),
            "Rule Validation Error", JOptionPane.ERROR_MESSAGE);
        return false;
      }
    }
    return true;
  }

  public void convertAndSaveCurrentRule() {
    if (currentRuleInEditor != null) {
      currentRuleInEditor.setType((RuleType) editorPanel.getRuleEditorPanel().getRuleTypeSelector().getSelectedItem());
      if (!validateRule(currentRuleInEditor)) {
        return;
      }
      savedRuleState.setData(currentRuleInEditor);
      outputPanel.getRuleOutputArea().setText(currentRuleInEditor.toLMNtal());
      outputPanel.getRuleOutputArea().setCaretPosition(0);
    }
  }

  private void updateAllLMNtalOutput() {
    if (currentGraphInEditor == null || currentRuleInEditor == null) {
      outputPanel.getGraphOutputArea().setText("// No graph selected.");
      outputPanel.getRuleOutputArea().setText("// No rule selected.");
      return;
    }
    if (currentGraphInEditor.isEmpty()) {
      outputPanel.getGraphOutputArea().setText("// " + currentGraphInEditor.getName() + " is empty.");
    } else {
      outputPanel.getGraphOutputArea().setText(currentGraphInEditor.toLMNtal() + ".");
    }
    if (currentRuleInEditor.isEmpty()) {
      outputPanel.getRuleOutputArea().setText("// " + currentRuleInEditor.getName() + " is empty.");
    } else {
      outputPanel.getRuleOutputArea().setText(currentRuleInEditor.toLMNtal());
    }
  }

  public void exportToFile() {
    if (!checkForAllUnsavedChanges())
      return;

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export as LMNtal file");
    fileChooser.setFileFilter(new FileNameExtensionFilter("LMNtal files (*.lmn)", "lmn"));

    if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
      File fileToSave = fileChooser.getSelectedFile();
      if (!fileToSave.getName().toLowerCase().endsWith(".lmn")) {
        fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".lmn");
      }

      StringBuilder exportContent = new StringBuilder();
      StringBuilder errorMessages = new StringBuilder();

      exportContent.append("// === Graph Definitions ===\n\n");
      for (NamedGraph graph : graphs) {
        if (!graph.isEmpty()) {
          exportContent.append(String.format("// %s\n%s.\n\n", graph.getName(), graph.toLMNtal()));
        }
      }

      exportContent.append("// === Rule Definitions ===\n\n");
      for (ZXRule rule : rules) {
        if (rule.isEmpty()) {
          continue;
        }

        if (validateRule(rule)) {
          exportContent.append(String.format("%s\n\n", rule.toLMNtal()));
        } else {
          errorMessages.append("Rule '").append(rule.getName())
              .append("' was not exported because it failed validation (boundary nodes or variables).\n");
        }
      }

      try (FileWriter writer = new FileWriter(fileToSave)) {
        writer.write(exportContent.toString());

        String successMessage = "File exported successfully:\n" + fileToSave.getAbsolutePath();
        if (errorMessages.length() > 0) {
          JOptionPane.showMessageDialog(mainFrame,
              successMessage + "\n\nHowever, some rules could not be exported:\n" + errorMessages.toString(),
              "Export Complete with Warnings", JOptionPane.WARNING_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(mainFrame, successMessage, "Export Successful",
              JOptionPane.INFORMATION_MESSAGE);
        }

      } catch (IOException e) {
        JOptionPane.showMessageDialog(mainFrame, "Error occurred during export: " + e.getMessage(), "Export Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public SidebarPanel getSidebarPanel() {
    return sidebarPanel;
  }

  public AppToolbar getToolbar() {
    return toolbar;
  }

  public EditorPanel getEditorPanel() {
    return editorPanel;
  }

  public OutputPanel getOutputPanel() {
    return outputPanel;
  }

  public DefaultListModel<NamedGraph> getGraphListModel() {
    return graphListModel;
  }

  public DefaultListModel<ZXRule> getRuleListModel() {
    return ruleListModel;
  }
}
