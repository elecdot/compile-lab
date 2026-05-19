# Scripts

Directory entry point for project helper scripts.

Use this directory for repeatable project workflows.

## Scope

- Build helpers.
- Test runners.
- Submission packaging helpers.

## Local Conventions

- Prefer scripts for repeatable workflows that are too long for `Makefile`
  targets.
- Keep scripts deterministic and runnable from the repository root.
- Document required arguments and side effects at the top of each script.
- Do not put one-off local commands here.
