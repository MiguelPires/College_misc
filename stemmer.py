from nltk.stem import RSLPStemmer
import sys


if len(sys.argv) < 2:
    print("ERROR: No file was specified (ex: ./Dir1/Dir2/test.txt)")
    exit(1)

stemmer = RSLPStemmer()
split_path = str(sys.argv[1]).split("/")
old_filename = sys.argv[1]
new_filename = "stemmed-"+ split_path[len(split_path)-1]

print("Stemming file '"+old_filename+"' and writing in '"+new_filename+"'")
with open(old_filename) as old_file:
    with open(new_filename, 'w+') as new_file:
        for line in old_file:
            new_line = line
            for token in line.split():
                new_line = new_line.replace(token, stemmer.stem(token))

            new_file.write(new_line)