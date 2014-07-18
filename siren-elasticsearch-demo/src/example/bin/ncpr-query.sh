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

checkResponse() {
  [[ "$1" =~ error ]] && echo "$lr$1" || echo "${lg}OK"
}

[ "$1" = "-pretty" ] && PRETTY="?pretty" || PRETTY=""

escape() {
    cat $1 | tr '\n' ' '
}

LG=$(echo -e $lg)
D=$(echo -e $d)

for q in datasets/ncpr/q*.json
do
    print "------------------------------------------------------\n"
    print "About to run query $q: \n\n"
    cat $q
    EXPECTED=$(cat $q.result)
    print "\nExpected result: $d$EXPECTED \n"
    read -p "[Press Enter]"
    echo
    QUERY=$(escape $q)

    curl -X POST "http://localhost:9200/ncpr/_search?pretty" -d  "{\"query\":{\"tree\":$QUERY}}" 2> /dev/null | sed -r "s/(^.*\"total\".*$)/$LG\1$D/;s/(^.*\"_id\".*$)/$LG\1$D/"
done
