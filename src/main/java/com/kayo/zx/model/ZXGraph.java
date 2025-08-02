package com.kayo.zx.model;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ZXGraph {
  protected final List<Spider> spiders = new ArrayList<>();
  protected final List<Edge> edges = new ArrayList<>();
  private int boundaryCounter = 0;
  final private AtomicInteger variableCounter = new AtomicInteger(0);

  public ZXGraph() {
  }

  public ZXGraph(ZXGraph other) {
    this.setData(other);
  }

  public void addSpider(Spider spider) {
    spiders.add(spider);
  }

  public void addEdge(Edge edge) {
    if (edge.getSource().getType() == SpiderType.BOUNDARY && edge.getTarget().getType() == SpiderType.BOUNDARY) {
      System.err.println("Attempted to connect two boundary spiders. Operation aborted in model.");
      return;
    }
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
    boundaryCounter = 0;
    variableCounter.set(0);
  }

  public final void setData(ZXGraph other) {
    this.clear();
    if (other == null)
      return;

    this.boundaryCounter = other.boundaryCounter;
    this.variableCounter.set(other.variableCounter.get());
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

  public String generateUniqueBoundaryLabel() {
    String label;
    do {
      boundaryCounter++;
      label = "b" + boundaryCounter;
    } while (isLabelTaken(label));
    return label;
  }

  public String generateUniqueVariableLabel() {
    return "v" + variableCounter.incrementAndGet();
  }

  private boolean isLabelTaken(String label) {
    return spiders.stream()
        .filter(s -> s.getType() == SpiderType.BOUNDARY)
        .anyMatch(s -> label.equals(s.getLabel().toUpperCase()));
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
    Map<String, Long> thisBoundaryCounts = this.spiders.stream()
        .filter(s -> s.getType() == SpiderType.BOUNDARY && s.getLabel() != null)
        .collect(Collectors.groupingBy(Spider::getLabel, Collectors.counting()));
    Map<String, Long> otherBoundaryCounts = other.spiders.stream()
        .filter(s -> s.getType() == SpiderType.BOUNDARY && s.getLabel() != null)
        .collect(Collectors.groupingBy(Spider::getLabel, Collectors.counting()));

    if (!thisBoundaryCounts.equals(otherBoundaryCounts)) {
      return false;
    }
    return this.toLMNtal().equals(other.toLMNtal());
  }

  public String toLMNtal() {
    return toLMNtal(new java.util.HashSet<>());
  }

  public String toLMNtal(Set<String> variables) {
    if (isEmpty())
      return "";

    ArrayList<String> components = new ArrayList<>();
    Map<Spider, List<String>> spiderToLinks = new HashMap<>();
    for (Spider spider : this.spiders) {
      if (spider.getType() != SpiderType.BOUNDARY) {
        spiderToLinks.put(spider, new ArrayList<>());
      }
    }

    AtomicInteger linkCounter = new AtomicInteger(0);

    for (Edge edge : this.edges) {
      Spider source = edge.getSource();
      Spider target = edge.getTarget();
      boolean sourceIsBoundary = source.getType() == SpiderType.BOUNDARY;
      boolean targetIsBoundary = target.getType() == SpiderType.BOUNDARY;

      if (sourceIsBoundary && targetIsBoundary) {
        continue;
      }

      if (sourceIsBoundary || targetIsBoundary) {
        Spider boundaryNode = sourceIsBoundary ? source : target;
        Spider otherNode = sourceIsBoundary ? target : source;
        String boundaryLabel = "+" + boundaryNode.getLabel().toUpperCase();

        if (edge.getType() == EdgeType.HADAMARD) {
          String intermediateLink = "+L" + linkCounter.incrementAndGet();
          spiderToLinks.get(otherNode).add(intermediateLink);
          components.add(String.format("h{e^i(180), %s, %s}", intermediateLink, boundaryLabel));
        } else { // NORMAL Edge
          spiderToLinks.get(otherNode).add(boundaryLabel);
        }
      } else { // Edge between two non-boundary spiders
        String linkName = "+L" + linkCounter.incrementAndGet();
        if (edge.getType() == EdgeType.HADAMARD) {
          String link1 = linkName + "a";
          String link2 = linkName + "b";
          spiderToLinks.get(source).add(link1);
          spiderToLinks.get(target).add(link2);
          components.add(String.format("h{e^i(180), %s, %s}", link1, link2));
        } else {
          spiderToLinks.get(source).add(linkName);
          spiderToLinks.get(target).add(linkName);
        }
      }
    }

    for (Spider spider : this.spiders) {
      if (spider.getType() == SpiderType.BOUNDARY)
        continue;
      String color;
      String phase;

      if (spider.isUndefined()) {
        String vLabel = spider.getVariableLabel();
        if (vLabel == null || vLabel.trim().isEmpty()) {
          // This should be prevented by the controller logic
          continue;
        }
        if (spider.isColorUndefined()) {
          color = "C" + vLabel;
          variables.add(color);
        } else {
          color = (spider.getType() == SpiderType.Z) ? "+1" : "-1";
        }

        if (spider.isPhaseUndefined()) {
          phase = "P" + vLabel;
          variables.add(phase);
        } else {
          phase = spider.getPhase();
        }
      } else {
        color = (spider.getType() == SpiderType.Z) ? "+1" : "-1";
        phase = spider.getPhase();
      }

      String links = String.join(", ", spiderToLinks.get(spider));
      components.add(String.format("{c(%s), e^i(%s), %s}", color, phase, links));
    }

    return String.join(",\n", components);
  }
}
