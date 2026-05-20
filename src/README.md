# Source

Directory entry point for implementation source code.

## Scope

- Shared lexer and token model.
- Lab command entry points.
- Recursive-descent parser implementation.
- Three-address-code generation helpers.
- Shared model types used by compiler subsystems.

## Entry Points

- `Main.java`: lab 1 lexer output entry point using the shared lexer.
- `Experiment2.java`: lab 2 and lab 3 command-line entry point. Use `--tree`
  for syntax-tree output and `--tac` for three-address-code output.
- `Parser.java`: parser interface and recursive-descent implementation used by
  lab 2 and lab 3.
- `Lexer.java`: shared lexer used by lab 1, lab 2, and lab 3 entry points.
- `Token.java`: shared token model with lab-specific output helpers.
- `ExprAttr.java`: expression attribute model used during TAC generation.
- `CodeGenerator.java`: label, temporary, and formatted TAC output helper.

## Local Conventions

- Keep source files organized by compiler subsystem.
- Prefer clear module boundaries over large catch-all files.
- Treat `Lexer` and `Token` as shared interfaces across lab entries; update
  shared lexer fixtures before changing their behavior.
- Preserve lab entry behavior with tests before extracting more code from
  `Experiment2.java`.
- Keep public interfaces documented close to their declarations.
- Do not add generated sources here unless the generation step is part of the
  documented build.
