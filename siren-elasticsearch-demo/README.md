# SIREn ElasticSearch Plugin Demo

## Introduction

This module provides a demonstration of the integration of SIREn in Elasticsearch. 
For more information on how to run the demo, please refer to the file BUILD.md at the
root of the siren-elasticsearch-demo directory.

## Module Description

* **src/example**

    The template of the Elasticsearch example.

## Note about Data

The data used in this demo are not identical to the data of the Solr demo. They
are only cleaned up for ElasticSearch. For example the original Solr 
NCPR dataset contains documents with property with heterogeneous types. In the
dataset of this demo, we preserved only documents with the major
property type, e.g., if most docs have the long-typed property "age" then
all docs where the "age" property has a string or object value would
be removed. Note that this is a restriction of ElasticSearch only.
SIREn can handle same properties with different types without issues. 

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.
