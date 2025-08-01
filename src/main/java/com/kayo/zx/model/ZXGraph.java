package com.kayo.zx.model;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ZXGraph {
  protected final List<Spider> spiders = new ArrayList<>();
  protected final List<Edge> edges = new ArrayList<>();

  public ZXGraph() {
  }

  public ZXGraph(ZXGraph other) {
    this.setData(other);
  }

  public void addSpider(Spider spider) {
    spiders.add(spider);
  }

  public void addEdge(Edge edge) {
    edges.add(edge);
  }

  public void removeSpider(Spider spider) {
    edges.removeIf(e -> e.getSource().equals(spider) || e.getTarget().equals(spider));
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
      if (Math.sqrt(Math.pow(s.getX() - x, 2) + Math.pow(s.getY() - y, 2)) < tolerance) {
        return s;
      }
    }
    return null;
  }

  public Edge findEdgeAt(int x, int y, int tolerance) {
    for (Edge e : edges) {
      if (new Line2D.Float(e.getSource().getLocation(), e.getTarget().getLocation()).ptSegDist(x, y) < tolerance) {
        return e;
      }
    }
    return null;
  }

  public void clear() {
    spiders.clear();
    edges.clear();
  }

  public final void setData(ZXGraph other) {
    this.clear();
    if (other == null)
      return;

    Map<Integer, Spider> oldIdToNewSpider = new HashMap<>();
    for (Spider oldSpider : other.getSpiders()) {
      Spider newSpider = new Spider(oldSpider);
      this.spiders.add(newSpider);
      oldIdToNewSpider.put(oldSpider.getId(), newSpider);
    }
    for (Edge oldEdge : other.getEdges()) {
      Spider newSource = oldIdToNewSpider.get(oldEdge.getSource().getId());
      Spider newTarget = oldIdToNewSpider.get(oldEdge.getTarget().getId());
      if (newSource != null && newTarget != null) {
        this.edges.add(new Edge(oldEdge, newSource, newTarget));
      }
    }
  }

  public boolean isEmpty() {
    return spiders.isEmpty() && edges.isEmpty();
  }

  public boolean isIdenticalTo(ZXGraph other) {
    if (other == null)
      return false;
    if (this.spiders.size() != other.spiders.size() || this.edges.size() != other.edges.size()) {
      return false;
    }
    return this.toLMNtal().equals(other.toLMNtal());
  }

  public String toLMNtal() {
    if (isEmpty())
      return "";

    ArrayList<String> components = new ArrayList<>();

    Map<Edge, String> edgeToLinkName = new HashMap<>();
    AtomicInteger linkCounter = new AtomicInteger(0);
    for (Edge edge : this.edges) {
      edgeToLinkName.put(edge, "+L" + linkCounter.incrementAndGet());
    }

    Map<Spider, List<String>> spiderToLinks = new HashMap<>();
    for (Spider spider : this.spiders) {
      spiderToLinks.put(spider, new ArrayList<>());
    }

    for (Edge edge : this.edges) {
      String linkName = edgeToLinkName.get(edge);
      if (edge.getType() == EdgeType.HADAMARD) {
        String link1 = linkName + "a";
        String link2 = linkName + "b";
        spiderToLinks.get(edge.getSource()).add(link1);
        spiderToLinks.get(edge.getTarget()).add(link2);
        components.add(String.format("h{e^i(180), %s, %s}", link1, link2));
      } else {
        spiderToLinks.get(edge.getSource()).add(linkName);
        spiderToLinks.get(edge.getTarget()).add(linkName);
      }
    }

    for (Spider spider : this.spiders) {
      String color = (spider.getType() == SpiderType.Z) ? "+1" : "-1";
      String phase = spider.getPhase();
      String links = String.join(", ", spiderToLinks.get(spider));
      components.add(String.format("{c(%s), e^i(%s), %s}", color, phase, links));
    }
    if (components.size() > 2 && components.get(components.size() - 1).endsWith(",")) {
      components.remove(components.size() - 1);
    }
    return String.join(",\n", components);
  }
}
