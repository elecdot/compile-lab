# Tests

Directory entry point for project test cases and expected outputs.

## Scope

- Lab 1 lexer-output fixtures.
- Shared lexer contract fixtures.
- Lab 2 syntax-tree fixtures.
- Lab 3 three-address-code fixtures.

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

## Local Conventions

- Run the characterization suite through `make test`.
- Keep lexer contract fixtures broad enough to protect every lab entry point
  that uses the shared scanner.
- Keep test fixtures small and focused.
- Use `<area>_<case>.in` for input fixtures and `<area>_<case>.expected` for
  expected output.
- Use stable area names such as `lab1`, `lab2_tree`, `lab3_tac`, `lexer`,
  `parser`, `tac`, and `e2e`.
- Prefer `lab2_tree` for syntax-tree fixtures and `lab3_tac` for
  three-address-code fixtures when the same source program is used across labs.
- Keep `<case>` lowercase, descriptive, and separated with underscores.
- Prefer plain text fixtures that can be reviewed in diffs.
- Keep illustrative samples in `examples/`; keep automated assertions here.
- Do not store large generated logs here; keep only stable expected results.
