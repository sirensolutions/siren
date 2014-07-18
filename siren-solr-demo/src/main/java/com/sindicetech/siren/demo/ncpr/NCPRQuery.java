/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * This file is part of the SIREn project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sindicetech.siren.demo.ncpr;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindicetech.siren.qparser.tree.dsl.ConciseQueryBuilder;
import com.sindicetech.siren.qparser.tree.dsl.TwigQuery;

import java.io.IOException;

/**
 * This class executes various queries, each one showing a particular feature
 * available in SIREn.
 */
public class NCPRQuery {

  /**
   * URL of SIREn index
   */
  private final String INDEX_URL = "http://localhost:8983/solr/";

  private final SolrServer server;

  private static final Logger logger = LoggerFactory.getLogger(NCPRQuery.class);

  public NCPRQuery() {
    this.server = new HttpSolrServer(INDEX_URL);
  }

  public void execute() throws SolrServerException, QueryNodeException {
    this.query(this.getGeoQuery());
    this.query(this.getOutputCurrentQuery());
    this.query(this.getWebsiteQuery());
    this.query(this.getDeviceControllerFacet());
    this.query(this.getNestedQuery());
  }

  public void query(final SolrQuery query) throws SolrServerException {
    final QueryResponse rsp = this.server.query(query);
    logger.info("Query: {}", query.getQuery());
    logger.info("Hits: {}", rsp.getResults().getNumFound());
    logger.info("Execution time: {} ms", rsp.getQTime());
    logger.info(rsp.toString());
  }

  /**
   * A query that shows how to use a custom datatype (uri)
   */
  private SolrQuery getWebsiteQuery() throws QueryNodeException {
    ConciseQueryBuilder b = new ConciseQueryBuilder();
    String q = b.newTwig("DeviceOwner")
                .with(b.newNode("uri(www.sourcelondon.net)").setAttribute("Website"))
                .toString();
    final SolrQuery query = new SolrQuery();
    query.setQuery(q);
    return query;
  }

  /**
   * A query that shows how to use range query on geo-location data (double)
   */
  private SolrQuery getGeoQuery() throws QueryNodeException {
    ConciseQueryBuilder b = new ConciseQueryBuilder();
    String q = b.newTwig("ChargeDeviceLocation")
                .with(b.newNode("xsd:double([52 TO 53])").setAttribute("Latitude"))
                .with(b.newNode("xsd:double([-2 TO 2])").setAttribute("Longitude"))
                .toString();
    final SolrQuery query = new SolrQuery();
    query.setQuery(q);
    return query;
  }

  /**
   * A query that shows how to use range query on long numeric value
   */
  private SolrQuery getOutputCurrentQuery() throws QueryNodeException {
    ConciseQueryBuilder b = new ConciseQueryBuilder();
    TwigQuery twig1 = b.newTwig("ChargeDeviceLocation")
                       .with(b.newNode("xsd:double([52 TO 53])").setAttribute("Latitude"))
                       .with(b.newNode("xsd:double([-2 TO 2])").setAttribute("Longitude"));
    TwigQuery twig2 = b.newTwig("Connector")
                       .with(b.newNode("xsd:long([32 TO *])").setAttribute("RatedOutputCurrent"));
    String q = b.newBoolean()
                .with(twig1)
                .with(twig2)
                .toString();

    final SolrQuery query = new SolrQuery();
    query.setQuery(q);
    return query;
  }

  /**
   * A query that shows how to combine SIREn query with the Solr's facet feature
   * on a Solr field.
   */
  private SolrQuery getDeviceControllerFacet() throws QueryNodeException {
    ConciseQueryBuilder b = new ConciseQueryBuilder();
    String q = b.newTwig("Connector")
                .with(b.newNode("xsd:long([32 TO *])").setAttribute("RatedOutputCurrent"))
                .toString();
    final SolrQuery query = new SolrQuery();
    query.setQuery(q);
    query.addFacetField("DeviceController_facet");
    return query;
  }

  /**
   * A query that shows how to use the nested query parameter to use Solr's
   * query parsers on Solr's fields.
   */
  private SolrQuery getNestedQuery() throws QueryNodeException {
    ConciseQueryBuilder b = new ConciseQueryBuilder();
    String q = b.newTwig("DeviceOwner")
                .with(b.newNode("uri(www.sourcelondon.net)").setAttribute("Website"))
                .toString();
    final SolrQuery query = new SolrQuery();
    query.setQuery(q);
    query.setParam("nested", "{!lucene} name:university");
    return query;
  }

  public void close() {
    this.server.shutdown();
  }

  public static void main(final String[] args) throws IOException, SolrServerException, QueryNodeException {
    final NCPRQuery querier = new NCPRQuery();
    try {
      querier.execute();
    }
    finally {
      querier.close();
    }
  }

}
