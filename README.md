# ZX <-> LMNtal Converter

A Java tool for creating ZX-calculus diagrams and converting them to LMNtal code.

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
- Select, Add Z/X Spider, Add Edge/Hadamard Edge

**Interactions:**
- Double-click spider: Edit phase
- Right-click: Delete or toggle edge type
- Drag: Move spiders (Select mode) or create edges

## Development Status

This is a work-in-progress research tool. Current features include basic diagram editing and LMNtal code generation. Planned features include a rule system for ZX-calculus transformations, import/export functionality, and an enhanced user interface.
