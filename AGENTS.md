# AGENTS.md

Read `README.md` and relevant directory-level `README.md` files first
for the project overview and local conventions.

Align implementation with the compiler-principles labt guidelines and requirements found in the `material/` directory.

## Working Notes

- Keep documentation complete and in sync throughout development;
  if you find gaps, fill them in.
- Follow TDD where practical: write or update a failing test before implementing
  behavior, then make it pass and clean up.
- Apply YAGNI: do not add abstractions, features, tools, or configuration that
  are not needed for the current task.
- Apply DRY pragmatically: remove meaningful duplication, but do not introduce
  indirection that makes small code harder to understand.

## Temporary Tools

- Agents may use .cache/ for temporary tool caches, downloaded helper binaries,
  and one-off task dependencies.
- Do not commit .cache/ contents or make normal development, CI, or deployment
  depend on files that only exist in .cache/.
- If a temporary tool becomes part of the regular workflow, promote it into the
  project dependency system and document it.

## Definition Of Done

- The requested change is implemented end to end, not only sketched or partially
  wired.
- Relevant tests are added or updated, and the expected test command has been
  run successfully; if tests cannot run, document the blocker.
- Documentation and README files affected by the change are updated in the same
  pass.
- The solution stays within the requested scope and follows local conventions.
- No unrelated files, generated artifacts, or temporary cache contents are
  included.
- The final response states what changed, how it was verified, and any remaining
  risks or follow-up work.
