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
package com.sindicetech.siren.demo.movie;

import org.apache.commons.io.FileUtils;
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
import java.util.Iterator;

/**
 * Index a set of movies encoded in JSON and execute various search queries over
 * the JSON data structure.
 * <p>
 * Each search query is written using both the keyword query syntax and the
 * JSON query syntax.
 */
public class MovieDemo {

  private final File indexDir;

  private static final File MOVIE_PATH = new File("./datasets/movies/docs/");

  private static final Logger logger = LoggerFactory.getLogger(MovieDemo.class);

  public MovieDemo(final File indexDir) {
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
      final Iterator<File> it = FileUtils.iterateFiles(MOVIE_PATH, null, false);
      while (it.hasNext()) {
        final File file = it.next();
        final String id = file.getName().toString();
        final String content = FileUtils.readFileToString(file);
        logger.info("Indexing document {}", id);
        indexer.addDocument(id, content);
      }
      logger.info("Commiting all pending documents");
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
      b.newNode("\"Marie Antoinette\"").toString(),
      b.newNode("Drama").setAttribute("genre").toString(),
      b.newBoolean().with(b.newNode("Drama").setAttribute("genre"))
                    .with(b.newNode("2010").setAttribute("year")).toString(),
      b.newTwig("director").with(b.newNode("Eastwood").setAttribute("last_name"))
                           .with(b.newNode("Clint").setAttribute("first_name")).toString(),
      b.newBoolean().with(b.newTwig("actors").with(b.newNode("Timberlake")))
                    .with(b.newTwig("actors").with(b.newNode("Eisenberg"))).toString(),
    };
    return queries;
  }

  public static void main(final String[] args) throws IOException {
    final File indexDir = new File("./target/demo/movie/");
    final MovieDemo demo = new MovieDemo(indexDir);
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
