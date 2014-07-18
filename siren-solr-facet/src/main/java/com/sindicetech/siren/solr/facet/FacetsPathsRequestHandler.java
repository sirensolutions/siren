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
package com.sindicetech.siren.solr.facet;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrIndexSearcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Request handler for facet browsers, it lists facets paths.
 * <p>
 * Add the following line to solrconfig.xml to enable it &lt;requestHandler name="/fprh"
 * class="com.sindicetech.siren.solr.fasets.FasetsPathsRequestHandler"/&gt;
 * </p>
 *
 */
public class FacetsPathsRequestHandler extends RequestHandlerBase {
  private static final Set<String> ignoredFields = new HashSet<String>(Arrays.asList(new String[] {
      "id", "json", "_version_" }));

  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    SolrIndexSearcher searcher = req.getSearcher();
    AtomicReader reader = searcher.getAtomicReader();
    Set<String> fieldNames = new TreeSet<String>();
    for (FieldInfo fieldInfo : reader.getFieldInfos()) {
      if (!ignoredFields.contains(fieldInfo.name)) {
        fieldNames.add(fieldInfo.name);
      }
    }
    rsp.add("fields", fieldNames);
    rsp.setHttpCaching(false);
  }

  @Override
  public String getDescription() {
    return "Simple retriever of path fields names from index, inspired by LukeRequestHandler ";
  }

  @Override
  public String getSource() {
    return null;
  }

}
