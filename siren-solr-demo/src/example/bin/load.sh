#!/bin/bash
# Generic loader for JSON files to Solr/SIREn

CP=./post.jar

java -cp $CP com.sindicetech.siren.demo.loader.Loader $@

