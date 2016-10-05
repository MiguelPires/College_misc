rm -f *.pdf *.fst s-*.txt f-*.txt result-*.txt result-*.fst

echo "Cleaned dir"

if [ $# != 0 ] && [ $1 = "-c" ]; then
	exit 0
fi

# compiles transducer and generates .pdf
python3 ../compact2fst.py p1-compact.txt > p1.txt 
fstcompile --isymbols=../syms.txt --osymbols=../syms.txt p1.txt | fstarcsort > p1.fst
fstdraw --portrait --isymbols=../syms.txt --osymbols=../syms.txt p1.fst | dot -Tpdf  > p1.pdf
echo "Compiled transducer"

# compiles and composes tests
array=( casa asa asar aso exame existe exogeno extra exterior )
for i in "${array[@]}"
do
	echo "Testing the conversion of \"$i\""
	python3 ../word2fst.py $i > s-$i.txt
	fstcompile --isymbols=../syms.txt --osymbols=../syms.txt s-$i.txt | fstarcsort > s-$i.fst
 	fstcompose s-$i.fst p1.fst > result-s-$i.fst
	fstdraw --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst | dot -Tpdf  > s-$i.pdf
	fstprint --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst > result-s-$i.txt
done

# compiles and composes failure tests
array=( ca csar sass deixa passas dextro )
for i in "${array[@]}"
do
        echo "Testing the conversion of \"$i\""
        python3 ../word2fst.py $i > f-$i.txt
        fstcompile --isymbols=../syms.txt --osymbols=../syms.txt f-$i.txt | fstarcsort > f-$i.fst
        fstcompose f-$i.fst p1.fst > result-f-$i.fst
        fstdraw --isymbols=../syms.txt --osymbols=../syms.txt result-f-$i.fst | dot -Tpdf > f-$i.pdf
        fstprint --isymbols=../syms.txt --osymbols=../syms.txt result-f-$i.fst > result-f-$i.txt
done
echo "Ran tests"
