package com.lmntal.zx.model;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ZXGraphTest {

  private ZXGraph graph;
  private Spider zSpider;
  private Spider xSpider;

  @BeforeEach
  void setUp() {
    graph = new ZXGraph();
    zSpider = new Spider(10, 10, SpiderType.Z);
    xSpider = new Spider(50, 50, SpiderType.X);
  }

  @Test
  void testAddAndRemoveSpider() {
    assertTrue(graph.getSpiders().isEmpty());
    graph.addSpider(zSpider);
    assertEquals(1, graph.getSpiders().size());
    assertTrue(graph.getSpiders().contains(zSpider));

    graph.removeSpider(zSpider);
    assertTrue(graph.getSpiders().isEmpty());
  }

  @Test
  void testAddAndRemoveEdge() {
    graph.addSpider(zSpider);
    graph.addSpider(xSpider);
    Edge edge = new Edge(zSpider, xSpider, EdgeType.NORMAL);

    assertTrue(graph.getEdges().isEmpty());
    graph.addEdge(edge);
    assertEquals(1, graph.getEdges().size());
    assertTrue(graph.getEdges().contains(edge));

    graph.removeEdge(edge);
    assertTrue(graph.getEdges().isEmpty());
  }

  @Test
  void testRemoveSpiderShouldAlsoRemoveAssociatedEdges() {
    graph.addSpider(zSpider);
    graph.addSpider(xSpider);
    Edge edge = new Edge(zSpider, xSpider, EdgeType.NORMAL);
    graph.addEdge(edge);

    assertEquals(1, graph.getEdges().size());
    graph.removeSpider(zSpider);
    assertTrue(graph.getSpiders().contains(xSpider));
    assertFalse(graph.getSpiders().contains(zSpider));
    assertTrue(graph.getEdges().isEmpty());
  }

  @Test
  void testFindSpiderAt() {
    graph.addSpider(zSpider);
    assertNotNull(graph.findSpiderAt(12, 12, 5));
    assertNull(graph.findSpiderAt(100, 100, 5));
  }

  @Test
  void testClear() {
    graph.addSpider(zSpider);
    graph.addSpider(xSpider);
    graph.addEdge(new Edge(zSpider, xSpider, EdgeType.NORMAL));
    assertFalse(graph.isEmpty());

    graph.clear();
    assertTrue(graph.isEmpty());
  }

  @Test
  void testIsIdenticalTo() {
    ZXGraph graph2 = new ZXGraph();
    Spider zSpider2 = new Spider(10, 10, SpiderType.Z);
    Spider xSpider2 = new Spider(50, 50, SpiderType.X);
    graph.addSpider(zSpider);
    graph.addSpider(xSpider);
    graph2.addSpider(zSpider2);
    graph2.addSpider(xSpider2);

    // Note: isIdenticalTo compares LMNtal output, so IDs don't matter
    assertTrue(graph.isIdenticalTo(graph2));

    graph.addEdge(new Edge(zSpider, xSpider, EdgeType.NORMAL));
    assertFalse(graph.isIdenticalTo(graph2));
  }

  @Test
  void testToLMNtalSimple() {
    graph.addSpider(zSpider);
    zSpider.setPhase("90");
    String expected = "{c(+1), e^i(90), }.";
    // toLMNtal() doesn't add the trailing dot, so we add it for comparison
    assertEquals(expected.substring(0, expected.length() - 1), graph.toLMNtal());
  }

  @Test
  void testToLMNtalWithVariable() {
    Spider undefinedSpider = new Spider(10, 10, SpiderType.Z);
    undefinedSpider.setColorUndefined(true);
    undefinedSpider.setVariableLabel("v1");
    graph.addSpider(undefinedSpider);
    Set<String> variables = new HashSet<>();
    String lmntal = graph.toLMNtal(variables);

    assertTrue(variables.contains("Cv1"));
    assertTrue(lmntal.contains("c(Cv1)"));
  }

  @Test
  void testGenerateUniqueBoundaryLabel() {
    Spider b1 = new Spider(0, 0, SpiderType.BOUNDARY);
    b1.setLabel("b1");
    graph.addSpider(b1);
    String newLabel = graph.generateUniqueBoundaryLabel();
    assertEquals("b2", newLabel);
  }
}
