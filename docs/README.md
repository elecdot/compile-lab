# Documentation

Directory entry point for project documentation and report-supporting notes.

Keep this directory for notes that should stay in sync with the implementation
and final lab report.

## Documents

- [Lexer notes](lexer.md): shared scanner contract and lab 1 output rules.
- [Parser notes](parser.md): grammar scope, parser entry points, and syntax
  tree behavior.
- [Three-address code notes](tac.md): lab 3 translation and output
  conventions.
- [MiniYacc SLR demo](minislr-demo.md): fixed-grammar LR(0) item sets,
  GOTO transitions, and ACTION/GOTO table output.
- [Lab 2/3 design report](lab2-3-design-report.md): Bison-based parser and
  three-address-code generation design.
- [Lab 2/3 execution plan](lab2-3-execution-plan.md): authoritative plan for
  remaining extensions, final report work, and presentation preparation.

## Plan

- Keep `lexer.md` as the stable reference for token shape, lab 1 attributes,
  and scanner compatibility across lab entries.
- Keep `parser.md` as the stable reference for grammar coverage and the lab 2
  syntax-tree mode.
- Keep `tac.md` as the stable reference for lab 3 translation rules, labels,
  temporaries, and expected output style.
- Add report-facing notes here only after the implementation contract is
  reflected in tests.

## Local Conventions

- Start each document with a short purpose statement.
- Keep generated files out of documentation unless they are explicitly needed
  for submission.
- Prefer short Markdown notes that point to source files instead of duplicating
  large code snippets.
- Update docs in the same change as the behavior or workflow they describe.
- Use relative links for files inside this repository.
