package com.lmntal.zx.model;

public class Edge extends GraphElement {
  private final Spider source;
  private final Spider target;
  private EdgeType type;

  public Edge(Spider source, Spider target, EdgeType type) {
    super();
    this.source = source;
    this.target = target;
    this.type = type;
  }

  public Edge(Edge other, Spider newSource, Spider newTarget) {
    this.source = newSource;
    this.target = newTarget;
    this.type = other.type;
  }

  public Spider getSource() {
    return source;
  }

  public Spider getTarget() {
    return target;
  }

  public EdgeType getType() {
    return type;
  }

  public void setType(EdgeType type) {
    this.type = type;
  }
}
