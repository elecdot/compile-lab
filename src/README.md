# Source

Directory entry point for implementation source code.

## Scope

- Lexer implementation.
- Parser implementation.
- Code generation implementation.
- Program entry point.

## Entry Points

- `Main.java`: imported lab 1 lexer entry point.
- `Experiment2.java`: imported lab 2 parser and lab 3 code-generation entry
  point with its own lexer. Use `--tree` for syntax-tree output and `--tac`
  for three-address-code output.

## Local Conventions

- Keep source files organized by compiler subsystem.
- Prefer clear module boundaries over large catch-all files.
- Keep public interfaces documented close to their declarations.
- Do not add generated sources here unless the generation step is part of the
  documented build.
