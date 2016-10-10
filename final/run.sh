rm *.pdf *.fst *.txt 

if [ $# != 0 ] && [ $1 = "-c" ]; then
	for i in `seq 1 4`; do
        	cd ../part$i
	        ./run.sh -c
	done
	exit
fi

# compile the partial transducers
for i in `seq 1 4`; do
	echo "Compiling part $i"
	cd ../part$i
	./run.sh
done

cd ../final

# composing final transducer
echo "Composing final transducer"

fstcompose ../part1/p1.fst ../part2/p2.fst > 12.fst
fstcompose 12.fst ../part3/p3.fst > 123.fst
fstcompose 123.fst ../part4/p4.fst > final.fst
rm 12.fst 123.fst

fstdraw --isymbols=../syms.txt --osymbols=../syms.txt final.fst | dot -Tpdf > final.pdf
fstprint --isymbols=../syms.txt --osymbols=../syms.txt final.fst > final.txt 

# testing final transducer
echo "Generating tests for final transducer"

array=( as baroes praia mares por antes navegados passaram ainda nunca pires lusitana excitado armas assinalados ocidental )
for i in "${array[@]}"
do 
	python ../word2fst.py $i > s-$i.txt
	fstcompile --isymbols=../syms.txt --osymbols=../syms.txt s-$i.txt | fstarcsort > s-$i.fst
	fstcompose s-$i.fst final.fst > result-s-$i.fst
	fstdraw --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst | dot -Tpdf > s-$i.pdf
	fstprint --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst > result-s-$i.txt
done	
echo "Done"
