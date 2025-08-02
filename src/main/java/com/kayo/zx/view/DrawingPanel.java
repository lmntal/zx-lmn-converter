package com.kayo.zx.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.kayo.zx.controller.DiagramController;
import com.kayo.zx.model.Edge;
import com.kayo.zx.model.EdgeType;
import com.kayo.zx.model.Spider;
import com.kayo.zx.model.SpiderType;
import com.kayo.zx.model.ZXGraph;

public class DrawingPanel extends JPanel {
  public static final int SPIDER_RADIUS = 15;
  private static final Color Z_SPIDER_COLOR = new Color(180, 255, 180);
  private static final Color X_SPIDER_COLOR = new Color(255, 180, 180);
  private static final Color HADAMARD_GATE_COLOR = new Color(255, 255, 150);
  private static final Stroke NORMAL_EDGE_STROKE = new BasicStroke(2f);
  private static final Stroke HADAMARD_EDGE_STROKE = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
      10.0f, new float[] { 5.0f }, 0.0f);
  private static final Stroke BOUNDARY_SPIDER_STROKE = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
      10.0f, new float[] { 3.0f }, 0.0f);
  private static final Color HADAMARD_EDGE_COLOR = Color.BLUE;

  private ZXGraph graph;
  private DiagramController controller;

  public DrawingPanel() {
    this.graph = new ZXGraph(); // Start with an empty, non-null graph
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
  }

  public void setController(DiagramController controller) {
    this.controller = controller;
  }

  public ZXGraph getGraph() {
    return graph;
  }

  public void setGraph(ZXGraph graph) {
    this.graph = graph;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (graph == null)
      return;
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    graph.getEdges().forEach(edge -> drawEdge(g2d, edge));
    if (controller != null && controller.getEdgeStartSpider() != null && controller.getCurrentMousePoint() != null) {
      Spider start = controller.getEdgeStartSpider();
      Point end = controller.getCurrentMousePoint();
      g2d.setColor(Color.GRAY);
      g2d.setStroke(HADAMARD_EDGE_STROKE);
      g2d.drawLine(start.getX(), start.getY(), end.x, end.y);
    }
    graph.getSpiders().forEach(spider -> drawSpider(g2d, spider));
  }

  private void drawSpider(Graphics2D g2d, Spider spider) {
    int r = SPIDER_RADIUS;
    int x = spider.getX() - r;
    int y = spider.getY() - r;

    if (spider.getType() == SpiderType.BOUNDARY) {
      g2d.setColor(Color.BLACK);
      g2d.setStroke(BOUNDARY_SPIDER_STROKE);
      g2d.draw(new Ellipse2D.Double(x, y, 2 * r, 2 * r));
      if (spider.getLabel() != null && !spider.getLabel().isEmpty()) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int stringWidth = fm.stringWidth(spider.getLabel());
        g2d.drawString(spider.getLabel(), spider.getX() - stringWidth / 2, spider.getY() + fm.getAscent() / 2);
      }
      return;
    }

    g2d.setColor(spider.getType() == SpiderType.Z ? Z_SPIDER_COLOR : X_SPIDER_COLOR);
    g2d.fill(new Ellipse2D.Double(x, y, 2 * r, 2 * r));
    g2d.setColor(Color.BLACK);
    g2d.setStroke(NORMAL_EDGE_STROKE);
    g2d.draw(new Ellipse2D.Double(x, y, 2 * r, 2 * r));
    if (!"0".equals(spider.getPhase()) && spider.getPhase() != null && !spider.getPhase().isEmpty()) {
      g2d.setColor(Color.BLACK);
      g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
      FontMetrics fm = g2d.getFontMetrics();
      int stringWidth = fm.stringWidth(spider.getPhase());
      g2d.drawString(spider.getPhase(), spider.getX() - stringWidth / 2, spider.getY() + fm.getAscent() / 2);
    }
  }

  private void drawEdge(Graphics2D g2d, Edge edge) {
    Point p1 = edge.getSource().getLocation();
    Point p2 = edge.getTarget().getLocation();
    if (edge.getType() == EdgeType.HADAMARD && controller != null && controller.isShowHadamardGate()) {
      g2d.setColor(Color.BLACK);
      g2d.setStroke(NORMAL_EDGE_STROKE);
      g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
      int midX = (p1.x + p2.x) / 2;
      int midY = (p1.y + p2.y) / 2;
      int gateSize = 16;
      g2d.setColor(HADAMARD_GATE_COLOR);
      g2d.fillRect(midX - gateSize / 2, midY - gateSize / 2, gateSize, gateSize);
      g2d.setColor(Color.BLACK);
      g2d.drawRect(midX - gateSize / 2, midY - gateSize / 2, gateSize, gateSize);
      g2d.drawString("H", midX - 4, midY + 5);
    } else if (edge.getType() == EdgeType.HADAMARD) {
      g2d.setColor(HADAMARD_EDGE_COLOR);
      g2d.setStroke(HADAMARD_EDGE_STROKE);
      g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    } else {
      g2d.setColor(Color.BLACK);
      g2d.setStroke(NORMAL_EDGE_STROKE);
      g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
    }
  }
}
