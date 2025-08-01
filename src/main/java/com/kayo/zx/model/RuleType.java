package com.kayo.zx.model;

public enum RuleType {
  REWRITE("→ (one-way)"),
  EQUALS("= (two-way)");

  private final String symbol;

  RuleType(String symbol) {
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return this.symbol;
  }
}
