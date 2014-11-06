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

checkResponse() {
  [[ "$1" =~ error ]] && echo "$lr$1" || echo "${lg}OK"
}

[ "$1" = "-pretty" ] && PRETTY="?pretty" || PRETTY=""

escape() {
    cat $1 | tr '\n' ' '
}

LG=$(echo -e $lg)
D=$(echo -e $d)

# Detect os type for sed
if [[ "$OSTYPE" == "linux-gnu" ]]; then
        SED_FLAGS="-r"
elif [[ "$OSTYPE" == "darwin"* ]]; then
        SED_FLAGS="-E"
else
        SED_FLAGS="-r"
fi

for q in datasets/bnb/q*.json
do
    print "------------------------------------------------------\n"
    print "About to run query $q: \n\n"
    cat $q
    EXPECTED=$(cat $q.result)
    print "\nExpected result: $d$EXPECTED \n"
    read -p "[Press Enter]"
    echo
    QUERY=$(escape $q)
    curl -X POST "http://localhost:9200/bnb/_search?pretty" -d  "{\"query\":{\"tree\":$QUERY}}" 2> /dev/null | sed $SED_FLAGS "s/(^.*\"total\".*$)/$LG\1$D/;s/(^.*\"_id\".*$)/$LG\1$D/"
done
