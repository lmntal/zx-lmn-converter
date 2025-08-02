package com.lmntal.zx.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class GraphElement {
  private static final AtomicInteger idCounter = new AtomicInteger(0);
  protected final int id;

  public GraphElement() {
    this.id = idCounter.getAndIncrement();
  }

  public int getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    GraphElement that = (GraphElement) o;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
