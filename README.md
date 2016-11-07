# Forensic Author Identification

This is a tool that uses n-grams, along with several other techniques, to identify the authors of a set of test files. The unigram/bigram system acts as a baseline and 3 experiments are executed with varying sets of optimizations and techniques, namely:
 - Normalization
 - Stemming
 - Smoothing
 
These techniques are applied in a cumulative way (meaning that the first experiment only applies normalization and the third applies all of them). In the end, the accuracy of the experiments is evaluated to allow us to draw conclusions about the effectiveness of each of them.
The accuracy of the baseline is 41% (5 correct answers out of 12) and the third experiment has an accuracy of 66% (8 out 12), up to 75% at one point. The training and test corpora are included in the project and consist of texts belonging to several Portuguese authors.

## Requirements

To check if your environment has all the required tools, execute the *requirements.sh* script.

## Usage

The interaction with the project is done solely through the run.sh script, which
 is executed as *./run.sh* or *./run.sh \<phase\>*, where *\<phase\>* can take the following values:

 - **clean** - eliminates all generated files and directories
 - **model** - generates the language models for each author and experiment, as long with every necessary file (e.g., normalized/stemmed training files, ARPA files, etc)
 - **evaluate** - runs the language models against the test files and presents the r
esults of each experiment (these are also stored in a "results.txt" file in each experiment)
 - **help** - displays this information

Running the script without specifying a phase runs every phase in the listed order (except the help information).
