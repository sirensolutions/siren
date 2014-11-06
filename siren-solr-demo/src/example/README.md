# SIREn Solr Plugin Demo

This document describes two examples of using SIREn: with Lucene and with Solr.

This directory is a distribution of Solr 4.8 with SIREn pre-installed and pre-configured. 
This directory contains an instance of the Jetty Servlet container setup to run Solr using an example
configuration. We provide a Solr configuration example which use SIREn for
indexing and searching JSON data.

### Starting Solr

To run this example:

    $ java -jar start.jar

in this example directory, and when Solr is started connect to

    http://localhost:8983/solr/

### Loading the dataset

To add documents to the index, use the load-ncpr.sh script (while Solr is running):

    $ bash bin/load-ncpr.sh

This example executes the class NCPRIndexer.java which loads into Solr the
National Charge Point Registry dataset in JSON format.

If you go to the statistic page

    http://localhost:8983/solr/#/collection1

of the Solr admin interface, you should see the counter 'Num Docs' equals to
1078 (the NCPR dataset contains 1078 JSON documents).

### Querying the dataset

You can execute queries using the Solr admin interface

    http://localhost:8983/solr/#/collection1/query

The keyword query handler is configured by default. You can enter in the field
'q' a simple JSON query

    { "node" : { "query" : "Newcastle" } }

to get a list of JSON objects containing the term 'Newcastle'

    http://localhost:8983/solr/collection1/select?q={+%22node%22+%3A+{+%22query%22+%3A+%22Newcastle%22+}+}&wt=json

or more complex query

    { "twig" : 
      { 
        "root" : "DeviceOwner", 
        "child" : [{ 
          "node" : {
            "attribute" : "OrganisationName",
            "query" : "transport scotland"
          }
        }]
      }
    }

to get a list of JSON objects which are owned by the organisation 'Transport Scotland'

    http://localhost:8983/solr/collection1/select?q={%0A++%22twig%22+%3A+{%0A++++%22root%22+%3A+%22DeviceOwner%22%2C%0A++++%22child%22+%3A+[+{%0A++++++%22node%22+%3A+{%0A++++++++%22query%22+%3A+%22transport+scotland%22%2C%0A++++++++%22attribute%22+%3A+%22OrganisationName%22%0A++++++}%0A++++}+]%0A++}%0A}&wt=json

We provide also some examples on how to query programmatically the Solr/SIREn
instance in the class NCPRQuery.java. You can use the query-ncpr.sh script to execute it:

    $ bash bin/query-ncpr.sh

### Notes About These Examples

For more information please read...

 * example/solr/README.md
   For more information about the "Solr/SIREn Home" and Solr/SIREn specific configuration
 * example/multicore/README.md
   For more information about Solr/SIREn multicore specific configuration
 * http://lucene.apache.org/solr/tutorial.html
   For a Tutorial on how to use Solr
 * http://wiki.apache.org/solr/SolrResources
   For a list of other tutorials and introductory articles.

### Solr Home

By default, start.jar starts Solr in Jetty using the default Solr Home
directory of "./solr/" (relative to the working directory of the servlet
container). To run other example configurations, e.g., the multicore example
located in "./multicore/", you can specify the solr.solr.home system property
when starting jetty.

    java -Dsolr.solr.home=multicore -jar start.jar

### Logging

By default, Jetty & Solr will log to the console a logs/solr.log. This can be convenient when
first getting started, but eventually you will want to log just to a file. To
configure logging, edit the log4j.properties file in "resources".

It is also possible to setup log4j or other popular logging frameworks.

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.
