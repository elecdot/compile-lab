.DEFAULT_GOAL := help

.PHONY: help build test clean

help:
	@printf '%s\n' 'Project command entry point.'
	@printf '%s\n' '  build  - build project artifacts'
	@printf '%s\n' '  test   - run the project test suite'
	@printf '%s\n' '  clean  - remove generated artifacts'

build:
	@printf '%s\n' 'build target is not implemented yet'

test:
	@printf '%s\n' 'test target is not implemented yet'

clean:
	@printf '%s\n' 'clean target is not implemented yet'
