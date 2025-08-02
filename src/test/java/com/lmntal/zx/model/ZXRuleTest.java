package com.lmntal.zx.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ZXRuleTest {

  private ZXRule rule;
  private Spider zSpider;
  private Spider xSpider;

  @BeforeEach
  void setUp() {
    rule = new ZXRule("test_rule");
    zSpider = new Spider(10, 10, SpiderType.Z);
    xSpider = new Spider(50, 50, SpiderType.X);
  }

  @Test
  void testRuleCreation() {
    assertEquals("test_rule", rule.getName());
    assertTrue(rule.getLhs().isEmpty());
    assertTrue(rule.getRhs().isEmpty());
    assertEquals(RuleType.REWRITE, rule.getType());
  }

  @Test
  void testIsEmpty() {
    assertTrue(rule.isEmpty());
    rule.getLhs().addSpider(zSpider);
    assertFalse(rule.isEmpty());
  }

  @Test
  void testToLMNtalRewriteRule() {
    rule.getLhs().addSpider(zSpider);
    rule.getRhs().addSpider(xSpider);
    rule.setType(RuleType.REWRITE);

    String lhsStr = "{c(+1), e^i(0), }";
    String rhsStr = "{c(-1), e^i(0), }";

    String expected = String.format("test_rule@@\n%s\n:-\n%s.", lhsStr, rhsStr);
    // Normalize line endings and spacing for reliable comparison
    String actual = rule.toLMNtal().replaceAll("\\s+", " ").trim();
    expected = expected.replaceAll("\\s+", " ").trim();
    assertEquals(expected, actual);
  }

  @Test
  void testToLMNtalEqualsRule() {
    rule.getLhs().addSpider(zSpider);
    rule.getRhs().addSpider(xSpider);
    rule.setType(RuleType.EQUALS);

    String lhsStr = "{c(+1), e^i(0), }";
    String rhsStr = "{c(-1), e^i(0), }";

    String forwardRule = String.format("test_rule@@\n%s\n:-\n%s.", lhsStr, rhsStr);
    String backwardRule = String.format("test_rule@@\n%s\n:-\n%s.", rhsStr, lhsStr);
    String expected = forwardRule + "\n\n" + backwardRule;

    String actual = rule.toLMNtal().replaceAll("\\s+", " ").trim();
    expected = expected.replaceAll("\\s+", " ").trim();

    assertEquals(expected, actual);
  }

  @Test
  void testIsIdenticalTo() {
    ZXRule rule2 = new ZXRule("test_rule");
    assertTrue(rule.isIdenticalTo(rule2));

    rule.getLhs().addSpider(new Spider(10, 10, SpiderType.Z));
    rule2.getLhs().addSpider(new Spider(10, 10, SpiderType.Z));
    assertTrue(rule.isIdenticalTo(rule2));

    rule2.getRhs().addSpider(new Spider(50, 50, SpiderType.X));
    assertFalse(rule.isIdenticalTo(rule2));
  }
}
