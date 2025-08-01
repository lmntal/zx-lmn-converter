package com.kayo.zx.model;

public enum RuleType {
  REWRITE("→"),
  EQUALS("=");

  private final String symbol;

  RuleType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return this.symbol;
  }
}
