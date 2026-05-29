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


## Open Loops
