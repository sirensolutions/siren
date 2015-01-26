#!/bin/bash

r="\e[31m" #red
lr="\e[91m" #light red
g="\e[32m" #green
lg="\e[92m"
lgr="\e[37m" #light gray
dgr="\e[90m" # dark gray
d="\e[39m" # default

print_help() {
    echo "Usage: "$(basename $0)" indexName [fileWithQuery]"
    echo -e "\t reads query from stdin if fileWithQuery is omitted"
}

# prints with interpreting color sequences and reverts text formatting to default
print() {
    echo -en "$lgr$1$d"
}

checkResponse() {
  [[ "$1" =~ error ]] && echo "$lr$1" || echo "${lg}OK"
}

[ "$1" = "-pretty" ] && PRETTY="?pretty" || PRETTY=""

escape() {
    cat $1 | tr '\n' ' ' #| sed 's/\\/\\\\/g;s/"/\\"/g'
}

LG=$(echo -e $lg)
D=$(echo -e $d)

[ -z "$1" ] && print_help && exit 1

INDEX=$1
shift

[ -z "$1" ] && echo "Enter query"

# if $1 is empty then reads from stdin
QUERY=$(cat $1)

# remove newlines
QUERY=$(echo $QUERY)

curl -X POST "http://localhost:9200/$INDEX/_search?pretty" -d  "$QUERY" 2> /dev/null | sed -r "s/(^.*\"total\".*$)/$LG\1$D/;s/(^.*\"_id\".*$)/$LG\1$D/"

