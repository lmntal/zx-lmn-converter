grammar LMNtal;

@header {
    package com.lmntal.zx.parser;
}

// Parser Rules
file: toplevel_element* EOF;

toplevel_element: graph | rule;

graph: atom_list '.';

rule: RULE_ID atom_list separator guard? body '.';

separator: ':-';
guard: guard_decl (',' guard_decl)* '|'; // Handle multiple guard declarations
guard_decl: ID '(' CAP_ID ')';

body: atom_list;

atom_list: atom (',' atom)*;

atom: '{' atom_content_list? '}' | hadamard_gate; // atom_content_list can be optional

atom_content_list: atom_content (',' atom_content)* ','?; // Allow optional trailing comma

atom_content: color_atom | phase_atom | link;

hadamard_gate: 'h' '{' hadamard_content_list '}';
hadamard_content_list: hadamard_content (',' hadamard_content)*;
hadamard_content: phase_atom | link;


color_atom: 'c' '(' value ')';
phase_atom: 'e^i' '(' value ')';
link: SIGN? CAP_ID;
value: (SIGN? INT) | CAP_ID;

// Lexer Rules
RULE_ID: ID '@@';
ID: [a-z_] [a-zA-Z_0-9]*;
CAP_ID: [A-Z] [a-zA-Z_0-9]*;

SIGN: '+' | '-';
INT: [0-9]+;

LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
COMMA: ',';
DOT: '.';
PIPE: '|';
COLON: ':';

COMMENT: ('//' | '%') ~[\r\n]* -> skip;
WS: [ \t\r\n]+ -> skip;
