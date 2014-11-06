# SIREn ElasticSearch Plugin Demo

This directory contains an instance of Elasticsearch with the SIREn plugin
pre-installed and pre-configured. It contains also three demos showing the SIREn Elasticsearch 
plugin in action.

## Launch Elasticsearch

First, run ElasticSearch from this folder:

    $ ./bin/elasticsearch

## Movie Demo

This demo uses a simple small movie dataset. The demo will index a collection of JSON documents 
and execute various search queries.

To index the movie dataset:

    $ ./bin/movies-index.sh
    
This script first creates an index and sets a mapping which ensures that all
documents are indexed both by ElasticSearch and by SIREn. You can find the source of 
the mapping in `./examples/movies/mapping.json`

To query the movie dataset:

    $ ./bin/movies-query.sh

You will see the results of the execution on standard output. In addition, the 
queries are displayed on the standard output. You can find the source of the queries in 
the folder `./datasets/movies/`.

## BNB Demo

This demo uses a set of British National Bibliography references encoded in JSON.

To index the BNB dataset:

    $ ./bin/bnb-index.sh
    
To query the BNB dataset:

    $ ./bin/bnb-query.sh
    
Same as above, the source of the mapping and of the queries can be found in
`./datasets/bnb/`.

## NCPR Demo

This demo uses the National Charge Point Registry dataset in JSON format.

To index the NCPR dataset:

    $ ./bin/ncpr-index.sh
    
To query the NCPR dataset:

    $ ./bin/ncpr-query.sh

# Notes

SIREn can be installed in Elasticsearch as any other Elasticsearch plugin. This
folder contains an instance of Elasticsearch with the SIREn plugin already 
installed and configured.

You can see the example SIREn configuration in `config/elasticsearch.yml`

The example configuration shows how to set up the default SIREn datatypes
and how to use custom analyzers declared in the ElasticSearch configuration 
file.

Note that it is possible to configure SIREn to index only a specific field
of a document (instead of indexing the whole document).

For more information please read...

 * http://siren.solutions/docs/
   For online documentation about SIREn
 * http://www.elasticsearch.org/guide/
   For a tutorial on how to use ElasticSearch

# Elasticsearch

The provided Elasticsearch instance is configured to run in a single node 
stand-alone mode (multicast discovery disabled, listening only on 
localhost). See `config/elasticsearch.yml` and look for lines with comments
starting `# for SIREn demo`.

# Elasticsearch Marvel

Elasticsearch Marvel and its developer console Sense makes it easier for interacting 
with the Elasticsearch API, managing data and prototyping queries.
To install the Elasticsearch Marvel plugin, you can run the following command from this
directory and restart ElasticSearch:

    bin/plugin -i elasticsearch/marvel/latest

Then access http://localhost:9200/_plugin/marvel/

http://www.elasticsearch.org/guide/en/marvel/current/

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.
