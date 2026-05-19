.DEFAULT_GOAL := help

BUILD_DIR := build/classes
JAVA_SOURCES := $(wildcard src/*.java)
JAVAC ?= javac

.PHONY: help build test clean

help:
	@printf '%s\n' 'Project command entry point.'
	@printf '%s\n' '  build  - build project artifacts'
	@printf '%s\n' '  test   - run the project test suite'
	@printf '%s\n' '  clean  - remove generated artifacts'

build: $(BUILD_DIR)/.stamp

$(BUILD_DIR)/.stamp: $(JAVA_SOURCES)
	@mkdir -p $(BUILD_DIR)
	$(JAVAC) -encoding UTF-8 -d $(BUILD_DIR) $(JAVA_SOURCES)
	@touch $@

test: build
	@scripts/run_tests.sh

clean:
	@rm -rf build
