# Source

Directory entry point for implementation source code.

## Scope

- Lexer implementation.
- Parser implementation.
- Code generation implementation.
- Program entry point.
- Shared model types used by compiler subsystems.

## Entry Points

- `Main.java`: lab 1 lexer output entry point using the shared lexer.
- `Experiment2.java`: imported lab 2 parser and lab 3 code-generation entry
  point. Use `--tree` for syntax-tree output and `--tac` for
  three-address-code output.
- `Lexer.java`: shared lexer used by lab 1, lab 2, and lab 3 entry points.
- `Token.java`: shared token model with lab-specific output helpers.
- `ExprAttr.java`: expression attribute model used during TAC generation.
- `CodeGenerator.java`: label, temporary, and formatted TAC output helper.

## Local Conventions

- Keep source files organized by compiler subsystem.
- Prefer clear module boundaries over large catch-all files.
- Keep public interfaces documented close to their declarations.
- Do not add generated sources here unless the generation step is part of the
  documented build.
