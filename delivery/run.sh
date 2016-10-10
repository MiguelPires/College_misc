echo "Cleaning directory"

rm -f *.pdf *.fst testResult.txt transdutorFinal.txt

if [ $# != 0 ] && [ $1 = "-c" ]; then
	echo "Done"
	exit
fi

# compile the partial transducers
for i in `seq 1 4`; do
	echo "Compiling part $i"
	
	python3 compact2fst.py part$i-compact.txt > part$i.txt 
	fstcompile --isymbols=syms.txt --osymbols=syms.txt part$i.txt | fstarcsort > part$i.fst
	fstdraw --portrait --isymbols=syms.txt --osymbols=syms.txt part$i.fst | dot -Tpdf > part$i.pdf
done

# composing final transducer
echo "Composing final transducer"

fstcompose part1.fst part2.fst > 12.fst
fstcompose 12.fst part3.fst > 123.fst
fstcompose 123.fst part4.fst > transdutorFinal.fst
rm 12.fst 123.fst

fstdraw --isymbols=syms.txt --osymbols=syms.txt transdutorFinal.fst | dot -Tpdf > transdutorFinal.pdf
#fstprint --isymbols=syms.txt --osymbols=syms.txt transdutorFinal.fst > transdutorFinal.txt 

# testing final transducer
echo "Compiling test word 'pires'"

fstcompile --isymbols=syms.txt --osymbols=syms.txt pires.txt | fstarcsort > pires.fst
fstcompose pires.fst transdutorFinal.fst > testResult.fst
fstdraw --isymbols=syms.txt --osymbols=syms.txt testResult.fst | dot -Tpdf > testResult.pdf
#fstprint --isymbols=syms.txt --osymbols=syms.txt testResult.fst > testResult.txt

echo "Done"
