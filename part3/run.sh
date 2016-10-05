rm -f *.pdf *.fst s-*.txt f-*.txt result-*.txt result-*.fst

echo "Cleaned dir"

if [ $# != 0 ] && [ $1 = "-c" ]; then
	exit 0
fi

# compiles transducer and generates .pdf
python3 ../compact2fst.py p3-compact.txt > p3.txt 
fstcompile --isymbols=../syms.txt --osymbols=../syms.txt p3.txt | fstarcsort > p3.fst
fstdraw --portrait --isymbols=../syms.txt --osymbols=../syms.txt p3.fst | dot -Tpdf  > p3.pdf
echo "Compiled transducer"

# compiles and composes tests
array=( rato hoje quanto paz celofane calar gelo galo xerox cisterna calo cloroformio )
for i in "${array[@]}"
do
	echo "Compiling test word \"$i\""
	python3 ../word2fst.py $i > s-$i.txt
	fstcompile --isymbols=../syms.txt --osymbols=../syms.txt s-$i.txt | fstarcsort > s-$i.fst
 	fstcompose s-$i.fst p3.fst > result-s-$i.fst
	fstdraw --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst | dot -Tpdf  > s-$i.pdf
	fstprint --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst > result-s-$i.txt
done

# compiles and composes failure tests
array=( xarope )
for i in "${array[@]}"
do
        echo "Compiling test word \"$i\""
        python3 ../word2fst.py $i > f-$i.txt

        fstcompile --isymbols=../syms.txt --osymbols=../syms.txt f-$i.txt | fstarcsort > f-$i.fst
        fstcompose f-$i.fst p3.fst > result-f-$i.fst
        fstdraw --isymbols=../syms.txt --osymbols=../syms.txt result-f-$i.fst | dot -Tpdf > f-$i.pdf
        fstprint --isymbols=../syms.txt --osymbols=../syms.txt result-f-$i.fst > result-f-$i.txt
done
