rm -f *.pdf *.fst s-*.txt f-*.txt result-*.txt result-*.fst

echo "Cleaned dir"

if [ $# != 0 ] && [ $1 = "-c" ]; then
	exit 0
fi

# compiles transducer and generates .pdf
python3 ../compact2fst.py p2-compact.txt > p2.txt 
fstcompile --isymbols=../syms.txt --osymbols=../syms.txt p2.txt | fstarcsort > p2.fst
fstdraw --portrait --isymbols=../syms.txt --osymbols=../syms.txt p2.fst | dot -Tpdf  > p2.pdf
echo "Compiled transducer"

# compiles and composes tests
array=( acho galho ganho carro massa messe marreta espelho tacanho )
for i in "${array[@]}"
do
	echo "Compiling test word \"$i\""
	python3 ../word2fst.py $i > s-$i.txt
	fstcompile --isymbols=../syms.txt --osymbols=../syms.txt s-$i.txt | fstarcsort > s-$i.fst
 	fstcompose s-$i.fst p2.fst > result-s-$i.fst
	fstdraw --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst | dot -Tpdf  > s-$i.pdf
	fstprint --isymbols=../syms.txt --osymbols=../syms.txt result-s-$i.fst > result-s-$i.txt
done

# compiles and composes failure tests
array=( mestre galo asco )
for i in "${array[@]}"
do
        echo "Compiling test word \"$i\""
        python3 ../word2fst.py $i > f-$i.txt

        fstcompile --isymbols=../syms.txt --osymbols=../syms.txt f-$i.txt | fstarcsort > f-$i.fst
        fstcompose f-$i.fst p2.fst > result-f-$i.fst
        fstdraw --isymbols=../syms.txt --osymbols=../syms.txt result-f-$i.fst | dot -Tpdf > f-$i.pdf
        fstprint --isymbols=../syms.txt --osymbols=../syms.txt result-f-$i.fst > result-f-$i.txt
done
