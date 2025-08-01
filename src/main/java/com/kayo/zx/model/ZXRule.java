package com.kayo.zx.model;

public class ZXRule {
  private String name;
  private final ZXGraph lhs = new ZXGraph();
  private final ZXGraph rhs = new ZXGraph();
  private RuleType type = RuleType.REWRITE;

  public ZXRule(String name) {
    this.name = name;
  }

  public ZXRule(ZXRule other) {
    this.name = other.name;
    this.type = other.type;
    this.lhs.setData(other.lhs);
    this.rhs.setData(other.rhs);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ZXGraph getLhs() {
    return lhs;
  }

  public ZXGraph getRhs() {
    return rhs;
  }

  public RuleType getType() {
    return type;
  }

  public void setType(RuleType type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return name;
  }

  public boolean isEmpty() {
    return lhs.isEmpty() && rhs.isEmpty();
  }

  public boolean isIdenticalTo(ZXRule other) {
    if (other == null)
      return false;
    return this.type == other.type &&
        this.lhs.isIdenticalTo(other.lhs) &&
        this.rhs.isIdenticalTo(other.rhs);
  }

  public String toLMNtal() {
    StringBuilder sb = new StringBuilder();
    String lhsStr = lhs.toLMNtal();
    String rhsStr = rhs.toLMNtal();

    sb.append(String.format("%s@@\n%s\n:-\n%s.", this.getName(), lhsStr, rhsStr));

    if (type == RuleType.EQUALS) {
      sb.append("\n\n");
      sb.append(String.format("%s@@\n%s\n:-\n%s.", this.getName(), rhsStr, lhsStr));
    }
    return sb.toString();
  }

  public final void setData(ZXRule other) {
    this.lhs.setData(other.lhs);
    this.rhs.setData(other.rhs);
    this.type = other.type;
    this.name = other.name;
  }
}
