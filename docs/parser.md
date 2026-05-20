# Parser

Purpose: document the parser contract used by lab 2 syntax-tree output and lab 3
three-address-code generation.

## Entry Points

- `Experiment2 --tree` parses stdin and prints the lab 2 syntax tree.
- `Experiment2 --tac` parses stdin and prints lab 3 three-address code.
- `Experiment2` without arguments defaults to `--tac`.

## Grammar Scope

The parser is a recursive-descent parser for the lab statement and expression
subset.

- Program: `P -> L+`.
- Statement line: `L -> S ;`.
- Assignment: `S -> id = E`.
- Conditional: `S -> if C then S S'`, with `S' -> else S | epsilon`.
- Loop: `S -> while C do S`.
- Condition: `C -> E relop E`.
- Expressions use the usual precedence split: `E`, `T`, and `F`.
- Factors support identifiers, parenthesized expressions, and valid integer
  tokens from the shared lexer.

The parser accepts relational operators `>`, `<`, `=`, `>=`, `<=`, and `<>`
because the shared lexer exposes those tokens.

## Syntax-Tree Mode

`--tree` is the lab 2 compatibility mode.

- Nonterminals are printed with the production selected by the parser.
- Terminals are printed with token type and value when a value is meaningful.
- `epsilon` productions are printed explicitly.
- A successful parse ends with `语法分析成功！`.

## Error Behavior

Parser errors throw `RuntimeException` internally and are printed by the
`Experiment2` command-line entry point. The message includes the expected shape
and the active token when available.

## Test Coverage

- `tests/lab2_tree_sample.*` protects the lab 2 syntax-tree output.
- `tests/lab3_tac_sample.*` protects the parser path used by lab 3 TAC output.
- Add focused fixtures before changing grammar acceptance, error messages, or
  tree formatting.
