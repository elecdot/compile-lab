# Documentation

Directory entry point for project documentation and report-supporting notes.

Keep this directory for notes that should stay in sync with the implementation
and final lab report.

## Documents

- Lab notes.
- Report notes.
- Design notes.

## Plan

- `lexer.md`: lexical rules, token kinds, invalid-number handling, and scanner
  behavior.
- `parser.md`: grammar choices, grammar rewrites, parser structure, and error
  handling notes.
- `tac.md`: syntax-directed translation notes, temporary and label strategy,
  and three-address-code output conventions.

## Local Conventions

- Start each document with a short purpose statement.
- Keep generated files out of documentation unless they are explicitly needed
  for submission.
- Prefer short Markdown notes that point to source files instead of duplicating
  large code snippets.
- Update docs in the same change as the behavior or workflow they describe.
- Use relative links for files inside this repository.
