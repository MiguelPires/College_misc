CORP_DIR=Corpora/treino

if [ $# != 0 ] && [ $1 = "-c" ]; then
	rm -f *-bigrams.txt temp-*.txt
	exit
fi

for AUTHOR in $( ls $CORP_DIR ); do
count=0
	for i in $CORP_DIR/$AUTHOR/*.txt; do
	echo "$i"
	ngram-count -sort -write-order 2 -text "$i" > temp-$count.txt
	let count=count+1
	done
	
	ngram-merge -write $AUTHOR-bigrams.txt temp-*.txt
	rm temp-*.txt
done
