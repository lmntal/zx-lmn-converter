package com.kayo.zx.model;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class ZXGraph {
  private final List<Spider> spiders = new ArrayList<>();
  private final List<Edge> edges = new ArrayList<>();

  public void addSpider(Spider spider) {
    spiders.add(spider);
  }

  public void addEdge(Edge edge) {
    edges.add(edge);
  }

  public void removeSpider(Spider spider) {
    edges.removeIf(edge -> edge.getSource().equals(spider) || edge.getTarget().equals(spider));
    spiders.remove(spider);
  }

  public void removeEdge(Edge edge) {
    edges.remove(edge);
  }

  public List<Spider> getSpiders() {
    return new ArrayList<>(spiders);
  }

  public List<Edge> getEdges() {
    return new ArrayList<>(edges);
  }

  public Spider findSpiderAt(int x, int y, int tolerance) {
    for (Spider s : spiders) {
      double dist = Math.sqrt(Math.pow(s.getX() - x, 2) + Math.pow(s.getY() - y, 2));
      if (dist < tolerance) {
        return s;
      }
    }
    return null;
  }

  public Edge findEdgeAt(int x, int y, int tolerance) {
    for (Edge e : edges) {
      Line2D line = new Line2D.Float(e.getSource().getLocation(), e.getTarget().getLocation());
      if (line.ptSegDist(x, y) < tolerance) {
        return e;
      }
    }
    return null;
  }

  public void clear() {
    spiders.clear();
    edges.clear();
  }
}
