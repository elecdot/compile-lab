# MiniYacc SLR Demo

Purpose: document the standalone SLR analysis-table demonstration used for the
lab 2/3 report and presentation.

## Scope

`src/MiniSlrDemo.java` is an independent parser-generator principle demo. It
does not replace the GNU Bison parser used by the lab 3 TAC path, and it does
not parse the full experiment language.

The demo uses this fixed expression grammar:

```text
(0) S' -> E
(1) E -> E + T
(2) E -> T
(3) T -> T * F
(4) T -> F
(5) F -> ( E )
(6) F -> id
```

## Output

Run:

```sh
make build
java -cp build/classes MiniSlrDemo
```

The program prints:

- the numbered grammar productions;
- the canonical LR(0) item sets;
- GOTO transitions between item-set states;
- a state-indexed ACTION/GOTO analysis table.

ACTION cells use standard compact notation:

- `sN`: shift and go to state `N`;
- `rK`: reduce by production `K`;
- `acc`: accept;
- empty cell: no valid parser action.

GOTO cells contain the target state for nonterminals `E`, `T`, and `F`.

## Design Boundary

This file is deliberately small and fixed-scope. Its purpose is to show that
closure, GOTO, FOLLOW-based reductions, and ACTION/GOTO table construction are
understood and reproducible. The production parser remains `TacBisonParser.y`
because the experiment's main target is reliable TAC generation for the lab
language.
