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
- Prefer `make build`, `make test`, and `make clean` over direct compiler or
  script commands when documenting workflows.
- Keep generated files out of Git unless they are required deliverables.

## Commands

- `make build`: compile all Java sources into `build/classes`.
- `make test`: build and run the checked fixture suite.
- `make clean`: remove generated build artifacts.

## Current State

- Java is the implementation language for the current lab entries.
- `Main.java` is the lab 1 lexer-output entry point.
- `Experiment2.java` is the lab 2 and lab 3 entry point: `--tree` emits the
  syntax tree, and `--tac` emits three-address code.
- `Lexer.java` and `Token.java` are shared across lab 1, lab 2, and lab 3.
- The default `make test` suite covers the lab 1 sample, shared lexer contract,
  lab 2 syntax-tree sample, and lab 3 TAC sample.

## Open Loops

- [ ] Continue reducing `Experiment2.java` into smaller parser and lab-entry
  modules behind the existing fixtures.
- [ ] Expand fixtures for invalid input, nested control flow, and expression
  precedence before changing parser behavior.
- [ ] Keep `docs/` aligned with the source as the lab report takes shape.
