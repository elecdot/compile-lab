# Tests

Directory entry point for project test cases and expected outputs.

## Scope

- Lexer tests.
- Parser tests.
- Three-address-code tests.
- End-to-end examples.

## Local Conventions

- Keep test fixtures small and focused.
- Use `<area>_<case>.in` for input fixtures and `<area>_<case>.expected` for
  expected output.
- Use stable area names such as `lab1`, `lab23`, `lexer`, `parser`, `tac`, and
  `e2e`.
- Keep `<case>` lowercase, descriptive, and separated with underscores.
- Prefer plain text fixtures that can be reviewed in diffs.
- Keep illustrative samples in `examples/`; keep automated assertions here.
- Do not store large generated logs here; keep only stable expected results.
