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
package com.sindicetech.siren.demo.bnb;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindicetech.siren.demo.SimpleIndexer;
import com.sindicetech.siren.demo.SimpleSearcher;
import com.sindicetech.siren.qparser.tree.dsl.ConciseQueryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Index a set of bibliographical references encoded in JSON and execute various
 * search queries over the JSON data structure.
 * <p>
 * Each search query is written using both the keyword query syntax and the
 * JSON query syntax.
 */
public class BNBDemo {

  private final File indexDir;

  private static final File BNB_PATH = new File("./datasets/bnb/data.json");

  private static final Logger logger = LoggerFactory.getLogger(BNBDemo.class);

  public BNBDemo(final File indexDir) {
    this.indexDir = indexDir;
    if (indexDir.exists()) {
      logger.error("Existing directory {} - aborting", indexDir);
      System.exit(1);
    }
    logger.info("Creating index directory {}", indexDir);
    indexDir.mkdirs();
  }

  public void index() throws IOException {
    final SimpleIndexer indexer = new SimpleIndexer(indexDir);
    try {
      int counter = 0;
      final LineIterator it = FileUtils.lineIterator(BNB_PATH);
      while (it.hasNext()) {
        final String id = Integer.toString(counter++);
        final String content = (String) it.next();
        logger.info("Indexing document {}", id);
        indexer.addDocument(id, content);
      }
      LineIterator.closeQuietly(it);
      logger.info("Committing all pending documents");
      indexer.commit();
    }
    finally {
      logger.info("Closing index");
      indexer.close();
    }
  }

  public void search() throws QueryNodeException, IOException {
    final SimpleSearcher searcher = new SimpleSearcher(indexDir);
    final String[] jsonQueries = this.getJsonQueries();

    for (int i = 0; i < jsonQueries.length; i++) {
      Query q = searcher.parseJsonQuery(jsonQueries[i]);
      logger.info("Executing json query: '{}'", jsonQueries[i]);
      String[] results = searcher.search(q, 1000);
      logger.info("Json query returned {} results: {}", results.length, Arrays.toString(results));
    }

  }

  /**
   * Get a list of queries that are based on the JSON query syntax
   *
   * @see com.sindicetech.siren.qparser.tree.ExtendedTreeQueryParser
   */
  private String[] getJsonQueries() throws QueryNodeException {
    final ConciseQueryBuilder b = new ConciseQueryBuilder();
    final String[] queries = {
      b.newNode("Cambridge").toString(),
      b.newNode("Cambridge").setAttribute("placeOfPublication").toString(),
      b.newNode("Cambridge Scholars").setAttribute("publisher").toString(),
      b.newNode("Environmental").setAttribute("subject").toString(),
      b.newBoolean().with(b.newNode("Environmental").setAttribute("subject"))
                    .with(b.newNode("2009").setAttribute("issued")).toString(),
      // search an attribute with two different values
      b.newBoolean().with(b.newNode("text").setAttribute("type"))
                    .with(b.newNode("monographic").setAttribute("type")).toString(),
      b.newTwig("identifier").with(b.newNode("9780852935392").setAttribute("id"))
                             .with(b.newNode("isbn").setAttribute("type")).toString(),
      b.newBoolean().with(b.newNode("Computer security").setAttribute("subject"))
                    .with(b.newTwig("isPartOf").with(
                            b.newTwig("identifier").with(
                            b.newNode("'0302-9743'").setAttribute("id")
                          )
                        )).toString()
    };
    return queries;
  }

  public static void main(final String[] args) throws IOException {
    final File indexDir = new File("./target/demo/bnb/");
    final BNBDemo demo = new BNBDemo(indexDir);
    try {
      demo.index();
      demo.search();
    }
    catch (final Throwable e) {
      logger.error("Unexpected error during demo", e);
    }
    finally {
      logger.info("Deleting index directory {}", indexDir);
      FileUtils.deleteQuietly(indexDir);
    }
  }

}
