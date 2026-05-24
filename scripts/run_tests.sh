#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
BUILD_DIR=${BUILD_DIR:-"$ROOT_DIR/build/classes"}
JAVA=${JAVA:-java}
JAVA_FLAGS=${JAVA_FLAGS:-"-XX:-UsePerfData"}

if [ ! -d "$BUILD_DIR" ]; then
  printf '%s\n' "Missing build directory: $BUILD_DIR" >&2
  printf '%s\n' "Run make build first." >&2
  exit 1
fi

TMP_DIR=$(mktemp -d "${TMPDIR:-/tmp}/compiler-lab-tests.XXXXXX")
trap 'rm -rf "$TMP_DIR"' EXIT HUP INT TERM

run_case() {
  case_name=$1
  shift
  input_file="$ROOT_DIR/tests/$case_name.in"
  expected_file="$ROOT_DIR/tests/$case_name.expected"
  actual_file="$TMP_DIR/$case_name.actual"

  "$JAVA" $JAVA_FLAGS -cp "$BUILD_DIR" "$@" < "$input_file" > "$actual_file"
  diff -u "$expected_file" "$actual_file"
  printf 'ok %s\n' "$case_name"
}

run_case lab1_sample Main
run_case lexer_contract Main
run_case lab2_tree_sample Experiment2 --tree
run_case lab2_tree_invalid_octal Experiment2 --tree
run_case lab3_tac_sample Experiment2 --tac
run_case lab3_tac_precedence Experiment2 --tac
run_case lab3_tac_nested_control Experiment2 --tac
run_case lab3_tac_relop_extended Experiment2 --tac
run_case lab3_tac_compound Experiment2 --tac
run_case lab3_tac_dangling_else Experiment2 --tac
run_case lab3_tac_error_recovery Experiment2 --tac
run_case lab3_tac_error_missing_rparen Experiment2 --tac
run_case lab3_tac_error_missing_then Experiment2 --tac
run_case lab3_tac_error_compound_recovery Experiment2 --tac
run_case lab3_tac_error_multiple Experiment2 --tac
run_case lab3_ast_sample Experiment2 --ast
run_case lab3_ast_dot_sample Experiment2 --ast-dot
run_case lab3_tac_constant_folding Experiment2 --tac-opt
run_case minislr_table MiniSlrDemo
run_case minislr_dot MiniSlrDemo --dot
