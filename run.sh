TRAIN_DIR=Corpora/treino
#TEST_DIR=Corpora/teste/500Palavras
TEST_DIR=Corpora/teste/1000Palavras
PROFILE_SIZE=100

if [[ $1 == help ]]; then
	echo "run.sh <command>"
	echo "clean: deletes the author n-grams and profiles" 
	echo "generate: generates the authors' n-grams and profiles"
	echo "help: displays this list"
	exit
fi

# Deletes the n-grams and profiles

if [[ $# == 0 ]] || [[ $1 == "clean" ]]; then
	echo "Cleaning directory"
	rm -f *arpa.txt temp-*.txt norm-*
	
	if [[ $1 == "clean" ]]; then
		exit
	fi
fi



# Generates the author n-grams and profiles
if [[ $# == 0 ]] || [[ $1 == "generate" ]]; then
	for AUTHOR in $( ls $TRAIN_DIR ); do
		echo "Normalizing text files"			

		count=0
		OLDIFS="$IFS"
		IFS=$'\n'
		for i in $( ls $TRAIN_DIR/$AUTHOR ); do
			cat $TRAIN_DIR/$AUTHOR/$i | tr -d "[?|\.|!|:|,|;_\(\)]*" > norm-$i
			
			ngram-count -sort -order 2 -text norm-$i -addsmooth 0 -write temp-$AUTHOR-$count.txt
			let count=count+1
		done
		IFS=$OLDIFS

		# create author profile
		echo "Generating n-grams for author '$AUTHOR'"
		ngram-merge -write $AUTHOR-count.txt temp-$AUTHOR-*.txt
		ngram-count -sort -read $AUTHOR-count.txt -addsmooth 0 -lm $AUTHOR-arpa.txt 
		rm temp-*.txt $AUTHOR-count.txt
	done

	if [[ $1 == "generate" ]]; then
		exit
	fi
fi

if [[ $# == 0 ]] || [[ $1 == "run" ]]; then
	for TEST_FILE in $( ls $TEST_DIR ); do
		BEST_PPL=-1
		BEST_AUTHOR=""

		for AUTHOR in $( ls $TRAIN_DIR ); do	
			# if we want to use only the N most frequent ngrams
			#head -n $PROFILE_SIZE $AUTHOR-bigrams.txt $AUTHOR-profile

			# apply the model to the text and extract the perplexity
			PPL="$(ngram -lm $AUTHOR-arpa.txt -ppl $TEST_DIR/$TEST_FILE  | grep -o "ppl= [0-9]*" | grep -o "[0-9]*")"

			if [[ $BEST_PPL == -1 ]] || [[ $PPL -lt $BEST_PPL ]]; then
				BEST_AUTHOR=$AUTHOR
				BEST_PPL=$PPL
			fi				
		done
		
		echo "The author of the text '$TEST_FILE' is $BEST_AUTHOR (ppl = $BEST_PPL)"
	done
	
	if [[ $1 == "run" ]]; then
		exit
	fi
fi
