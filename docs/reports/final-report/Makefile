LATEX ?= xelatex
SVG_CONVERT ?= rsvg-convert
MAIN := main
PDF := $(MAIN).pdf
SVG_PDFS := assets/lab3-ast.pdf assets/minislr-automaton.pdf

.PHONY: all clean

all: $(PDF)

$(PDF): $(MAIN).tex $(SVG_PDFS)
	$(LATEX) -interaction=nonstopmode -halt-on-error $(MAIN).tex
	$(LATEX) -interaction=nonstopmode -halt-on-error $(MAIN).tex

assets:
	mkdir -p assets

assets/lab3-ast.pdf: ../assets/lab3-ast.svg | assets
	$(SVG_CONVERT) -f pdf -o $@ $<

assets/minislr-automaton.pdf: ../assets/minislr-automaton.svg | assets
	$(SVG_CONVERT) -f pdf -o $@ $<

clean:
	rm -f $(MAIN).aux $(MAIN).log $(MAIN).out $(MAIN).toc $(MAIN).pdf
