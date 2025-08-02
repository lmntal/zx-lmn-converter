package com.lmntal.zx.model;

import java.awt.Point;

public class Spider extends GraphElement {
  private int x, y;
  private SpiderType type;
  private String phase = "0";
  private String label; // For BOUNDARY type
  private String variableLabel; // For undefined spiders in rules
  private boolean isColorUndefined = false;

  public Spider(int x, int y, SpiderType type) {
    super();
    this.x = x;
    this.y = y;
    this.type = type;
  }

  public Spider(Spider other) {
    this.x = other.x;
    this.y = other.y;
    this.type = other.type;
    this.phase = other.phase;
    this.label = other.label;
    this.variableLabel = other.variableLabel;
    this.isColorUndefined = other.isColorUndefined;
  }

  public boolean isPhaseUndefined() {
    return "?".equals(phase);
  }

  public boolean isUndefined() {
    return isColorUndefined() || isPhaseUndefined();
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public Point getLocation() {
    return new Point(x, y);
  }

  public void setLocation(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public SpiderType getType() {
    return type;
  }

  public void setType(SpiderType type) {
    this.type = type;
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getVariableLabel() {
    return variableLabel;
  }

  public void setVariableLabel(String variableLabel) {
    this.variableLabel = variableLabel;
  }

  public boolean isColorUndefined() {
    return isColorUndefined;
  }

  public void setColorUndefined(boolean isColorUndefined) {
    this.isColorUndefined = isColorUndefined;
  }
}
