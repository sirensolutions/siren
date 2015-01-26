# Build Instructions for the SIREn Elasticsearch Plugin 

## Introduction

We assume that you have installed Elasticsearch in the directory `${ES_HOME}`.

## Building the siren-elasticsearch Module

From the command line, change (cd) into the directory of the 
siren-elasticsearch module of your SIREn installation. Then, typing

    $ mvn package
    
in your shell prompt should execute the Maven assembly task. The Maven assembly task will create 
the archive of the plugin at "./target/releases/siren-elasticsearch-1.3-plugin.zip".

## Installing the Plugin

Go to the Elasticsearch folder:
 
    $ cd ${ES_HOME}

Make sure that ElasticSearch is NOT running.

Install the plugin (change the `${SIREN_HOME}` path accordingly):

    $ bin/plugin --url file:///${SIREN_HOME}/siren-elasticsearch/target/releases/siren-elasticsearch-1.3-plugin.zip --install siren-plugin

Run ElasticSearch:

    $ bin/elasticsearch

If the SIREn plugin is correctly installed, you should see in the Elasticsearch log 
a line saying `loaded [siren-plugin]`:

```
[2014-05-09 14:40:04,008][INFO ][plugins ] [Basilisk] loaded [siren-plugin], sites []
```

## Updating the Plugin

To update the plugin, assuming you are in the Elasticsearch installation directory: 

 - stop ElasticSearch
 - remove the plugin
 
    $ `bin/plugin --remove siren-plugin`

 - add it again
 - start ElasticSearch

## Configuring Analyzers for Datatypes 

It is necessary to configure analyzers for at least the five core datatypes in the Elasticsearch configuration file 
which is located at `${ES_HOME}/config/elasticsearch.yml`. A default setting should look like:

```
siren:
  analysis:
    datatype:
      http://www.w3.org/2001/XMLSchema#string:
        index_analyzer: standard
      http://json.org/field:
        index_analyzer: whitespace
      http://www.w3.org/2001/XMLSchema#double:
          index_analyzer: double
      http://www.w3.org/2001/XMLSchema#long:
          index_analyzer: long
      http://www.w3.org/2001/XMLSchema#boolean:
          index_analyzer: whitespace
```

# Testing the Plugin

Assuming that your Elasticsearch installation is clean, i.e., that there is no index called 'siren'.

## Creating an Index

Create an index called "test-siren" by typing in your shell prompt:

    $ curl -XPOST "localhost:9200/test-siren/?pretty"

## Configuring of the Mapping 

The following creates a mapping that enables SIREn indexing and querying for the "json"
document type in an index. Note that the mapping has to be specified before you
start indexing documents. Otherwise, the existing documents would have to be reindexed.

```
    $ curl -XPUT "http://localhost:9200/test-siren/json/_mapping?pretty" -d '
    {
      "json" : {
        "properties" : {
          "_siren_source":{
            "index" : "analyzed",
            "analyzer" : "concise",
            "postings_format" : "Siren10AFor",
            "store" : "no",
            "type" : "string"
          }
        },
        "_siren" : { }
      }
    }'
```

You can ask for the mapping of "test-siren/json" to verify it

    $ curl "http://localhost:9200/test-siren/json/_mapping?pretty"

## Testing the Analyzer Without Indexing a Document

You can test the SIREn "concise" analyzer using the command:

    $ curl -XGET 'localhost:9200/test-siren/_analyze?analyzer=concise&pretty' -d '{ "key": "value", "age" : 35 }'`

The response should be something like:

```
{
  "tokens" : [ {
    "token" : "key:value",
    "start_offset" : 0,
    "end_offset" : 5,
    "type" : "<ALPHANUM>",
    "position" : 1
  }, {
    "token" : "age:LONG8 \u0001\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000#",
    "start_offset" : 0,
    "end_offset" : 0,
    "type" : "fullPrecNumeric",
    "position" : 2
  },
  ...
}
```

## Indexing a Document

The following example inserts a JSON document into the "test-siren" index under the type "json"
with an id equal to "1":

```
    $ curl -XPUT 'http://localhost:9200/test-siren/json/1?pretty' -d '
    {
      "name" : "Elasticsearch", 
      "category_code" : "search",
      "funding_rounds" : [
        {
          "round_code" : "a",
          "raised_amount" : 10000000,
          "funded_year" : 2012,
          "investments" : [
            {
              "name" : "Data Collective",
              "type" : "financial-org"
            }
          ]
        }
      ]
    }'
```

## Searching a Document

The following example search for all JSON documents with an object having an attribute "name" matching the 
keyword "Elasticsearch":

```
    $ curl -XGET 'http://localhost:9200/test-siren/json/_search?pretty' -d '
    {
      "query" : {
        "concise" : {
          "node" : {
            "attribute" : "name",
            "query" : "Elasticsearch" 
          }
        }
      }
    }'
```

It is also possible to combine freely Elasticsearch queries with SIREn queries:

```
    $ curl -XGET 'http://localhost:9200/test-siren/json/_search?pretty' -d '
    {
      "query" : {
        "bool" : {
          "must" : {
            "concise" : {
              "node" : {
                "query" : "search"
              }
            }
          },
          "must" : {
            "term" : { "funded_year" : "2012" }
          }
        }
      },
      "sort" : [
        {
          "name" : {
            "order" : "desc"
          }
        } 
      ]
    }'

# Tips

 - List all indexes: `curl 'localhost:9200/_cat/indices?v'` - also reports about index health - very useful, I've seen red states after unclean shutdowns.
 - Delete index: `curl -XDELETE 'http://localhost:9200/testsiren/'`
 - Search all indices: `curl 'http://localhost:9200/_search?q=*:*&pretty'`
 - Search index called siren: `curl 'http://localhost:9200/siren/_search?q=*:*&pretty'`
 - Check mappings: `curl 'http://localhost:9200/siren/_mapping?pretty'`
 
# References

 - Indices API: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices.html
 - Search API: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search.html

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.

