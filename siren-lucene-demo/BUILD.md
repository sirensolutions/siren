# SIREn ElasticSearch Demo Build Instructions

## Introduction

Basic steps:
  0) Install SIREn
  1) Build the demo with Maven
  2) Run the Lucene demo 

## Install SIREn

We'll assume you already did this. However, if this is not the case, then
please follows the instructions from the file BUILD.txt at the root of your
SIREn installation.

## Build the demo with Maven

From the command line, change (cd) into the directory of the 
siren-lucene-demo module of your SIREn installation. Then, typing

    $ mvn clean package antrun:run

in your shell prompt should run the Maven assembly task.

The Maven assembly task will will generate the 
`target/siren-lucene-demo-jar-with-dependencies.jar` file
and copy the src/datasets data folder to the target/ folder.

## Run the demo

Go to the target/ directory:

    cd target/

Run the movies demo:

    java -cp siren-lucene-demo-jar-with-dependencies.jar com.sindicetech.siren.demo.movie.MovieDemo

Run the BNB demo:

    java -cp siren-lucene-demo-jar-with-dependencies.jar com.sindicetech.siren.demo.bnb.BNBDemo

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.

