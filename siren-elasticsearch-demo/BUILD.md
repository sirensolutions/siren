# SIREn Elasticsearch Demo Build Instructions

## Introduction

Basic steps:
  0) Install SIREn
  1) Build the demo with Maven
  2) Run the Elasticsearch demo 

## Install SIREn

We'll assume you already did this. However, if this is not the case, then
please follows the instructions from the file BUILD.txt at the root of your
SIREn installation.

## Build the demo with Maven

In order to run the demo, we first need to generate the directory that 
contains an instance of Elasticsearch with an example configuration.

From the command line, change (cd) into the directory of the 
siren-elasticsearch-demo module of your SIREn installation. Then, typing

    $ mvn clean package antrun:run

in your shell prompt should run the Maven assembly task.

The Maven assembly task will create a directory 
"./target/siren-elasticsearch-demo-example/" that contains the Elasticsearch
instance.

## Run the ElasticSearch demo

Assuming you are still in the directory of the siren-elasticsearch-demo module, 
change (cd) into the directory of the Elasticsearch example:

    $ cd ./target/siren-elasticsearch-demo-example/

and follow the instructions in the README.md file. 

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.

