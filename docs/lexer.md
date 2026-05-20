# Lexer

Purpose: document the shared lexical contract used by lab 1, lab 2, and lab 3.

## Entry Points

- `Main.java` reads stdin, scans through `Lexer`, and prints lab 1 token lines.
- `Experiment2.java` reads stdin, scans through `Lexer`, and consumes parser
  tokens for lab 2 syntax-tree output and lab 3 TAC generation.
- `Lexer.nextToken()` returns `null` at EOF.

## Token Model

`Token` is the shared token object for lab entries.

- `type`: token category used by output and parser matching.
- `value`: normalized semantic value used by parser and code generation.
- `lexeme`: original source slice, kept when output rules need the raw text.
- `lab1Attribute()`: lab 1 display attribute. Identifiers and valid integers
  print their normalized value; other recognized tokens print `-`; unknown
  tokens print the original lexeme.

## Token Kinds

- Keywords: `IF`, `THEN`, `ELSE`, `WHILE`, `DO`, `BEGIN`, `END`.
- Identifiers: `IDN`.
- Integers: `DEC`, `OCT`, `HEX` with normalized decimal `value`.
- Invalid integers: `ILOCT`, `ILHEX`, `ILNUM` with `value` set to `-`.
- Operators and delimiters: `ADD`, `SUB`, `MUL`, `DIV`, `EQ`, `GT`, `LT`,
  `GE`, `LE`, `NEQ`, `SLP`, `SRP`, `SEMI`.
- Fallback: `UNKNOWN` for non-empty lexemes that do not match a known class.

## Scanner Rules

- Whitespace separates tokens and is not emitted.
- Operator and delimiter characters terminate the preceding lexeme.
- `>=`, `<=`, and `<>` are recognized before single-character relational
  operators.
- Decimal, octal, and hexadecimal literals are converted to decimal strings in
  `Token.value` when valid.
- Invalid numeric lexemes remain a single token so lab 1 output and parser error
  handling see the same source unit.

## Test Coverage

- `tests/lab1_sample.*` protects the lab 1 handout-style output.
- `tests/lexer_contract.*` protects scanner behavior shared by all lab entries.
- Run both through `make test` rather than calling `scripts/run_tests.sh`
  directly.
