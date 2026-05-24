.DEFAULT_GOAL := help

BUILD_DIR := build/classes
GENERATED_SRC_DIR := build/generated/src
GENERATED_SOURCES := $(GENERATED_SRC_DIR)/TacBisonParser.java
JAVA_SOURCES := $(wildcard src/*.java) $(GENERATED_SOURCES)
JAVAC ?= javac
BISON ?= bison

.PHONY: help build test clean

help:
	@printf '%s\n' 'Project command entry point.'
	@printf '%s\n' '  build  - build project artifacts'
	@printf '%s\n' '  test   - run the project test suite'
	@printf '%s\n' '  clean  - remove generated artifacts'

build: $(BUILD_DIR)/.stamp

$(GENERATED_SRC_DIR):
	@mkdir -p $@

$(GENERATED_SRC_DIR)/TacBisonParser.java: src/TacBisonParser.y | $(GENERATED_SRC_DIR)
	$(BISON) --warnings=conflicts-sr,conflicts-rr -o $@ $<

$(BUILD_DIR)/.stamp: $(JAVA_SOURCES)
	@mkdir -p $(BUILD_DIR)
	$(JAVAC) -encoding UTF-8 -d $(BUILD_DIR) $(JAVA_SOURCES)
	@touch $@

test: build
	@scripts/run_tests.sh

clean:
	@rm -rf build
