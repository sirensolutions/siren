#!/bin/bash

r="\e[31m" #red
lr="\e[91m" #light red
g="\e[32m" #green
lg="\e[92m"
lgr="\e[37m" #light gray
dgr="\e[90m" # dark gray
d="\e[39m" # default

# prints with interpreting color sequences and reverts text formatting to default
print() {
    echo -en "$lgr$1$d"
}

escape() {
  sed 's/\$//g'
}

checkResponse() {
  [[ "$1" =~ error ]] && echo "$lr$1" || echo "${lg}OK"
}

[ "$1" = "-pretty" ] && PRETTY="?pretty" || PRETTY=""

print "Creating index ${lg}movies: "
RESPONSE=$(curl -XPOST "localhost:9200/movies/$PRETTY" 2> /dev/null)
MSG=$(checkResponse $RESPONSE)
print "$MSG\n"

print "Setting mapping for type ${lg}movie$lgr in index ${lg}movies: "
RESPONSE=$(curl -XPUT "http://localhost:9200/movies/movie/_mapping$PRETTY" -d @datasets/movies/mapping.json 2> /dev/null)
MSG=$(checkResponse $RESPONSE)
print "$MSG\n"

i=1
for file in datasets/movies/docs/*.json
do
    print "Indexing doc $lgr#$i: "
    RESPONSE=$(curl -XPUT "http://localhost:9200/movies/movie/$i$PRETTY" -d @$file 2> /dev/null)
    MSG=$(checkResponse $RESPONSE)
    print "$MSG\n"
    i=$((i + 1))
done
