# Compiler Labs

Repository for compiler-principles lab work.

## Documentation

- [docs/README.md](docs/README.md)
- [docs/lexer.md](docs/lexer.md)
- [docs/parser.md](docs/parser.md)
- [docs/tac.md](docs/tac.md)
- [material/README.md](material/README.md)

## Layout

- `material/`: original lab handouts.
- `docs/`: project notes and report materials.
- `src/`: source code.
- `examples/`: illustrative inputs, outputs, and usage samples.
- `tests/`: test cases and expected outputs.
- `scripts/`: helper scripts.
- `util/`: project-specific utility files.

Read the README in each directory before adding files there. Directory-level
README files define the local purpose and conventions for that subtree.

## Local Conventions

- Keep this root README focused on repository orientation, not implementation
  details.
- Put directory-specific rules in that directory's README.
- Use `Makefile` as the project command entry point for build, test, run, and
  cleanup workflows.
- Prefer `make build`, `make dist`, `make test`, and `make clean` over direct
  compiler or script commands when documenting workflows.
- Keep generated files out of Git unless they are required deliverables.

## Commands

- `make build`: generate the lab 3 parser with Bison, then compile all Java
  sources into `build/classes`.
- `make dist`: build `dist/compiler-lab.jar`, the executable submission
  artifact.
- `make test`: build and run the checked fixture suite.
- `make clean`: remove generated build artifacts.

`make build` and `make dist` require GNU Bison and a JDK on `PATH`; override
them with `BISON=...`, `JAVAC=...`, or `JAR=...` when needed.

Run the executable jar after `make dist`:

```sh
java -jar dist/compiler-lab.jar lab1 < examples/lab1/handout.in
java -jar dist/compiler-lab.jar tree < examples/lab2/tree-sample.in
java -jar dist/compiler-lab.jar tac < examples/lab3/handout-tac.in
```

Use `java -jar dist/compiler-lab.jar help` for all supported commands.

## Current State

- Java is the implementation language for the current lab entries.
- `CompilerLab.java` is the executable jar entry point.
- `Main.java` is the lab 1 lexer-output entry point.
- `Experiment2.java` is the lab 2 and lab 3 entry point: `--tree` emits the
  syntax tree, and `--tac` emits three-address code.
- `Parser.java` contains the parser interface and recursive-descent
  implementation used by lab 2 syntax-tree output.
- `src/TacBisonParser.y` is the Bison grammar used to generate the lab 3 TAC
  parser during `make build`.
- `Lexer.java` and `Token.java` are shared across lab 1, lab 2, and lab 3.
- The default `make test` suite covers the lab 1 sample, shared lexer contract,
  lab 2 syntax-tree output and invalid-octal error handling, plus lab 3 TAC
  samples for material output, expression precedence, nested control flow,
  language extensions, error recovery, AST display, constant folding, the
  MiniYacc/SLR demo, and executable-jar smoke coverage.

## Open Loops

- [ ] Separate TAC generation policy from parser traversal behind the expanded
  fixture suite.
- [ ] Add focused parser fixtures for additional syntax errors before changing
  grammar acceptance or diagnostics.
- [ ] Keep `docs/` aligned with the source as the lab report takes shape.
