/**
 * SIREn is a high-performance, full-featured semi-structured text search engine
 * library. This module presents the SIREn Elasticsearch Plugin.
 *
 * <h2>Introduction</h2>
 * The SIREn Elasticsearch plugin provides all SIREn functionality to Elasticsearch
 * users. Moreover, it is also possible to combine Elasticsearch and SIREn queries.
 * The SIREn plugin is enabled by configuring a mapping for a newly created index.
 *
 *  <p>Documents are indexed independently both by SIREn and Elasticsearch so the standard
 *  Elasticsearch functionality is not modified. </p>
 *
 * <p>
 * {@link com.sindicetech.siren.elasticsearch.plugin.SirenPlugin} registers
 * all the individual modules that make up the SIREn Elasticsearch plugin:
 *
 * <ul>
 * <li> {@link com.sindicetech.siren.elasticsearch.query.TreeParserModule}
 * <li>{@link com.sindicetech.siren.elasticsearch.index.SirenFieldModule}
 * <li>{@link com.sindicetech.siren.elasticsearch.analysis.RegisterDatatypeModule}
 * <li>{@link com.sindicetech.siren.elasticsearch.analysis.AnalyzerModule}
 * </ul>
 * </p>
 *
 *  <p>Please refer to the siren-elasticsearch-demo module for complete examples that demonstrate
 *  creating an index, configuring a mapping, indexing documents, and running queries in Elasticsearch.
 *  </p>
 *
 *  <h2>Mapping configuration</h2>
 * The following mapping enables SIREn indexing and querying for the <em>myDocType</em>
 * document type in an index. Note that the mapping has to be specified before you
 * start indexing documents. Otherwise, the existing documents would have to be reindexed.
 *
 *  <p>The following mapping sets the type, analyzer, and posting format settings for the
 *  special <b>_siren_source</b> field:
 *  <pre>
 *  {@code
 *  {
 *    "myDocType" : {
 *      "properties" : {
 *        "_siren_source":{
 *          "index" : "analyzed",
 *          "analyzer" : "concise",
 *          "postings_format" : "Siren10AFor",
 *          "store" : "no",
 *          "type" : "string"
 *        }
 *      },
 *      "_siren" : { }
 *    }
 *  }
 *  </pre>
 *
 *  <p>Analyzer can be set to "concise" or "extended" based on which kind of model you want
 *  to use. Note that the preferred SIREn model is "concise".
 *  </p>
 *
 *  <h3>How it works</h3>
 *  The root field <b>_siren</b> makes the SIREn plugin copy the _source field to the
 *  <b>_siren_source</b> field during posting of documents to Elasticsearch.
 *  See {@link SirenFieldMapper} for details.
 *
 *  <h2>Search</h2>
 *
 *  <h3>Query Parsers</h3>
 *
 *  <p>
 *  Two query parsers are pre-registered by the SIREn plugin. To perform a search, the "concise"
 *  (resp. "extended" for the "extended" model) type has to be used. The value of the "concise" field is
 *  a SIREn query object. The following example shows two valid queries using the "concise" query
 *  parser.
 *  </p>
 *  <pre>
 *  {@code
 *  {
 *    "query" : {
 *      "concise" : {
 *        "node" : {
 *          "query" : "siren"
 *        }
 *      }
 *    }
 *  }
 *
 *  {
 *    "query" : {
 *      "concise" :{
 *        "twig" : {
 *          "root" : "author",
 *          "child" : [{
 *            "occur" : "MUST",
 *            "node" : {
 *              "attribute" : "name"
 *              "query" : "Minsky"
 *            }
 *          }]
 *        }
 *      }
 *    }
 *  }
 *  }
 *  </pre>
 *
 *  <h3>Combined Queries</h3>
 *  The following example shows a combination of SIREn and Elasticsearch queries:
 *  <pre>
 *  {@code
 *  {
 *   "query" : {
 *     "bool" : {
 *       "must" : {
 *         "concise" : {
 *           "node" : {
 *             "query": "siren"
 *           }
 *         }
 *       },
 *       "must" : {
 *         "term" : { "name" : "search" }
 *       }
 *     }
 *   },
 *   "sort" : [
 *     {
 *       "name" : {
 *         "order" : "desc"
 *       }
 *     }
 *   ]
 * }
 * }</pre>
 *
 *  <h2>Analysis</h2>
 *
 *  <h3>Concise JSON Analysis</h3>
 *
 *  <p>The "concise" analyzer can be configured to enable or disable attribute wildcard queries using the
 *  settings 'siren.analysis.concise.attribute_wildcard.enabled'. If the attribute wildcard setting is
 *  disabled, a node query will always expect an 'attribute' property. For example, the first query will
 *  be valid while the second one is only valid if the attribute wildcard has been enabled.</p>
 * <pre>
 *  {@code
 *  {
 *    "query" : {
 *      "concise" : {
 *        "node" : {
 *          "attribute" : "title",
 *          "query" : "siren"
 *        }
 *      }
 *    }
 *  }
 *
 *  {
 *    "query" : {
 *      "concise" : {
 *        "node" : {
 *          "query" : "siren"
 *        }
 *      }
 *    }
 *  }
 *  </pre>
 *
 *  <h3>Datatype Analysis</h3>
 *
 *  <p>The SIREn's analyzers, i.e., "concise" and "extended", rely on datatype analyzers to analyze the
 *  content of a JSON document or the keyword of a query. Please refer to the
 *  <a href="{@docRoot}/com/sindicetech/siren/analysis/package-summary.html#package_description"> for more information
 *  about datatypes. Datatype analyzers are configured in the elasticsearch.yml file as follows:</p>
 *
 * <pre>
 *  {@code
 *  siren:
 *    analysis:
 *      datatype:
 *        http://www.w3.org/2001/XMLSchema#string:
 *          index_analyzer: standard
 *          search_analyzer: lowerWhitespace
 *
 *        http://www.w3.org/2001/XMLSchema#long:
 *          index_analyzer: long
 *
 *  index:
 *    analysis:
 *      analyzer:
 *        lowerWhitespace:
 *          type: custom
 *          tokenizer: whitespace
 *          filter: [lowercase]
 *  </pre>
 *
 *  <p>In the first group of the settings, prefixed by siren.analysis.datatype, we configure the mapping between the
 *  datatype name and the analyzer name. For example, the datatype "http://www.w3.org/2001/XMLSchema#string" will be
 *  configured with the "standard" analyzer at indexing time, and the "lowerWhitespace" analyzer at search time.
 *  While the "standard" analyzer is an analyzer that is pre-registered by Elasticsearch, the "lowerWhitespace" analyzer
 *  is a custom one that is defined in the same settings.</p>
 *
 *  <p>With respect to numeric datatypes, e.g., "http://www.w3.org/2001/XMLSchema#long" or
 *  "http://www.w3.org/2001/XMLSchema#double", the SIREn plugin provides two pre-registered analyzers, named
 *  respectively "long" and "double". These numeric analyzers use specific tokenisation strategies in order to
 *  support numeric range queries.</p>
 *
 *  <h3>QNames</h3>
 *
 *  <p>The query parsers can be configured with a set of qname mappings. These qname mappings are useful to
 *  write more concise queries when using Uniform Resource Identifiers. For example, the core JSON datatypes
 *  are identified using URI, e.g., http://json.org/field or http://www.w3.org/2001/XMLSchema#long. A qname
 *  mapping "xsd" : "http://www.w3.org/2001/XMLSchema#" will allow you to write "xsd:long" instead of the full URI.</p>
 *
 *  <p>The qname mappings are configured in the elasticsearch.yml file using the "siren.analysis.qname" setting.
 *  The following example shows how to register two qnames:
 * <pre>
 *  {@code
 *  siren:
 *    analysis:
 *      qname:
 *        xsd : http://www.w3.org/2001/XMLSchema#
 *        json: http://json.org/
 *  </pre>
 *  </p>
 *
 * <h2>Fieldable mapping configuration</h2>
 * It is theoretically also possible to configure SIREn to index and query a specific field in a document.
 *
 * See {@link com.sindicetech.siren.elasticsearch.query.FieldableTreeQueryParser } for an example implementation.
 *
 */
package com.sindicetech.siren.elasticsearch.plugin;
