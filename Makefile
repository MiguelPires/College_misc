OUT = main
IN = bib
all: $(OUT).pdf

$(OUT).pdf: $(OUT).tex
	pdflatex $(OUT)
	bibtex $(OUT)
	pdflatex $(OUT)
	pdflatex $(OUT)

	
clean:
	rm -f *~ main.aux main.pdf main.brf main.bbl main.blg main.log main.out
