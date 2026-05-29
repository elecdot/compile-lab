# Tests

Directory entry point for project test cases and expected outputs.

## Scope

- Lab 1 lexer-output fixtures.
- Shared lexer contract fixtures.
- Lab 2 syntax-tree fixtures.
- Lab 3 three-address-code fixtures.
- MiniYacc/SLR analysis-table demonstration fixtures.
- Executable-jar smoke coverage for representative lab modes.

## Fixtures

- `lab1_sample.*`: lab 1 token output sample.
- `lexer_contract.*`: shared scanner behavior used by all lab entries.
- `lab2_tree_sample.*`: syntax-tree output for `Experiment2 --tree`.
- `lab2_tree_invalid_octal.*`: parser error output for an invalid octal
  literal in syntax-tree mode.
- `lab3_tac_sample.*`: three-address-code output for `Experiment2 --tac`.
- `lab3_tac_precedence.*`: TAC output for expression precedence and
  parenthesized expressions.
- `lab3_tac_nested_control.*`: TAC output for nested `while` and `if/else`
  control flow.
- `lab3_tac_relop_extended.*`: TAC output for extended relational operators
  `>=`, `<=`, and `<>`.
- `lab3_tac_compound.*`: TAC output for a `begin ... end` compound statement
  used as a loop body.
- `lab3_tac_dangling_else.*`: TAC output proving `else` binds to the nearest
  unmatched `if`.
- `lab3_tac_error_recovery.*`: Bison parser recovery from one malformed
  statement followed by TAC output for the next valid statement.
- `lab3_tac_error_missing_rparen.*`: recovery from a parenthesized expression
  missing its closing `)`.
- `lab3_tac_error_missing_then.*`: recovery from an `if` statement missing
  `then`.
- `lab3_tac_error_compound_recovery.*`: recovery from a malformed statement
  inside `begin ... end`, followed by later block and top-level TAC.
- `lab3_tac_error_multiple.*`: collection of multiple syntax errors before
  continuing with the next valid statement.
- `lab3_tac_error_invalid_lexemes.*`: recovery from invalid octal,
  hexadecimal, numeric, and unknown tokens in assignment expressions.
- `lab3_tac_error_invalid_condition.*`: recovery from invalid literals in
  conditional and loop predicates.
- `lab3_ast_sample.*`: AST text output for the Bison parser path used by lab 3.
- `lab3_ast_dot_sample.*`: Graphviz DOT AST output for report and presentation
  diagrams.
- `lab3_tac_constant_folding.*`: optimized TAC output for AST-level constant
  folding through `Experiment2 --tac-opt`.
- `minislr_table.*`: fixed expression grammar SLR demonstration, including
  LR(0) item sets, GOTO transitions, and the ACTION/GOTO analysis table.
- `minislr_dot.*`: Graphviz DOT output for the fixed expression grammar LR(0)
  state automaton.

## Local Conventions

- Run the characterization suite through `make test`.
- `make test` also builds `dist/compiler-lab.jar` and runs smoke checks through
  `java -jar` so the submission executable stays valid.
- Keep lexer contract fixtures broad enough to protect every lab entry point
  that uses the shared scanner.
- Keep test fixtures small and focused.
- Use `<area>_<case>.in` for input fixtures and `<area>_<case>.expected` for
  expected output.
- Use stable area names such as `lab1`, `lab2_tree`, `lab3_tac`, `lexer`,
  `parser`, `tac`, `minislr`, and `e2e`.
- Prefer `lab2_tree` for syntax-tree fixtures and `lab3_tac` for
  three-address-code fixtures when the same source program is used across labs.
- Keep `<case>` lowercase, descriptive, and separated with underscores.
- Prefer plain text fixtures that can be reviewed in diffs.
- Keep illustrative samples in `examples/`; keep automated assertions here.
- Do not store large generated logs here; keep only stable expected results.
