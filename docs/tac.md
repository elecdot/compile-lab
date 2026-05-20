# Three-Address Code

Purpose: document the lab 3 three-address-code conventions used by the
implementation and tests.

## Material Rules

- `S -> id = E` emits expression code followed by assignment code.
- `S -> if C then S1` uses `C.true = newlabel` and `C.false = S.next`.
- `S -> if C then S1 else S2` uses distinct labels for the true and false
  branches, and both branches continue at `S.next`.
- `S -> while C do S1` uses `S.begin = newlabel`, `C.true = newlabel`, and
  `C.false = S.next`. The loop body continues at `S.begin`.
- `C -> E1 relop E2` emits `if E1 relop E2 goto C.true` followed by
  `goto C.false`.

## Local Conventions

- `Experiment2 --tree` prints the lab 2 syntax tree.
- `Experiment2 --tac` prints lab 3 three-address code.
- Labels are printed inline with the following instruction when possible,
  matching the lab handout sample style.
- The first program-level continuation label is `L0`; ordinary generated
  labels start at `L1`.
- The material sample is tracked by `tests/lab3_tac_sample.*`.
- Expression precedence is tracked by `tests/lab3_tac_precedence.*`.
- Nested `while` and `if/else` control flow is tracked by
  `tests/lab3_tac_nested_control.*`.
- These fixtures run in the default `make test` suite.
