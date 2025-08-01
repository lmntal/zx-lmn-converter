# ZX <-> LMNtal Converter

A Java tool for creating ZX-calculus diagrams and rules and converting them to LMNtal code.

![example](zx<->lmn.png)

## Requirements

- Java 21+
- Maven 3.6+

## Running

```bash
mvn compile exec:java
```

## Usage

**Tools:**
- Diagram and rule converter
- Add Z/X Spider, Add normal Edge/Hadamard Edge
- export LMNtal code to the file

**Interactions:**
- Click: Create spiders
- Right-click: Delete spider/edge, toggle spider/edge type, edit phase
- Drag: Create edges
- Right-Drag: Move spiders

## Development Status

This is a work-in-progress research tool. Current features include basic diagram/rule editing and LMNtal code generation. Planned features include import file functionality, support (undefined number of) free-edges in rule.
