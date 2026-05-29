# Source

Directory entry point for implementation source code.

## Scope

- Shared lexer and token model.
- Lab command entry points.
- Recursive-descent parser implementation for lab 2 tree output.
- Bison grammar, adapter, AST, and emitter for lab 3 TAC output.
- Three-address-code generation helpers.
- Fixed-grammar MiniYacc/SLR demonstration for report and presentation support.
- Shared model types used by compiler subsystems.

## Entry Points

- `CompilerLab.java`: executable jar entry point. `make dist` packages it as
  `dist/compiler-lab.jar`, with subcommands for lab 1, lab 2 tree output,
  lab 3 TAC/AST modes, and MiniYacc/SLR demos.
- `Main.java`: lab 1 lexer output entry point using the shared lexer.
- `Experiment2.java`: lab 2 and lab 3 command-line entry point. Use `--tree`
  for syntax-tree output, `--tac` for three-address-code output, `--tac-opt`
  for constant-folded TAC, `--ast` for Bison-path AST text, and `--ast-dot`
  for Graphviz DOT AST output.
- `Parser.java`: parser interface and recursive-descent implementation used by
  lab 2 syntax-tree output.
- `TacBisonParser.y`: Bison grammar for the lab 3 parser generated into
  `build/generated/src/TacBisonParser.java`.
- `BisonTacParser.java`: adapter from the shared lexer to the Bison-generated
  lab 3 parser.
- `TacAst.java`, `TacAstPrinter.java`, `TacAstDotPrinter.java`, and
  `TacEmitter.java`: lab 3 AST model, report-facing AST displays, and TAC
  traversal.
- `TacOptimizer.java`: AST-level constant folding used by `--tac-opt`.
- `MiniSlrDemo.java`: standalone SLR(1) demonstration for a fixed expression
  grammar; prints LR(0) item sets, GOTO transitions, and an ACTION/GOTO table.
- `Lexer.java`: shared lexer used by lab 1, lab 2, and lab 3 entry points.
- `Token.java`: shared token model with lab-specific output helpers.
- `ExprAttr.java`: expression attribute model used during TAC generation.
- `CodeGenerator.java`: label, temporary, and formatted TAC output helper.

## Local Conventions

- Keep source files organized by compiler subsystem.
- Keep `CompilerLab` as a thin command dispatcher; implementation behavior
  should remain in the lab-specific entry points and helper classes.
- Prefer clear module boundaries over large catch-all files.
- Treat `Lexer` and `Token` as shared interfaces across lab entries; update
  shared lexer fixtures before changing their behavior.
- Preserve lab entry behavior with tests before extracting more code from
  `Experiment2.java`.
- Keep `TacBisonParser.y` as the source of truth for the generated lab 3 parser;
  do not edit `build/generated/src/TacBisonParser.java` directly.
- Keep public interfaces documented close to their declarations.
- Do not add generated sources here unless the generation step is part of the
  documented build.
