package com.lmntal.zx.controller;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.lmntal.zx.model.Edge;
import com.lmntal.zx.model.EdgeType;
import com.lmntal.zx.model.Spider;
import com.lmntal.zx.model.SpiderType;
import com.lmntal.zx.model.ZXGraph;
import com.lmntal.zx.view.DrawingPanel;

public class DiagramController extends MouseAdapter {
  private final DrawingPanel panel;
  private final boolean isRuleEditorContext;
  private SpiderType currentSpiderType = SpiderType.Z;
  private EdgeType currentEdgeType = EdgeType.NORMAL;
  private boolean showHadamardGate = false;

  private Spider spiderForRightDrag = null;
  private Spider edgeStartSpider = null;
  private Point pressPoint = null;
  private Point currentMousePoint = null;
  private ZXGraph otherGraph = null;

  public DiagramController(DrawingPanel panel, boolean isRuleEditorContext) {
    this.panel = panel;
    this.isRuleEditorContext = isRuleEditorContext;
    panel.addMouseListener(this);
    panel.addMouseMotionListener(this);
  }

  public void setOtherGraph(ZXGraph otherGraph) {
    this.otherGraph = otherGraph;
  }

  @Override
  public void mousePressed(MouseEvent e) {
    ZXGraph graph = panel.getGraph();
    if (graph == null)
      return;

    pressPoint = e.getPoint();
    Spider targetSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);

    if (SwingUtilities.isRightMouseButton(e)) {
      if (targetSpider != null) {
        spiderForRightDrag = targetSpider;
      }
    } else if (SwingUtilities.isLeftMouseButton(e)) {
      if (targetSpider != null) {
        edgeStartSpider = targetSpider;
      }
    }
    panel.repaint();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    currentMousePoint = e.getPoint();
    if (spiderForRightDrag != null && pressPoint != null) {
      int dx = e.getX() - pressPoint.x;
      int dy = e.getY() - pressPoint.y;
      spiderForRightDrag.setLocation(spiderForRightDrag.getX() + dx, spiderForRightDrag.getY() + dy);
      pressPoint = e.getPoint();
    }
    panel.repaint();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    ZXGraph graph = panel.getGraph();
    if (graph == null || pressPoint == null)
      return;

    boolean isClick = pressPoint.distance(e.getPoint()) < 5;

    if (SwingUtilities.isLeftMouseButton(e)) {
      Spider targetSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);

