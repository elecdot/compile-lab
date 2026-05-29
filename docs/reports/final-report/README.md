# Final Report LaTeX Project

This directory contains the formal LaTeX version of the final compiler lab
report. The report content is converted from `../final-report.md`; update that
Markdown source first when changing report facts, then regenerate `main.tex`.

## Build

```sh
make
```

The build uses XeLaTeX and `Noto Sans CJK SC` for Chinese text. SVG diagrams
from `../assets/` are converted into PDF assets before LaTeX compilation.

## Files

- `main.tex`: LaTeX report source.
- `Makefile`: local build entry point.
- `assets/`: generated PDF diagram assets used by LaTeX.

The report also references images under `../member-sources/` and
`../assets/make-test-success.png`.
