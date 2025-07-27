package com.kayo.zx.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.kayo.zx.model.Edge;
import com.kayo.zx.model.EdgeType;
import com.kayo.zx.model.Spider;
import com.kayo.zx.model.ZXGraph;

public class LMNtalConverter {

  /**
   * Converts a ZXGraph to its LMNtal representation.
   * 
   * @param graph The ZXGraph to convert.
   * @return The LMNtal representation of the graph as a string.
   */
  public static String toLMNtal(ZXGraph graph) {
    StringBuilder sb = new StringBuilder();

    Map<Edge, String> edgeToLinkName = new HashMap<>();
    AtomicInteger linkCounter = new AtomicInteger(0);
    for (Edge edge : graph.getEdges()) {
      edgeToLinkName.put(edge, "+L" + linkCounter.incrementAndGet());
    }

    Map<Spider, List<String>> spiderToLinks = new HashMap<>();
    for (Spider spider : graph.getSpiders()) {
      spiderToLinks.put(spider, new ArrayList<>());
    }

    for (Edge edge : graph.getEdges()) {
      String linkName = edgeToLinkName.get(edge);
      if (edge.getType() == EdgeType.HADAMARD) {
        String link1 = linkName + "a";
        String link2 = linkName + "b";
        spiderToLinks.get(edge.getSource()).add(link1);
        spiderToLinks.get(edge.getTarget()).add(link2);
        sb.append(String.format("h{%s, %s, e^i(180)},\n", link1, link2));
      } else {
        spiderToLinks.get(edge.getSource()).add(linkName);
        spiderToLinks.get(edge.getTarget()).add(linkName);
      }
    }

    for (Spider spider : graph.getSpiders()) {
      String color = (spider.getType() == com.kayo.zx.model.SpiderType.Z) ? "+1" : "-1";
      String phase = spider.getPhase();
      String links = spiderToLinks.get(spider).stream().collect(Collectors.joining(", "));
      sb.append(String.format("{c(%s), e^i(%s), %s},\n", color, phase, links));
    }

    if (sb.length() > 2 && sb.charAt(sb.length() - 2) == ',') {
      sb.delete(sb.length() - 2, sb.length());
    }
    sb.append(".");
    return sb.toString();
  }
}
