# SIREn Demo Build Instructions

## Introduction

Basic steps:
  0) Install SIREn
  1) Build the demo with Maven
  2) Run the Solr demo

## Install SIREn

We'll assume you already did this. However, if this is not the case, then
please follows the instructions from the file BUILD.txt at the root of your
SIREn installation.

## Build the demo with Maven

In order to run the demo, we first need to (1) build a jar that contains the
binary of the demo and of all its dependencies, and (2) generate the directory
that contains an instance of Solr with an example configuration.

From the command line, change (cd) into the directory of the siren-solr-demo 
module of your SIREn installation. Then, typing

    $ mvn clean package assembly:single antrun:run

at your shell prompt should run the Maven assembly task.

The Maven assembly task will create the jar with dependencies at the location
"./target/siren-solr-demo-jar-with-dependencies.jar" (with the appropriate
version), and a directory "./target/siren-solr-demo-example/" that contains 
the Solr instance.

## Run the Solr demo

Assuming you are still in the directory of the siren-solr-demo module, change 
(cd) into the directory of the Solr example:

    $ cd ./target/siren-solr-demo-example/

and follow the instructions in the README.md file. Note that in the
siren-solr-demo-example/ folder siren-solr-demo-jar-with-dependencies.jar is 
renamed to post.jar to mimic the Apache Solr distribution and example. The jar
contains a SIREn loader too.

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.

