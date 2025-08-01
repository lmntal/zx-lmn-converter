package com.kayo.zx.controller;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.kayo.zx.model.Edge;
import com.kayo.zx.model.EdgeType;
import com.kayo.zx.model.Spider;
import com.kayo.zx.model.SpiderType;
import com.kayo.zx.model.ZXGraph;
import com.kayo.zx.view.DrawingPanel;

public class DiagramController extends MouseAdapter {
  public enum Tool {
    ADD_Z_SPIDER, ADD_X_SPIDER, ADD_EDGE, ADD_HADAMARD_EDGE
  }

  private final DrawingPanel panel;
  private Tool currentTool = Tool.ADD_Z_SPIDER;
  private boolean showHadamardGate = false;

  private Spider spiderForRightDrag = null;
  private Spider edgeStartSpider = null;
  private Point lastMousePoint = null;
  private Point currentMousePoint = null;

  public DiagramController(DrawingPanel panel) {
    this.panel = panel;
    panel.addMouseListener(this);
    panel.addMouseMotionListener(this);
  }

  @Override
  public void mousePressed(MouseEvent e) {
    ZXGraph graph = panel.getGraph();
    if (graph == null)
      return;

    lastMousePoint = e.getPoint();
    Spider targetSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);

    if (SwingUtilities.isRightMouseButton(e)) {
      if (targetSpider != null) {
        spiderForRightDrag = targetSpider;
      }
    } else if (SwingUtilities.isLeftMouseButton(e)) {
      switch (currentTool) {
        case ADD_Z_SPIDER -> {
          if (targetSpider == null)
            graph.addSpider(new Spider(e.getX(), e.getY(), SpiderType.Z));
        }
        case ADD_X_SPIDER -> {
          if (targetSpider == null)
            graph.addSpider(new Spider(e.getX(), e.getY(), SpiderType.X));
        }
        case ADD_EDGE, ADD_HADAMARD_EDGE -> {
          if (targetSpider != null)
            edgeStartSpider = targetSpider;
        }
      }
    }
    panel.repaint();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    currentMousePoint = e.getPoint();
    if (spiderForRightDrag != null && lastMousePoint != null) {
      int dx = e.getX() - lastMousePoint.x;
      int dy = e.getY() - lastMousePoint.y;
      spiderForRightDrag.setLocation(spiderForRightDrag.getX() + dx, spiderForRightDrag.getY() + dy);
      lastMousePoint = e.getPoint();
    }
    panel.repaint();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    ZXGraph graph = panel.getGraph();
    if (graph == null)
      return;

    if (SwingUtilities.isLeftMouseButton(e)) {
      if ((currentTool == Tool.ADD_EDGE || currentTool == Tool.ADD_HADAMARD_EDGE) && edgeStartSpider != null) {
        Spider endSpider = graph.findSpiderAt(e.getX(), e.getY(), DrawingPanel.SPIDER_RADIUS);
        if (endSpider != null && !endSpider.equals(edgeStartSpider)) {
          EdgeType type = (currentTool == Tool.ADD_HADAMARD_EDGE) ? EdgeType.HADAMARD : EdgeType.NORMAL;
          graph.addEdge(new Edge(edgeStartSpider, endSpider, type));
        }
      }
    }

    spiderForRightDrag = null;
    edgeStartSpider = null;
    lastMousePoint = null;
    currentMousePoint = null;
    panel.repaint();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      handleRightClick(e);
    }
  }

  private void handleRightClick(MouseEvent e) {
    if (e.isConsumed() || (lastMousePoint != null && !e.getPoint().equals(lastMousePoint))) {
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

    JMenuItem phaseItem = new JMenuItem("Edit Phase");
    phaseItem.addActionListener(ev -> {
      String newPhase = JOptionPane.showInputDialog(panel, "Enter new phase:", spider.getPhase());
      if (newPhase != null) {
        spider.setPhase(newPhase);
        panel.repaint();
      }
    });

    JMenuItem deleteItem = new JMenuItem("Delete Spider");
    deleteItem.addActionListener(ev -> {
      if (graph != null) {
        graph.removeSpider(spider);
        panel.repaint();
      }
    });

    menu.add(phaseItem);
    menu.add(deleteItem);
    menu.show(panel, p.x, p.y);
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

  public void setTool(Tool tool) {
    this.currentTool = tool;
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
