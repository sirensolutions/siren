#!/bin/bash

r="\033[31m" #red
lr="\033[91m" #light red
g="\033[32m" #green
lg="\033[92m"
lgr="\033[37m" #light gray
dgr="\033[90m" # dark gray
d="\033[39m" # default

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

print "Creating index ${lg}ncpr: "
RESPONSE=$(curl -XPOST "localhost:9200/ncpr/$PRETTY" 2> /dev/null)
MSG=$(checkResponse $RESPONSE)
print "$MSG\n"

print "Setting mapping for type ${lg}chargepoint$d in index ${lg}ncpr: "
RESPONSE=$(curl -XPUT "http://localhost:9200/ncpr/chargepoint/_mapping$PRETTY" -d @datasets/ncpr/mapping.json 2> /dev/null)
MSG=$(checkResponse $RESPONSE)
print "$MSG\n"

i=1
while read -r doc
do
    print "Indexing doc $lgr#$i: "
    RESPONSE=$(curl -XPUT "http://localhost:9200/ncpr/chargepoint/$i$PRETTY" -d "$doc" 2> /dev/null)
    MSG=$(checkResponse $RESPONSE)
    print "$MSG\n"
    i=$((i + 1))
done < datasets/ncpr/ncpr-with-datatypes.json
