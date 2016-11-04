DEBUG=false

if [[ "$DEBUG" = true ]]; then
	OLD_PS4=$PS4
	PS4='Line ${LINENO}: '
	set -x
fi

TRAIN_DIR=Corpora/treino
TEST_DIR=Corpora/teste
PROFILE_SIZE=100
WORD_LIMIT=38000

if [[ $1 == help ]]; then
	echo "run.sh | run.sh <command>"
	echo "Running this script with no argument runs every phase sequentially"
	echo "clean: deletes the author n-grams and profiles" 
	echo "profile: generates the authors' n-grams and profiles"
	echo "evaluate: runs the n-gram models in each experiment against test data"
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
if [[ $# == 0 ]] || [[ $1 == "profile" ]]; then
	# experiment1 only uses simple normalization (removal of punctuation)
	# experiment2 uses normalization and stemming
	# experiment3 uses normalization, stemming and smoothing
	rm -rf experiment[123]
	mkdir experiment1 experiment2 experiment3

	for AUTHOR in $( ls $TRAIN_DIR ); do
		echo "Normalizing and stemming text files for $AUTHOR"			
		COUNT=0
		WORDS=0

		# we change the filename separator to be \n because
		# some of the text files have spaces in them
		OLDIFS="$IFS"
		IFS=$'\n'
		for i in $( ls $TRAIN_DIR/$AUTHOR ); do
			# normalize text files and move them to every experiment's folder
			# TODO: it may be beneficial to substitute punctuation by whitespace
			# instead of deleting it. It's something to explroe, when the test set 
			# annotations become available
				
			cat $TRAIN_DIR/$AUTHOR/$i | tr -d "[?|\.|!|:|,|;|_|\(|\)\$#\$\']" > norm-$AUTHOR-$COUNT.txt

			let WORDS+=$(wc -w norm-$AUTHOR-$COUNT.txt | grep -o -E "[0-9]+")
			#cat $TRAIN_DIR/$AUTHOR/$i | tr -d "[?|\.|!|:|,|;|_|\(|\)\$#\$\']" | sed -r "s/ as / /g;s/ os / /g;s/ a / /g;s/ o / /g;s/ de / /g;s/ das / /g;s/ dos / /g;" > norm-$AUTHOR-$COUNT.txt
			#cat $TRAIN_DIR/$AUTHOR/$i |  "(\was\w|\wa\w|\wos\w|\wo\w|\wde\w|\wdas\w|\wdos\w)" > norm-$AUTHOR-$COUNT.txt

			#[?|\.|!|:|,|;|_|\(|\)\$#\$\'])" > norm-$AUTHOR-$COUNT.txt
			#TODO: eliminate stop words
			#cat norm-$AUTHOR_$COUNT.txt | tr -d "( as | a | os | o | de | das | dos )" >  experiment3/norm-$AUTHOR-$COUNT.txt
			cp norm-$AUTHOR-$COUNT.txt experiment1/norm-$AUTHOR-$COUNT.txt
			cp norm-$AUTHOR-$COUNT.txt experiment2/norm-$AUTHOR-$COUNT.txt
			cp norm-$AUTHOR-$COUNT.txt experiment3/norm-$AUTHOR-$COUNT.txt
			
			# stem and move the stemmed files to experiments 2 and 3
			python3 stemmer.py norm-$AUTHOR-$COUNT.txt
			cp stemmed-norm-$AUTHOR-$COUNT.txt experiment2/stemmed-norm-$AUTHOR-$COUNT.txt
			cp stemmed-norm-$AUTHOR-$COUNT.txt experiment3/stemmed-norm-$AUTHOR-$COUNT.txt

			# generate ngram counts
			# just normalization	
			ngram-count -tolower -sort -order 2 -text norm-$AUTHOR-$COUNT.txt -addsmooth 0 -write experiment1/temp-$AUTHOR-$COUNT.txt
			# normalization and stemming
			ngram-count -tolower -sort -order 2 -text stemmed-norm-$AUTHOR-$COUNT.txt -addsmooth 0 -write experiment2/temp-$AUTHOR-$COUNT.txt
			# normalization, stemming and LaPlace smoothing
			ngram-count -tolower -sort -order 2 -text stemmed-norm-$AUTHOR-$COUNT.txt -addsmooth 0 -write experiment3/temp-$AUTHOR-$COUNT.txt
			
			rm norm-$AUTHOR-$COUNT.txt stemmed-norm-$AUTHOR-$COUNT.txt
			let COUNT=COUNT+1
			if [[ $WORDS -gt $WORD_LIMIT ]]; then
				break			
			fi
		done
		IFS=$OLDIFS
		
		# create author profile
		echo "Generating bigram/unigram model for author '$AUTHOR'"
		
		cd experiment1
		FILES_NUM=$(find . -name "temp-$AUTHOR-*.txt" | wc -l)
		if [[ $FILES_NUM -gt 1 ]]; then
			ngram-merge -write $AUTHOR-count.txt temp-$AUTHOR-*.txt	
		else	
			mv temp-$AUTHOR-0.txt $AUTHOR-count.txt
		fi
		
		#head --lines=38000 $AUTHOR-count.txt > trunc-$AUTHOR-count.txt
		ngram-count -tolower -order 2 -sort -read $AUTHOR-count.txt -addsmooth 0 -lm $AUTHOR-arpa.txt 
		
		cd ../experiment2
		
		FILES_NUM=$(find . -name "temp-$AUTHOR-*.txt" | wc -l)
		if [[ $FILES_NUM -gt 1 ]]; then
			ngram-merge -write $AUTHOR-count.txt temp-$AUTHOR-*.txt	
		else	
			mv temp-$AUTHOR-0.txt $AUTHOR-count.txt
		fi
		#head --lines=38000 $AUTHOR-count.txt > trunc-$AUTHOR-count.txt
		ngram-count -tolower -order 2 -sort -read $AUTHOR-count.txt  -addsmooth 0 -lm $AUTHOR-arpa.txt 
		
		cd ../experiment3
		
		# Uses only the N most frequent ngrams
		#for STEMMED_FILE in temp-$AUTHOR-*.txt; do
		#	LEN=$(wc -l $STEMMED_FILE | grep -o -E "[0-9]+[0-9][0-9]" | tr -d '\n ')
		#	HALF=$(echo "$LEN/2" | bc )
		#	head --lines=$HALF $STEMMED_FILE > $STEMMED_FILE-rand
		#	rm $STEMMED_FILE
		#	mv $STEMMED_FILE-rand $STEMMED_FILE
		#done
		FILES_NUM=$(find . -name "temp-$AUTHOR-*.txt" | wc -l)
		if [[ $FILES_NUM -gt 1 ]]; then
			ngram-merge -write $AUTHOR-count.txt temp-$AUTHOR-*.txt	
		else	
			mv temp-$AUTHOR-0.txt $AUTHOR-count.txt
		fi

		# o resultado do merge nao ta sorted e portanto isto descarta coisas arbitrarias
		# nao se devia de fazer a truncacao aqui devia ser ao consumir o texto
		# fazer wordcount dos ficheiros e parar quando se excede um limite
		#head --lines=38000 $AUTHOR-count.txt > trunc-$AUTHOR-count.txt
		#echo "$AUTHOR lines: $( wc -l $AUTHOR-count.txt | grep -o -E "[0-9]+" )"
		ngram-count -tolower -order 2 -sort -read $AUTHOR-count.txt -addsmooth 0 -lm $AUTHOR-arpa.txt
		
		cd ..
	done

	if [[ $1 == "profile" ]]; then
		exit
	fi
fi

authors=( "JoseSaramago" "AlmadaNegreiros" "LuisaMarquesSilva" "EcaDeQueiros" "CamiloCasteloBranco" "JoseRodriguesSantos" )

if [[ $# == 0 ]] || [[ $1 == "evaluate" ]]; then
	for DIR in experiment*/; do
		CORRECT=0
		TOTAL=0

		echo "Running experiment $( echo "$DIR" | grep -o "[123]" )"
		cd $DIR
		rm -f results.txt

		for SUB_DIR in $( ls ../$TEST_DIR ); do
			
			COUNT=1
			for TEST_FILE in $( ls ../$TEST_DIR/$SUB_DIR ); do	
				BEST_PPL=-1
				BEST_AUTHOR=""

				for AUTHOR in $( ls ../$TRAIN_DIR ); do	
					# normalize or stem the test files
					cat ../$TEST_DIR/$SUB_DIR/$TEST_FILE | tr -d "[?|\.|!|:|,|;|_|\(|\)\$#\$\']" > $TEST_FILE-normed.txt
					python3 ../stemmer.py $TEST_FILE-normed.txt
					# apply the model to the text and extract the perplexity

					DIR_NO=$( echo "$DIR" | grep -o "[123]" ) 
					if [[ $DIR_NO = "1" ]]; then
						PPL="$( ngram -skipoovs -tolower -lm $AUTHOR-arpa.txt -ppl $TEST_FILE-normed.txt | grep -o "ppl= [0-9]*" | grep -o "[0-9]*" )"
					fi
					if [[ $DIR_NO = "2" || $DIR_NO = "3" ]]; then
						PPL="$( ngram -skipoovs -tolower -lm $AUTHOR-arpa.txt -ppl stemmed-$TEST_FILE-normed.txt | grep -o "ppl= [0-9]*" | grep -o "[0-9]*" )"
					fi		
					
					if [[ $BEST_PPL == -1 ]] || [[ $PPL -lt $BEST_PPL ]]; then
						BEST_AUTHOR=$AUTHOR
						BEST_PPL=$PPL
					fi				
				done
			echo "$SUB_DIR/$TEST_FILE's author is $BEST_AUTHOR (perplexity = $BEST_PPL)." >> results.txt
			
			if [[ $TEST_FILE = "text$COUNT.txt" ]]; then
				if [[ $BEST_AUTHOR = ${authors[$(expr $COUNT - 1)]} ]]; then
					let CORRECT=CORRECT+1
					echo "Correct author '$BEST_AUTHOR' for $SUB_DIR/text$COUNT"
					
				fi
			fi
			let TOTAL=TOTAL+1					
			let COUNT=COUNT+1
			done	
		done		
		
		acc="Accuracy: $(echo "scale=2;$CORRECT/$TOTAL*100" | bc)%"
		echo $acc
		echo $acc >> results.txt
		cd ..	
	done
	
	if [[ $1 == "evaluate" ]]; then
		exit
	fi
fi

if [[ "DEBUG" = true ]]; then
	set +x
	PS4=$OLD_PS4
fi


