package com.lmntal.zx.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ZXRule {
  private final String name;
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
    return Objects.equals(this.type, other != null ? other.type : null) &&
        this.lhs.isIdenticalTo(other != null ? other.lhs : null) &&
        this.rhs.isIdenticalTo(other != null ? other.rhs : null);
  }

  public String toLMNtal() {
    Set<String> lhsVars = new HashSet<>();
    Set<String> rhsVars = new HashSet<>();

    String lhsStr = lhs.toLMNtal(lhsVars);
    String rhsStr = rhs.toLMNtal(rhsVars);

    Set<String> allVars = new HashSet<>();
    allVars.addAll(lhsVars);
    allVars.addAll(rhsVars);

    StringBuilder sb = new StringBuilder(256);
    String guard = "";
    if (!allVars.isEmpty()) {
      String varString = allVars.stream().sorted().collect(Collectors.joining("), int("));
      guard = " int(" + varString + ") | ";
    }

    sb.append(String.format("%s@@\n%s\n:-%s\n%s.", this.getName(), lhsStr, guard, rhsStr));

    if (type == RuleType.EQUALS) {
      sb.append("\n\n");
      sb.append(String.format("%s@@\n%s\n:-%s\n%s.", this.getName(), rhsStr, guard, lhsStr));
    }
    return sb.toString();
  }

  public final void setData(ZXRule other) {
    if (other == null) {
      throw new IllegalArgumentException("Other rule cannot be null");
    }
    this.lhs.setData(other.lhs);
    this.rhs.setData(other.rhs);
    this.type = other.type;
  }
}
