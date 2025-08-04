package com.lmntal.zx.importer;

import com.lmntal.zx.model.*;
import com.lmntal.zx.parser.LMNtalBaseListener;
import com.lmntal.zx.parser.LMNtalLexer;
import com.lmntal.zx.parser.LMNtalParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class LMNtalImporter {

  private final List<NamedGraph> graphs = new ArrayList<>();
  private final List<ZXRule> rules = new ArrayList<>();
  private final List<String> errorMessages = new ArrayList<>();
  private final AtomicInteger graphCounter = new AtomicInteger(1);

  public void importFile(Path path) throws IOException {
    LMNtalLexer lexer = new LMNtalLexer(CharStreams.fromPath(path));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    LMNtalParser parser = new LMNtalParser(tokens);
    parser.removeErrorListeners(); // remove default console error listeners

    ImporterErrorListener errorListener = new ImporterErrorListener();
    parser.addErrorListener(errorListener);

    ParseTreeWalker walker = new ParseTreeWalker();
    ImporterListener listener = new ImporterListener();
    try {
      walker.walk(listener, parser.file());
    } catch (Exception e) {
      errorMessages.add("A critical error occurred during processing: " + e.getMessage());
    }

    if (!errorListener.getErrors().isEmpty()) {
      errorMessages.add("Syntax errors detected in LMNtal file:");
      errorMessages.addAll(errorListener.getErrors());
    }
  }

  public List<NamedGraph> getGraphs() {
    return graphs;
  }

  public List<ZXRule> getRules() {
    return rules;
  }

  public List<String> getErrorMessages() {
    return errorMessages;
  }

  private class ImporterListener extends LMNtalBaseListener {

    private Map<String, List<Spider>> currentLinkMap;
    private List<HadamardConnection> currentHadamards;

    @Override
    public void enterGraph(LMNtalParser.GraphContext ctx) {
      try {
        NamedGraph graph = new NamedGraph("graph_" + graphCounter.getAndIncrement());
        buildGraph(graph, ctx.atom_list());
        layoutGraph(graph, 400, 300);
        graphs.add(graph);
      } catch (Exception e) {
        errorMessages.add("Failed to process a graph: " + e.getMessage());
      }
    }

    @Override
    public void enterRule(LMNtalParser.RuleContext ctx) {
      String ruleName = ctx.RULE_ID().getText().replace("@@", "");
      ZXRule rule = new ZXRule(ruleName);

      try {
        // The first atom_list is the LHS
        buildGraph(rule.getLhs(), ctx.atom_list());
        // The atom_list inside the body is the RHS
        buildGraph(rule.getRhs(), ctx.body().atom_list());

        processBoundaries(rule);
        layoutGraph(rule.getLhs(), 250, 250);
        layoutGraph(rule.getRhs(), 250, 250);
        rules.add(rule);

      } catch (Exception e) {
        errorMessages
            .add("Failed to process rule '" + ruleName + "': " + e.getClass().getSimpleName() + " - " + e.getMessage());
      }
    }

    private void buildGraph(ZXGraph graph, LMNtalParser.Atom_listContext atomListCtx) {
      if (atomListCtx == null)
        return;

      currentLinkMap = new HashMap<>();
      currentHadamards = new ArrayList<>();

      for (LMNtalParser.AtomContext atomCtx : atomListCtx.atom()) {
        if (atomCtx.hadamard_gate() != null) {
          processHadamardGate(atomCtx.hadamard_gate());
        } else {
          processSpiderAtom(atomCtx, graph);
        }
      }

      connectEdges(graph);
    }

    private void processSpiderAtom(LMNtalParser.AtomContext atomCtx, ZXGraph graph) {
      Spider spider = new Spider(0, 0, SpiderType.Z);
      graph.addSpider(spider);

      for (LMNtalParser.Atom_contentContext contentCtx : atomCtx.atom_content_list().atom_content()) {
        if (contentCtx.color_atom() != null) {
          parseColor(spider, contentCtx.color_atom().value());
        } else if (contentCtx.phase_atom() != null) {
          parsePhase(spider, contentCtx.phase_atom().value());
        } else if (contentCtx.link() != null) {
          if (contentCtx.link().CAP_ID() != null) {
            String linkName = contentCtx.link().CAP_ID().getText();
            currentLinkMap.computeIfAbsent(linkName, k -> new ArrayList<>()).add(spider);
          }
        }
      }
    }

    private void parseColor(Spider spider, LMNtalParser.ValueContext valueCtx) {
      if (valueCtx.CAP_ID() != null) {
        spider.setColorUndefined(true);
        String var = valueCtx.CAP_ID().getText();
        if (var.length() > 1) {
          spider.setVariableLabel(var.substring(1));
        }
      } else if (valueCtx.getText().contains("-")) {
        spider.setType(SpiderType.X);
      }
    }

    private void parsePhase(Spider spider, LMNtalParser.ValueContext valueCtx) {
      if (valueCtx.CAP_ID() != null) {
        spider.setPhase("?");
        if (!spider.isColorUndefined()) {
          String var = valueCtx.CAP_ID().getText();
          if (var.length() > 1) {
            spider.setVariableLabel(var.substring(1));
          }
        }
      } else {
        spider.setPhase(valueCtx.getText());
      }
    }

    private void processHadamardGate(LMNtalParser.Hadamard_gateContext hadamardCtx) {
      List<String> links = new ArrayList<>();
      for (LMNtalParser.Hadamard_contentContext content : hadamardCtx.hadamard_content_list().hadamard_content()) {
        if (content.link() != null && content.link().CAP_ID() != null) {
          links.add(content.link().CAP_ID().getText());
        }
      }
      if (links.size() == 2) {
        currentHadamards.add(new HadamardConnection(links.get(0), links.get(1)));
      }
    }

    private void connectEdges(ZXGraph graph) {
      for (List<Spider> spiders : currentLinkMap.values()) {
        if (spiders.size() == 2) {
          graph.addEdge(new Edge(spiders.get(0), spiders.get(1), EdgeType.NORMAL));
        }
      }
      for (HadamardConnection hc : currentHadamards) {
        if (currentLinkMap.containsKey(hc.link1) && currentLinkMap.containsKey(hc.link2)) {
          Spider s1 = currentLinkMap.get(hc.link1).get(0);
          Spider s2 = currentLinkMap.get(hc.link2).get(0);
          graph.addEdge(new Edge(s1, s2, EdgeType.HADAMARD));
        }
      }
    }

    private void processBoundaries(ZXRule rule) {
      Map<String, Spider> lhsFreeLinks = findFreeLinks(rule.getLhs());
      Map<String, Spider> rhsFreeLinks = findFreeLinks(rule.getRhs());

      Set<String> commonLabels = new HashSet<>(lhsFreeLinks.keySet());
      commonLabels.retainAll(rhsFreeLinks.keySet());

      for (String label : commonLabels) {
        Spider lhsSpider = lhsFreeLinks.get(label);
        Spider rhsSpider = rhsFreeLinks.get(label);

        Spider lhsBoundary = new Spider(0, 0, SpiderType.BOUNDARY);
        lhsBoundary.setLabel(label);
        rule.getLhs().addSpider(lhsBoundary);
        rule.getLhs().addEdge(new Edge(lhsSpider, lhsBoundary, EdgeType.NORMAL));

        Spider rhsBoundary = new Spider(0, 0, SpiderType.BOUNDARY);
        rhsBoundary.setLabel(label);
        rule.getRhs().addSpider(rhsBoundary);
        rule.getRhs().addEdge(new Edge(rhsSpider, rhsBoundary, EdgeType.NORMAL));
      }
    }

    private Map<String, Spider> findFreeLinks(ZXGraph graph) {
      Map<Spider, Integer> linkCounts = new HashMap<>();
      for (Edge e : graph.getEdges()) {
        linkCounts.put(e.getSource(), linkCounts.getOrDefault(e.getSource(), 0) + 1);
        linkCounts.put(e.getTarget(), linkCounts.getOrDefault(e.getTarget(), 0) + 1);
      }

      Map<String, Spider> freeLinks = new HashMap<>();
      int counter = 1;
      List<Spider> sortedSpiders = graph.getSpiders().stream()
          .sorted(Comparator.comparingInt(GraphElement::getId))
          .collect(Collectors.toList());

      for (Spider s : sortedSpiders) {
        if (s.getType() != SpiderType.BOUNDARY && linkCounts.getOrDefault(s, 0) == 1) {
          freeLinks.put("b" + counter++, s);
        }
      }
      return freeLinks;
    }

    private void layoutGraph(ZXGraph graph, int centerX, int centerY) {
      List<Spider> spidersToLayout = graph.getSpiders().stream()
          .filter(s -> s.getType() != SpiderType.BOUNDARY).collect(Collectors.toList());
      if (spidersToLayout.isEmpty())
        return;

      int n = spidersToLayout.size();
      double radius = Math.min(150, n * 20 + 20);
      for (int i = 0; i < n; i++) {
        Spider s = spidersToLayout.get(i);
        double angle = 2 * Math.PI * i / n;
        int x = (int) (centerX + radius * Math.cos(angle));
        int y = (int) (centerY + radius * Math.sin(angle));
        s.setLocation(x, y);
      }
    }

    private record HadamardConnection(String link1, String link2) {
    }
  }
}
