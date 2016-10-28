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
	rm -f *arpa.txt temp-*.txt norm-* stemmed-*
	rm -rf experiment[123]
	if [[ $1 == "clean" ]]; then
		exit
	fi
fi

# Generates the author n-grams and profiles
if [[ $# == 0 ]] || [[ $1 == "generate" ]]; then
	# experiment1 only uses simple normalization (removal of punctuation)
	# experiment2 uses normalization and stemming
	# experiment3 uses normalization, stemming and smoothing
	rm -rf experiment[123]
	mkdir experiment1 experiment2 experiment3

	for AUTHOR in $( ls $TRAIN_DIR ); do
		echo "Normalizing text files"			

		COUNT=0
		# we change the filename separator to be \n because
		# some of the text files have spaces in them
		OLDIFS="$IFS"
		IFS=$'\n'
		for i in $( ls $TRAIN_DIR/$AUTHOR ); do
			# normalize text files and move them to every experiment's folder
			cat $TRAIN_DIR/$AUTHOR/$i | tr -d "[?|\.|!|:|,|;_\(\)\+\#\$\%]*" > norm-$AUTHOR-$COUNT.txt
			cp norm-$AUTHOR-$COUNT.txt experiment1/norm-$AUTHOR-$COUNT.txt
			cp norm-$AUTHOR-$COUNT.txt experiment2/norm-$AUTHOR-$COUNT.txt
			cp norm-$AUTHOR-$COUNT.txt experiment3/norm-$AUTHOR-$COUNT.txt
			
			# stem and move the stemmed files to experiments 2 and 3
			python3 stemmer.py norm-$AUTHOR-$COUNT.txt
			cp stemmed-norm-$AUTHOR-$COUNT.txt experiment2/stemmed-norm-$AUTHOR-$COUNT.txt
			cp stemmed-norm-$AUTHOR-$COUNT.txt experiment3/stemmed-norm-$AUTHOR-$COUNT.txt
		
			# generate ngram counts
			# just normalization	
			ngram-count -sort -order 2 -text norm-$AUTHOR-$COUNT.txt -addsmooth 0 -write experiment1/temp-$AUTHOR-$COUNT.txt
			# normalization and stemming
			ngram-count -sort -order 2 -text stemmed-norm-$AUTHOR-$COUNT.txt -addsmooth 0 -write experiment2/temp-$AUTHOR-$COUNT.txt
			# normalization, stemming and LaPlace smoothing
			ngram-count -sort -order 2 -text stemmed-norm-$AUTHOR-$COUNT.txt -addsmooth 1 -write experiment3/temp-$AUTHOR-$COUNT.txt
			
			rm norm-$AUTHOR-$COUNT.txt stemmed-norm-$AUTHOR-$COUNT.txt
			let COUNT=COUNT+1
		done
		IFS=$OLDIFS
		
		# create author profile
		echo "Generating n-grams for author '$AUTHOR'"
		
		cd experiment1
		ngram-merge -write $AUTHOR-count.txt temp-$AUTHOR-*.txt	
		ngram-count -sort -read $AUTHOR-count.txt -addsmooth 0 -lm $AUTHOR-arpa.txt 
		
		cd ../experiment2
		ngram-merge -write $AUTHOR-count.txt temp-$AUTHOR-*.txt
		ngram-count -sort -read $AUTHOR-count.txt -addsmooth 0 -lm $AUTHOR-arpa.txt 
		
		cd ../experiment3
		ngram-merge -write $AUTHOR-count.txt temp-$AUTHOR-*.txt
		ngram-count -sort -read $AUTHOR-count.txt -addsmooth 0 -lm $AUTHOR-arpa.txt
		
		cd ..
	done

	if [[ $1 == "generate" ]]; then
		exit
	fi
fi

if [[ $# == 0 ]] || [[ $1 == "run" ]]; then

	for DIR in experiment*/; do
		echo "Running experiment $( echo "$DIR" | grep -o "[123]" )"
		cd $DIR
		rm results.txt

		for TEST_FILE in $( ls ../$TEST_DIR ); do	
			BEST_PPL=-1
			BEST_AUTHOR=""

			for AUTHOR in $( ls ../$TRAIN_DIR ); do	
				# apply the model to the text and extract the perplexity
				PPL="$(ngram -lm $AUTHOR-arpa.txt -ppl ../$TEST_DIR/$TEST_FILE  | grep -o "ppl= [0-9]*" | grep -o "[0-9]*")"

				if [[ $BEST_PPL == -1 ]] || [[ $PPL -lt $BEST_PPL ]]; then
					BEST_AUTHOR=$AUTHOR
					BEST_PPL=$PPL
				fi				
			done
			echo "$TEST_DIR/$TEST_FILE's author is $BEST_AUTHOR (perplexity = $BEST_PPL)." >> results.txt
		done		
		cd ..
	done
	
	if [[ $1 == "run" ]]; then
		exit
	fi
fi