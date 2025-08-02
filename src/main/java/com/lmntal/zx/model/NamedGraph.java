package com.lmntal.zx.model;

public class NamedGraph extends ZXGraph {
  private String name;

  public NamedGraph(String name) {
    super();
    this.name = name;
  }

  public NamedGraph(NamedGraph other) {
    super(other);
    this.name = other.name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
