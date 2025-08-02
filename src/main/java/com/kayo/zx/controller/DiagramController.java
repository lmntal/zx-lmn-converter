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
  private final DrawingPanel panel;
  private final boolean isRuleEditorContext;
  private SpiderType currentSpiderType = SpiderType.Z;
  private EdgeType currentEdgeType = EdgeType.NORMAL;
  private boolean showHadamardGate = false;

  private Spider spiderForRightDrag = null;
  private Spider edgeStartSpider = null;
  private Point pressPoint = null;
  private Point currentMousePoint = null;

  public DiagramController(DrawingPanel panel, boolean isRuleEditorContext) {
    this.panel = panel;
    this.isRuleEditorContext = isRuleEditorContext;
    panel.addMouseListener(this);
    panel.addMouseMotionListener(this);
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
          // Prohibit connecting two boundary spiders
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
    // Automatically generate a unique label for the boundary node
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
      // Boundary nodes now have auto-generated labels, editing might not be desired.
      // If editing is still needed, the logic to ensure uniqueness must be robust.
      // For now, we disable direct editing to prefer auto-labels.
    } else {
      JMenuItem toggleTypeItem = new JMenuItem("Toggle Spider Type (Z/X)");
      toggleTypeItem.addActionListener(ev -> {
        spider.setType(spider.getType() == SpiderType.Z ? SpiderType.X : SpiderType.Z);
        panel.repaint();
      });

      JMenuItem phaseItem = new JMenuItem("Edit Phase");
      phaseItem.addActionListener(ev -> {
        String newPhase = JOptionPane.showInputDialog(panel, "Enter new phase:", spider.getPhase());
        if (newPhase != null) {
          spider.setPhase(newPhase);
          panel.repaint();
        }
      });
      menu.add(toggleTypeItem);
      menu.add(phaseItem);
    }

    JMenuItem deleteItem = new JMenuItem("Delete Spider");
    deleteItem.addActionListener(ev -> {
      if (graph != null) {
        graph.removeSpider(spider);
        panel.repaint();
      }
    });

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
