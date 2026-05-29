# Examples

Directory entry point for illustrative project examples.

Use this directory for sample inputs, sample outputs, and small usage scenarios
that help readers understand how to run the compiler lab programs.

These examples double as report evidence. Each `.out` file is copied from a
passing fixture in `tests/`, so screenshots or excerpts can be traced back to
the automated `make test` suite.

## Scope

- Lab handout examples.
- Demonstration inputs.
- Demonstration outputs.
- Command usage samples.

## Demo Catalog

Run `make build` before executing the Java commands below.

| Purpose | Input | Output | Command |
| --- | --- | --- | --- |
| Lab 1 handout lexer sample | `lab1/handout.in` | `lab1/handout.out` | `java -cp build/classes Main < examples/lab1/handout.in` |
| Lab 2 recursive-descent tree sample | `lab2/tree-sample.in` | `lab2/tree-sample.out` | `java -cp build/classes Experiment2 --tree < examples/lab2/tree-sample.in` |
| Lab 3 handout TAC sample | `lab3/handout-tac.in` | `lab3/handout-tac.out` | `java -cp build/classes Experiment2 --tac < examples/lab3/handout-tac.in` |
| Extended relational operators | `lab3/relop-extended.in` | `lab3/relop-extended.out` | `java -cp build/classes Experiment2 --tac < examples/lab3/relop-extended.in` |
| Compound statement loop body | `lab3/compound-while.in` | `lab3/compound-while.out` | `java -cp build/classes Experiment2 --tac < examples/lab3/compound-while.in` |
| Dangling-else binding | `lab3/dangling-else.in` | `lab3/dangling-else.out` | `java -cp build/classes Experiment2 --tac < examples/lab3/dangling-else.in` |
| Syntax-error recovery | `lab3/syntax-recovery.in` | `lab3/syntax-recovery.out` | `java -cp build/classes Experiment2 --tac < examples/lab3/syntax-recovery.in` |
| Invalid-token recovery | `lab3/invalid-token-recovery.in` | `lab3/invalid-token-recovery.out` | `java -cp build/classes Experiment2 --tac < examples/lab3/invalid-token-recovery.in` |
| Bison-path AST text | `lab3/ast-sample.in` | `lab3/ast-sample.out` | `java -cp build/classes Experiment2 --ast < examples/lab3/ast-sample.in` |
| Bison-path AST DOT | `lab3/ast-dot-sample.in` | `lab3/ast-dot-sample.out` | `java -cp build/classes Experiment2 --ast-dot < examples/lab3/ast-dot-sample.in` |
| Constant-folded TAC | `lab3/constant-folding.in` | `lab3/constant-folding.out` | `java -cp build/classes Experiment2 --tac-opt < examples/lab3/constant-folding.in` |
| MiniYacc SLR table | none | `minislr/table.out` | `java -cp build/classes MiniSlrDemo` |
| MiniYacc LR(0) automaton DOT | none | `minislr/automaton-dot.out` | `java -cp build/classes MiniSlrDemo --dot` |

## Report Usage

- Use `lab1/handout.*` as the basic-requirement evidence for the lexical
  analyzer.
- Use `lab2/tree-sample.*` as concise evidence that the recursive-descent Lab 2
  path still produces the required tree-style output.
- Use `lab3/handout-tac.*` as the basic-requirement evidence for three-address
  code generation.
- Use the remaining `lab3/` examples as extension evidence: richer operators,
  compound statements, dangling-else handling, error recovery, AST display, and
  constant folding.
- Use `minislr/` output when explaining the independent SLR table-generation
  demonstration.

## Local Conventions

- Keep examples readable and intentionally small.
- Prefer names that identify the lab or subsystem, such as
  `lab1_sample.in`, `lab23_sample.in`, or `tac_nested_loop.in`.
- Use examples to explain usage; use `tests/` for assertions that automated
  checks depend on.
- Keep generated output only when it is useful for documentation or manual
  inspection.
- Keep example outputs synchronized with the matching fixture outputs after
  behavior changes.
