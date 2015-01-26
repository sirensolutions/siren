# Example Solr/SIREn Multicore Home Directory

This is an alternative setup structure to support multiple cores.

To run this configuration, start jetty in the example/ directory using:

    java -Dsolr.solr.home=multicore -jar start.jar

To load JSON documents into the multicore instance, use the generic SIREn
loader load.sh. For example, you can execute the following command to load
the NCPR dataset into core0:

    bash load.sh -f json/ncpr.json -u http://localhost:8983/solr/core0/ -i ChargeDeviceId

## Basic Directory Structure

The Solr/SIREn Home directory typically contains the following...

    solr.xml

This is the primary configuration file Solr looks for when starting.
This file specifies high level configuration options that should be used for
all SolrCores.

## Individual SolrCore Instance Directories *

solr.xml is configured to look for SolrCore Instance Directories
in any path, simple sub-directories of the Solr Home Dir using relative paths
are common for many installations.  In this directory you can see the
"./core0" and "./core1" SIREn Instance Directories.

## A Shared 'lib' Directory *

solr.xml is configured with a "sharedLib" attribute that point to the "./lib"
sub-directory of the Solr/SIREn Home Directory. This sub-directory contains
all the libraries required by SIREn to function properly. These libraries
will be shared across all the SolrCore Instance Directories. It also contains a
set of default configuration files used when creating new Solr/SIREn cores
through the Solr Admin interface:

    http://localhost:8983/solr/#/~cores

## ZooKeeper Files

When using SolrCloud using the embedded ZooKeeper option for Solr, it is
common to have a "zoo.cfg" file and "zoo_data" directories in the Solr Home
Directory.  Please see the SolrCloud wiki page for more details...

    https://wiki.apache.org/solr/SolrCloud

- - -

Copyright (c) 2014, Sindice Limited. All Rights Reserved.