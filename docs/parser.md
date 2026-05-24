# Parser

Purpose: document the parser contract used by lab 2 syntax-tree output and lab 3
three-address-code generation.

## Entry Points

- `Experiment2 --tree` parses stdin and prints the lab 2 syntax tree.
- `Experiment2 --tac` parses stdin and prints lab 3 three-address code.
- `Experiment2 --ast` parses stdin through the Bison lab 3 path and prints the
  internal AST used before TAC generation.
- `Experiment2 --ast-dot` prints the same AST in Graphviz DOT format for
  report and presentation diagrams.
- `Experiment2` without arguments defaults to `--tac`.
- `Parsers.forTree(...)` constructs the parser mode used by lab 2.
- `Parsers.forTac(...)` constructs the Bison-generated parser mode used by
  lab 3.

## Grammar Scope

`Parser.java` contains the `Parser` interface and the recursive-descent parser
implementation used by lab 2 tree output. `src/TacBisonParser.y` contains the
Bison grammar used to generate the lab 3 parser into
`build/generated/src/TacBisonParser.java` during `make build`.
`Experiment2.java` owns stdin/stdout mode selection and does not depend on
parser implementation flags directly.

- Program: `P -> L+`.
- Statement line: `L -> S ;`.
- Assignment: `S -> id = E`.
- Conditional: `S -> if C then S S'`, with `S' -> else S | epsilon`.
- Loop: `S -> while C do S`.
- Compound statement: `S -> begin L_list end`.
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

Lab 3 syntax is recognized by the Bison-generated parser. The generated parser
builds a small AST, and `TacEmitter` traverses that AST with the existing
`CodeGenerator` so label, temporary, and formatting behavior stays aligned with
the lab 3 fixtures. `TacAstPrinter` can print that AST for report and
presentation use without changing default TAC output; `TacAstDotPrinter` can
emit the same tree as Graphviz DOT text.

## Test Coverage

- `tests/lab2_tree_sample.*` protects the lab 2 syntax-tree output.
- `tests/lab2_tree_invalid_octal.*` protects parser diagnostics for invalid
  numeric input in syntax-tree mode.
- `tests/lab3_tac_sample.*` protects the parser path used by lab 3 TAC output.
- `tests/lab3_tac_precedence.*` protects expression precedence and
  parenthesized expression parsing through TAC output.
- `tests/lab3_tac_nested_control.*` protects nested `while` and `if/else`
  parsing through TAC output.
- `tests/lab3_ast_sample.*` protects Bison-path AST display.
- `tests/lab3_ast_dot_sample.*` protects Graphviz DOT AST display.
- Add focused fixtures before changing grammar acceptance, error messages, or
  tree formatting.
