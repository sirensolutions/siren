#! /bin/bash

CP=./post.jar
INPUT=./datasets/ncpr/ncpr-with-datatypes.json

java -cp $CP com.sindicetech.siren.demo.ncpr.NCPRIndexer $INPUT