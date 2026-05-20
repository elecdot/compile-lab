# Compiler Labs

Repository for compiler-principles lab work.

## Documentation

- [docs/README.md](docs/README.md)
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
- Keep generated files out of Git unless they are required deliverables.

## Current State

- Repository structure and directory-level README files are initialized.
- Imported Java implementations for lab 1 and lab 2 are present under `src/`.
- Characterization fixtures cover lab 1 lexer behavior, lab 2 syntax-tree
  output, and the material-expected lab 3 three-address-code sample.

## Open Loops

- [ ] Confirm implementation language and command-line interface.
- [ ] Refactor imported Java sources behind stable tests.
- [ ] Add detailed design notes under `docs/`.
