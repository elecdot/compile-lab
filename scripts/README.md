# Scripts

Directory entry point for project helper scripts.

Use this directory for repeatable project workflows.

## Scope

- Build helpers used by `Makefile` targets.
- Test runners used by `Makefile` targets.
- Submission packaging helpers.

## Files

- `run_tests.sh`: executes the checked fixture suite after `make build` has
  produced `build/classes`.

## Local Conventions

- Treat `Makefile` as the public entry point; scripts should support Make
  targets rather than replace them.
- Prefer scripts for repeatable workflows that are too long for `Makefile`
  targets.
- Keep scripts deterministic and runnable from the repository root.
- Document required arguments and side effects at the top of each script.
- Do not put one-off local commands here.