      if (isClick) {
        if (targetSpider == null) {
          if (currentSpiderType == SpiderType.BOUNDARY) {
            if (!isRuleEditorContext) {
              JOptionPane.showMessageDialog(panel, "Boundary nodes can only be added in the rule editor.", "Info",
                  JOptionPane.INFORMATION_MESSAGE);
            } else {
              addBoundarySpider(e.getX(), e.getY());
            }
          } else {
            graph.addSpider(new Spider(e.getX(), e.getY(), currentSpiderType));
          }
        }
      } else { // It's a drag
        if (edgeStartSpider != null && targetSpider != null && !targetSpider.equals(edgeStartSpider)) {
          if (edgeStartSpider.getType() == SpiderType.BOUNDARY && targetSpider.getType() == SpiderType.BOUNDARY) {
            JOptionPane.showMessageDialog(panel, "Cannot connect two boundary spiders.", "Error",
                JOptionPane.ERROR_MESSAGE);
          } else {
            graph.addEdge(new Edge(edgeStartSpider, targetSpider, currentEdgeType));
          }
        }
      }
    }

    spiderForRightDrag = null;
    edgeStartSpider = null;
    pressPoint = null;
    currentMousePoint = null;
    panel.repaint();
  }

  private void addBoundarySpider(int x, int y) {
    String label = panel.getGraph().generateUniqueBoundaryLabel();
    Spider boundarySpider = new Spider(x, y, SpiderType.BOUNDARY);
    boundarySpider.setLabel(label);
    panel.getGraph().addSpider(boundarySpider);
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      handleRightClick(e);
    }
  }

  private void handleRightClick(MouseEvent e) {
    if (e.isConsumed() || (pressPoint != null && pressPoint.distance(e.getPoint()) >= 5)) {
      return;
    }

    ZXGraph graph = panel.getGraph();
    if (graph == null)
      return;

    Spider targetSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);
    if (targetSpider != null) {
      showSpiderContextMenu(targetSpider, e.getPoint());
      return;
    }
    Edge targetEdge = graph.findEdgeAt(e.getX(), e.getY(), 5);
    if (targetEdge != null) {
      showEdgeContextMenu(targetEdge, e.getPoint());
    }
  }

  private void showSpiderContextMenu(Spider spider, Point p) {
    JPopupMenu menu = new JPopupMenu();
    ZXGraph graph = panel.getGraph();

    if (spider.getType() == SpiderType.BOUNDARY) {
      if (isRuleEditorContext) {
        JMenuItem editLabelItem = new JMenuItem("Edit Label");
        editLabelItem.addActionListener(ev -> {
          Set<String> existingLabels = new HashSet<>();
          if (otherGraph != null) {
            existingLabels.addAll(getBoundaryLabelsFromGraph(otherGraph));
          }
          existingLabels.addAll(getBoundaryLabelsFromGraph(graph));
          if (spider.getLabel() != null) {
            existingLabels.remove(spider.getLabel());
          }

          List<String> options = new ArrayList<>(existingLabels.stream().sorted().toList());
          options.add(0, "New Label");

          String selected = (String) JOptionPane.showInputDialog(panel, "Choose a boundary label:", "Set Label",
              JOptionPane.PLAIN_MESSAGE, null, options.toArray(),
              spider.getLabel() != null && !spider.getLabel().isEmpty() ? spider.getLabel() : "New Label");

          if (selected != null) {
            if ("New Label".equals(selected)) {
              spider.setLabel(panel.getGraph().generateUniqueBoundaryLabel());
            } else {
              spider.setLabel(selected);
            }
            panel.repaint();
          }
        });
        menu.add(editLabelItem);
      }
    } else { // Z or X Spider
      if (isRuleEditorContext) {
        JMenuItem toggleTypeItem = new JMenuItem("Toggle Spider Type (Z/X)");
        toggleTypeItem.addActionListener(ev -> {
          spider.setType(spider.getType() == SpiderType.Z ? SpiderType.X : SpiderType.Z);
          if (spider.isColorUndefined()) {
            spider.setColorUndefined(false);
            updateVariableLabel(spider);
          }
          panel.repaint();
        });

        JMenuItem phaseItem = new JMenuItem("Edit Phase");
        phaseItem.addActionListener(ev -> {
          String currentPhase = spider.isPhaseUndefined() ? "" : spider.getPhase();
          String newPhase = JOptionPane.showInputDialog(panel, "Enter new phase:", currentPhase);
          if (newPhase != null) {
            spider.setPhase(newPhase);
            if (newPhase.equals("?") && spider.isUndefined()) {
              updateVariableLabel(spider);
            }
            panel.repaint();
          }
        });
        menu.add(toggleTypeItem);
        menu.add(phaseItem);

        menu.addSeparator();

        JMenuItem toggleUndefinedColorItem = new JMenuItem("Toggle Undefined Color");
        toggleUndefinedColorItem.addActionListener(ev -> {
          spider.setColorUndefined(!spider.isColorUndefined());
          updateVariableLabel(spider);
          panel.repaint();
        });

        JMenuItem setUndefinedPhaseItem = new JMenuItem("Set Phase Undefined (?)");
        setUndefinedPhaseItem.addActionListener(ev -> {
          if (!spider.isPhaseUndefined()) {
            spider.setPhase("?");
            updateVariableLabel(spider);
            panel.repaint();
          }
        });
        menu.add(toggleUndefinedColorItem);
        menu.add(setUndefinedPhaseItem);

        JMenuItem setVarLabelItem = new JMenuItem("Set Variable Label");
        setVarLabelItem.addActionListener(ev -> {
          if (!spider.isUndefined()) {
            JOptionPane.showMessageDialog(panel, "Spider must be undefined (color or phase) to have a variable label.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
          }
          Set<String> existingLabels = new HashSet<>();
          if (otherGraph != null) {
            existingLabels.addAll(getVariableLabelsFromGraph(otherGraph));
          }
          existingLabels.addAll(getVariableLabelsFromGraph(graph));

          List<String> options = new ArrayList<>(existingLabels.stream().sorted().toList());
          options.add(0, "New Variable");

          String selected = (String) JOptionPane.showInputDialog(panel, "Choose a variable label:", "Set Variable",
              JOptionPane.PLAIN_MESSAGE, null, options.toArray(),
              spider.getVariableLabel() != null ? spider.getVariableLabel() : "New Variable");

          if (selected != null) {
            if ("New Variable".equals(selected)) {
              spider.setVariableLabel(panel.getGraph().generateUniqueVariableLabel());
            } else {
              spider.setVariableLabel(selected);
            }
            panel.repaint();
          }
        });
        menu.add(setVarLabelItem);
      }
    }

    JMenuItem deleteItem = new JMenuItem("Delete Spider");
    deleteItem.addActionListener(ev -> {
      if (graph != null) {
        graph.removeSpider(spider);
        panel.repaint();
      }
    });

    menu.addSeparator();
    menu.add(deleteItem);
    menu.show(panel, p.x, p.y);
  }

  private Set<String> getVariableLabelsFromGraph(ZXGraph graph) {
    return graph.getSpiders().stream()
        .map(Spider::getVariableLabel)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private Set<String> getBoundaryLabelsFromGraph(ZXGraph graph) {
    return graph.getSpiders().stream()
        .filter(s -> s.getType() == SpiderType.BOUNDARY && s.getLabel() != null && !s.getLabel().isEmpty())
        .map(Spider::getLabel)
        .collect(Collectors.toSet());
  }

  private void updateVariableLabel(Spider spider) {
    if (!isRuleEditorContext)
      return;

    if (spider.isUndefined()) {
      if (spider.getVariableLabel() == null || spider.getVariableLabel().isEmpty()) {
        spider.setVariableLabel(panel.getGraph().generateUniqueVariableLabel());
      }
    } else {
      spider.setVariableLabel(null);
    }
  }

  private void showEdgeContextMenu(Edge edge, Point p) {
    JPopupMenu menu = new JPopupMenu();
    ZXGraph graph = panel.getGraph();

    JMenuItem toggleHadamardItem = new JMenuItem("Toggle Hadamard");
    toggleHadamardItem.addActionListener(ev -> {
      edge.setType(edge.getType() == EdgeType.NORMAL ? EdgeType.HADAMARD : EdgeType.NORMAL);
      panel.repaint();
    });
    JMenuItem deleteItem = new JMenuItem("Delete Edge");
    deleteItem.addActionListener(ev -> {
      if (graph != null) {
        graph.removeEdge(edge);
        panel.repaint();
      }
    });
    menu.add(toggleHadamardItem);
    menu.add(deleteItem);
    menu.show(panel, p.x, p.y);
  }

  public void setSpiderType(SpiderType type) {
    this.currentSpiderType = type;
  }

  public void setEdgeType(EdgeType type) {
    this.currentEdgeType = type;
  }

  public void setShowHadamardGate(boolean show) {
    this.showHadamardGate = show;
    panel.repaint();
  }

  public boolean isShowHadamardGate() {
    return showHadamardGate;
  }

  public Spider getEdgeStartSpider() {
    return edgeStartSpider;
  }

  public Point getCurrentMousePoint() {
    return currentMousePoint;
  }
}
