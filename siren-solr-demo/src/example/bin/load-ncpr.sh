#! /bin/bash

CP=./post.jar
INPUT=./datasets/ncpr/ncpr-with-datatypes.json

java -cp $CP com.sindicetech.siren.demo.loader.Loader -f $INPUT