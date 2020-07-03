TOOLS=( "ngram" "ngram-count" "ngram-merge" "sed" "bc" "python3" "grep" )
ALL=true

for UTIL in "${TOOLS[@]}"; do
	which $UTIL &> /dev/null
	if [[ $? -ne 0 ]]; then
		echo "You need to install the tool \"$UTIL\" to run the project"
		ALL=false
	fi
done

python3 -c "import nltk" &> /dev/null
if [[ $? -ne 0 ]]; then
	echo "You need to install the NLTK python module"
	ALL=false
fi

if [[ $ALL = "true" ]]; then
	echo "You have all the required tools"
fi
