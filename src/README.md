# Source

Directory entry point for implementation source code.

## Scope

- Lexer implementation.
- Parser implementation.
- Code generation implementation.
- Program entry point.
- Shared model types used by compiler subsystems.

## Entry Points

- `Main.java`: imported lab 1 lexer entry point.
- `Experiment2.java`: imported lab 2 parser and lab 3 code-generation entry
  point with its own lexer. Use `--tree` for syntax-tree output and `--tac`
  for three-address-code output.
- `Lexer.java`: lexer used by the lab 2/lab 3 pipeline.
- `Token.java`: token model used by the lab 2/lab 3 pipeline.
- `ExprAttr.java`: expression attribute model used during TAC generation.
- `CodeGenerator.java`: label, temporary, and formatted TAC output helper.

## Local Conventions

- Keep source files organized by compiler subsystem.
- Prefer clear module boundaries over large catch-all files.
- Keep public interfaces documented close to their declarations.
- Do not add generated sources here unless the generation step is part of the
  documented build.
