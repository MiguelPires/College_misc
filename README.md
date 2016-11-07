
## Requirements

To check if your environment has all the required tools, execute the *requirements.sh* script.

## Usage

The interaction with the project is done solely through the run.sh script, which
 is executed as *./run.sh* or *./run.sh <phase>*, where *<phase>* can take the following values:

 - **clean** - eliminates all generated files and directories
 - **model** - generates the language models for each author and experiment, as long with every necessary file (e.g., normalized/stemmed training files, ARPA files, etc)
 - **evaluate** - runs the language models against the test files and presents the r
esults of each experiment (these are also stored in a "results.txt" file in each experiment)
 - **help** - displays this information

Running the script without specifying a phase runs every phase in the listed order (except the help information).
