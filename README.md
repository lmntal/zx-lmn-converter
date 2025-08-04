# ZX <-> LMNtal Converter

A Java Swing tool for creating ZX-calculus diagrams and rules and converting them to LMNtal code.

![example](zx<->lmn.png)

## Requirements

- Java 21+
- Maven 3.6+

## Running

```bash
mvn compile exec:java
```

## Features

### Graph Editor
- Create and edit ZX-calculus diagrams
- Support for Z spiders (green), X spiders (red), and boundary nodes
- Normal and Hadamard edges with visual representation
- Real-time LMNtal code generation

### Rule Editor
- Create rewrite rules with left-hand side (LHS) and right-hand side (RHS)
- Support for one-way (→) and two-way (=) rules
- Undefined color and phase variables for pattern matching
- Boundary node matching between LHS and RHS

### Interface
- Dual-pane editor for graphs and rules
- Live LMNtal output preview
- Export functionality to .lmn files
- Project management with multiple graphs and rules

## Usage

### Toolbar Controls
- **Spider Type**: Select Z, X, or Boundary spider types
- **Edge Type**: Choose Normal or Hadamard edges
- **Show H Gate**: Toggle Hadamard gate visualization

### Mouse Interactions
- **Left Click**: Create spiders at cursor position
- **Left Drag**: Create edges between spiders
- **Right Click**: Context menu for editing/deleting elements
- **Right Drag**: Move spiders around the canvas

### Context Menu Options
- **Spiders**: Toggle type (Z/X), edit phase, delete
- **Rule Spiders**: Toggle undefined color/phase, set variable labels
- **Edges**: Toggle Hadamard type, delete
- **Boundary Nodes**: Edit labels (rule editor only)

### File Operations
- **New Graph/Rule**: Create additional diagrams
- **Export**: Save all graphs and rules to .lmn file
- **Convert/Save**: Generate LMNtal code for current item

## LMNtal Output Format

### Graphs
```
{c(+1), e^i(90), +L1, +L2}  // Z spider with phase π/2 and 2 legs
```

### Rules
```
rule_name@@
{c(+1), e^i(0), +L1}
:-
{c(-1), e^i(0), +L1}.
```

## Development Status

This is a research tool for ZX-calculus to LMNtal conversion. Current features include:
- ✅ Interactive diagram editing
- ✅ Rule creation with pattern matching
- ✅ LMNtal code generation
- ✅ File export functionality
- ✅ Undefined variables in rules
- ✅ Boundary node support
- ✅ Import .lmn files

Planned features:
- 🔄 Bidirectional rule import
- 🔄 Advanced pattern matching for QLMNtal
