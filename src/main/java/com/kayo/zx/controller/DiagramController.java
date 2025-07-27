package com.kayo.zx.controller;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import com.kayo.zx.converter.LMNtalConverter;
import com.kayo.zx.model.Edge;
import com.kayo.zx.model.EdgeType;
import com.kayo.zx.model.Spider;
import com.kayo.zx.model.SpiderType;
import com.kayo.zx.model.ZXGraph;
import com.kayo.zx.view.DrawingPanel;

public class DiagramController {
  public enum Tool {
    SELECT, ADD_Z_SPIDER, ADD_X_SPIDER, ADD_EDGE, ADD_HADAMARD_EDGE
  }

  private final ZXGraph graph;
  private final DrawingPanel panel;
  private JTextArea lmntalOutputArea;

  private Tool currentTool = Tool.SELECT;
  private boolean showHadamardGate = false;

  private Spider selectedSpider = null;
  private Spider edgeStartSpider = null;
  private Point lastMousePoint = null;
  private Point currentMousePoint = null;

  public DiagramController(ZXGraph graph, DrawingPanel panel) {
    this.graph = graph;
    this.panel = panel;
  }

  public void handleMousePress(MouseEvent e) {
    lastMousePoint = e.getPoint();
    selectedSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);

    switch (currentTool) {
      case ADD_Z_SPIDER -> {
        if (selectedSpider == null) {
          graph.addSpider(new Spider(e.getX(), e.getY(), SpiderType.Z));
        }
      }
      case ADD_X_SPIDER -> {
        if (selectedSpider == null) {
          graph.addSpider(new Spider(e.getX(), e.getY(), SpiderType.X));
        }
      }
      case ADD_EDGE, ADD_HADAMARD_EDGE -> {
        if (selectedSpider != null)
          edgeStartSpider = selectedSpider;
      }
      case SELECT -> {
      }
    }
    panel.repaint();
  }

  public void handleMouseDrag(MouseEvent e) {
    currentMousePoint = e.getPoint();
    if (currentTool == Tool.SELECT && selectedSpider != null && lastMousePoint != null) {
      int dx = e.getX() - lastMousePoint.x;
      int dy = e.getY() - lastMousePoint.y;
      selectedSpider.setLocation(selectedSpider.getX() + dx, selectedSpider.getY() + dy);
      lastMousePoint = e.getPoint();
    }
    panel.repaint();
  }

  public void handleMouseRelease(MouseEvent e) {
    if ((currentTool == Tool.ADD_EDGE || currentTool == Tool.ADD_HADAMARD_EDGE) && edgeStartSpider != null) {
      Spider endSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);
      if (endSpider != null && !endSpider.equals(edgeStartSpider)) {
        EdgeType type = (currentTool == Tool.ADD_HADAMARD_EDGE) ? EdgeType.HADAMARD : EdgeType.NORMAL;
        graph.addEdge(new Edge(edgeStartSpider, endSpider, type));
      }
    }
    selectedSpider = null;
    edgeStartSpider = null;
    lastMousePoint = null;
    currentMousePoint = null;
    panel.repaint();
  }

  public void handleMouseClick(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON3)
      handleRightClick(e);
    else if (e.getClickCount() == 2)
      handleDoubleClick(e);
  }

  private void handleRightClick(MouseEvent e) {
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

  private void handleDoubleClick(MouseEvent e) {
    Spider targetSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);
    if (targetSpider != null) {
      String newPhase = JOptionPane.showInputDialog(panel, "Enter a new phase:", targetSpider.getPhase());
      if (newPhase != null) {
        targetSpider.setPhase(newPhase);
        panel.repaint();
      }
    }
  }

  private void showSpiderContextMenu(Spider spider, Point p) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem deleteItem = new JMenuItem("Delete");
    deleteItem.addActionListener(e -> {
      graph.removeSpider(spider);
      panel.repaint();
    });
    menu.add(deleteItem);
    menu.show(panel, p.x, p.y);
  }

  private void showEdgeContextMenu(Edge edge, Point p) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem toggleHadamardItem = new JMenuItem("Switch Hadamard Gate");
    toggleHadamardItem.addActionListener(e -> {
      edge.setType(edge.getType() == EdgeType.NORMAL ? EdgeType.HADAMARD : EdgeType.NORMAL);
      panel.repaint();
    });
    JMenuItem deleteItem = new JMenuItem("Delete Edge");
    deleteItem.addActionListener(e -> {
      graph.removeEdge(edge);
      panel.repaint();
    });
    menu.add(toggleHadamardItem);
    menu.add(deleteItem);
    menu.show(panel, p.x, p.y);
  }

  public void setTool(Tool tool) {
    this.currentTool = tool;
  }

  public void setShowHadamardGate(boolean show) {
    this.showHadamardGate = show;
    panel.repaint();
  }

  public void clearGraph() {
    int result = JOptionPane.showConfirmDialog(panel, "Clear the entire graph?", "Confirm", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
      graph.clear();
      panel.repaint();
      convertToLMNtal();
    }
  }

  public void convertToLMNtal() {
    if (lmntalOutputArea != null) {
      String lmntalCode = LMNtalConverter.toLMNtal(graph);
      lmntalOutputArea.setText(lmntalCode);
    }
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

  public void setLmntalOutputArea(JTextArea area) {
    this.lmntalOutputArea = area;
  }
}
