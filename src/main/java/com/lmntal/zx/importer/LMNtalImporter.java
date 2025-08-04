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
    // Clear previous results
    this.graphs.clear();
    this.rules.clear();
    this.errorMessages.clear();
    this.graphCounter.set(1);

    // Setup ANTLR parser
    LMNtalLexer lexer = new LMNtalLexer(CharStreams.fromPath(path));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    LMNtalParser parser = new LMNtalParser(tokens);
    parser.removeErrorListeners(); // remove default console error listeners

    ImporterErrorListener errorListener = new ImporterErrorListener();
    parser.addErrorListener(errorListener);

    // Temporary storage for parsed rules and their guards
    List<ZXRule> rawRules = new ArrayList<>();
    Map<ZXRule, String> ruleGuards = new HashMap<>();

    // Parse the file
    ParseTreeWalker walker = new ParseTreeWalker();
    // Pass temporary storage to the listener
    ImporterListener listener = new ImporterListener(rawRules, ruleGuards);
    try {
      walker.walk(listener, parser.file());
    } catch (Exception e) {
      errorMessages.add("A critical error occurred during processing: " + e.getMessage());
    }

    // Process parsed rules to identify and merge two-way rules
    Set<ZXRule> pairedRules = new HashSet<>();
    for (int i = 0; i < rawRules.size(); i++) {
      ZXRule rule1 = rawRules.get(i);
      if (pairedRules.contains(rule1)) {
        continue;
      }

      boolean foundPair = false;
      for (int j = i + 1; j < rawRules.size(); j++) {
        ZXRule rule2 = rawRules.get(j);
        if (pairedRules.contains(rule2)) {
          continue;
        }

        String guard1 = ruleGuards.getOrDefault(rule1, "");
        String guard2 = ruleGuards.getOrDefault(rule2, "");

        // Check for same name, same guard, and reverse structure
        if (rule1.getName().equals(rule2.getName()) &&
            guard1.equals(guard2) &&
            rule1.getLhs().isIdenticalTo(rule2.getRhs()) &&
            rule1.getRhs().isIdenticalTo(rule2.getLhs())) {

          // Found a pair, create a single two-way rule from rule1
          rule1.setType(RuleType.EQUALS);
          this.rules.add(rule1);

          pairedRules.add(rule1);
          pairedRules.add(rule2);
          foundPair = true;
          break;
        }
      }

      if (!foundPair) {
        // No pair found, add as a one-way (rewrite) rule
        this.rules.add(rule1);
      }
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

    private final List<ZXRule> rawRules;
    private final Map<ZXRule, String> ruleGuards;

    // Constructor to receive the temporary storage
    public ImporterListener(List<ZXRule> rawRules, Map<ZXRule, String> ruleGuards) {
      this.rawRules = rawRules;
      this.ruleGuards = ruleGuards;
    }

    @Override
    public void enterGraph(LMNtalParser.GraphContext ctx) {
      try {
        NamedGraph graph = new NamedGraph("graph_" + graphCounter.getAndIncrement());
        buildGraphFromContext(graph, ctx.atom_list());
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
        buildGraphFromContext(rule.getLhs(), ctx.atom_list());
        buildGraphFromContext(rule.getRhs(), ctx.body().atom_list());

        layoutGraph(rule.getLhs(), 250, 250);
        layoutGraph(rule.getRhs(), 250, 250);

        rawRules.add(rule);

        if (ctx.guard() != null) {
          ruleGuards.put(rule, ctx.guard().getText());
        }
      } catch (Exception e) {
        errorMessages
            .add("Failed to process rule '" + ruleName + "': " + e.getClass().getSimpleName() + " - " + e.getMessage());
      }
    }

    private void buildGraphFromContext(ZXGraph graph, LMNtalParser.Atom_listContext atomListCtx) {
      if (atomListCtx == null)
        return;

      Map<String, List<Spider>> linkMap = new HashMap<>();
      List<HadamardConnection> hadamardLinks = new ArrayList<>();

      // 1. First pass: Create spiders and populate link maps
      for (LMNtalParser.AtomContext atomCtx : atomListCtx.atom()) {
        if (atomCtx.hadamard_gate() != null) {
          processHadamardGate(atomCtx.hadamard_gate(), hadamardLinks);
        } else {
          processSpiderAtom(atomCtx, graph, linkMap);
        }
      }

      // 2. Second pass: Create edges and identify true boundaries
      Set<String> hadamardInvolvedLinks = new HashSet<>();
      for (HadamardConnection hc : hadamardLinks) {
        hadamardInvolvedLinks.add(hc.link1());
        hadamardInvolvedLinks.add(hc.link2());
      }

      // Handle normal edges (links appearing twice)
      for (Map.Entry<String, List<Spider>> entry : linkMap.entrySet()) {
        if (entry.getValue().size() == 2) {
          graph.addEdge(new Edge(entry.getValue().get(0), entry.getValue().get(1), EdgeType.NORMAL));
        }
      }

      // Handle Hadamard edges
      for (HadamardConnection hc : hadamardLinks) {
        if (linkMap.containsKey(hc.link1()) && linkMap.containsKey(hc.link2())) {
          Spider s1 = linkMap.get(hc.link1()).get(0);
          Spider s2 = linkMap.get(hc.link2()).get(0);
          graph.addEdge(new Edge(s1, s2, EdgeType.HADAMARD));
        }
      }

      // 3. Third pass: Handle boundaries (links appearing once AND not in a hadamard
      // gate)
      for (Map.Entry<String, List<Spider>> entry : linkMap.entrySet()) {
        if (entry.getValue().size() == 1) {
          String label = entry.getKey();
          // Only create a boundary if the link is not involved in a Hadamard gate
          if (!hadamardInvolvedLinks.contains(label)) {
            Spider internalSpider = entry.getValue().get(0);
            Spider boundarySpider = new Spider(0, 0, SpiderType.BOUNDARY);
            boundarySpider.setLabel(label);
            graph.addSpider(boundarySpider);
            graph.addEdge(new Edge(internalSpider, boundarySpider, EdgeType.NORMAL));
          }
        }
      }
    }

    private void processSpiderAtom(LMNtalParser.AtomContext atomCtx, ZXGraph graph, Map<String, List<Spider>> linkMap) {
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
            linkMap.computeIfAbsent(linkName, k -> new ArrayList<>()).add(spider);
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

    private void processHadamardGate(LMNtalParser.Hadamard_gateContext hadamardCtx,
        List<HadamardConnection> hadamardLinks) {
      List<String> links = new ArrayList<>();
      for (LMNtalParser.Hadamard_contentContext content : hadamardCtx.hadamard_content_list().hadamard_content()) {
        if (content.link() != null && content.link().CAP_ID() != null) {
          links.add(content.link().CAP_ID().getText());
        }
      }
      if (links.size() == 2) {
        hadamardLinks.add(new HadamardConnection(links.get(0), links.get(1)));
      }
    }

    private void layoutGraph(ZXGraph graph, int centerX, int centerY) {
      List<Spider> spidersToLayout = graph.getSpiders().stream()
          .filter(s -> s.getType() != SpiderType.BOUNDARY).collect(Collectors.toList());
      if (spidersToLayout.isEmpty())
        return;

      int n = spidersToLayout.size();
      double radius = Math.min(100, n * 15 + 30);

      List<Spider> boundarySpiders = graph.getSpiders().stream()
          .filter(s -> s.getType() == SpiderType.BOUNDARY).collect(Collectors.toList());

      for (int i = 0; i < n; i++) {
        Spider s = spidersToLayout.get(i);
        double angle = 2 * Math.PI * i / n;
        int x = (int) (centerX + radius * Math.cos(angle));
        int y = (int) (centerY + radius * Math.sin(angle));
        s.setLocation(x, y);

        for (Edge e : graph.getEdges()) {
          if (e.getSource().equals(s) && e.getTarget().getType() == SpiderType.BOUNDARY) {
            int bx = (int) (centerX + (radius + 40) * Math.cos(angle));
            int by = (int) (centerY + (radius + 40) * Math.sin(angle));
            e.getTarget().setLocation(bx, by);
          } else if (e.getTarget().equals(s) && e.getSource().getType() == SpiderType.BOUNDARY) {
            int bx = (int) (centerX + (radius + 40) * Math.cos(angle));
            int by = (int) (centerY + (radius + 40) * Math.sin(angle));
            e.getSource().setLocation(bx, by);
          }
        }
      }
    }

    private record HadamardConnection(String link1, String link2) {
    }
  }
}
