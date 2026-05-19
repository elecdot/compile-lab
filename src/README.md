# Source

Directory entry point for implementation source code.

## Scope

- Lexer implementation.
- Parser implementation.
- Code generation implementation.
- Program entry point.

## Local Conventions

- Keep source files organized by compiler subsystem.
- Prefer clear module boundaries over large catch-all files.
- Keep public interfaces documented close to their declarations.
- Do not add generated sources here unless the generation step is part of the
  documented build.
