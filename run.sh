CORP_DIR=Corpora/treino
PROFILE_SIZE=100

if [[ $# == 0 ]] || [[ $1 == help ]]; then
	echo "run.sh <command>"
	echo "clean: deletes the author n-grams and profiles" 
	echo "generate: generates the authors' n-grams and profiles"
	echo "help: displays this list"
	exit
fi

# Deletes the n-grams and profiles
if [[ $1 == "clean" ]]; then
	echo "Cleaning directory"
	rm -f *-unigrams.txt *-bigrams.txt temp-*.txt
	exit
fi

# Generates the author n-grams and profiles
if [[ $1 == "generate" ]]; then

	for AUTHOR in $( ls $CORP_DIR ); do
		count=0
		echo "Generating n-grams for author '$AUTHOR'"
		for i in $CORP_DIR/$AUTHOR/*.txt; do
			ngram-count -sort -write1 temp-uni-$count.txt -write2 temp-bi-$count.txt -text "$i"
			let count=count+1
		done
		
		ngram-merge -write $AUTHOR-unigrams.txt temp-uni-*.txt	
		ngram-merge -write $AUTHOR-bigrams.txt temp-bi-*.txt
		rm temp-*.txt
	done
fi


