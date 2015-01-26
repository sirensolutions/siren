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
package com.sindicetech.siren.elasticsearch;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.hamcrest.ElasticsearchAssertions;
import org.junit.Test;

import com.sindicetech.siren.elasticsearch.analysis.ConciseJsonAnalyzerProvider;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

@ElasticsearchIntegrationTest.ClusterScope(scope=ElasticsearchIntegrationTest.Scope.SUITE, numDataNodes=1)
public class MiscQueryTest extends SirenTestCase {

  @Override
  protected Settings nodeSettings(int nodeOrdinal) {
    return ImmutableSettings.settingsBuilder()
      .put(ConciseJsonAnalyzerProvider.WILDCARD_ATTRIBUTE_SETTING, true)

      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#string.index_analyzer", "whitespace")
      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#long.index_analyzer", "whitespace")
      .put("siren.analysis.datatype.http://json.org/field.index_analyzer", "whitespace")
      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#long.index_analyzer", "long")
      .put("siren.analysis.qname.xsd", "http://www.w3.org/2001/XMLSchema#")
      .put("siren.analysis.qname.json", "http://json.org/")
      .put(super.nodeSettings(nodeOrdinal)).build();
  }

  @Test
  public void testMixedQuery() throws IOException {
    this.init();

    String input = "{\"name\":\"Radar Networks\",\"description\":\"Offers a personal recommendation engine\",\"location\":\"san francisco\",\"funding\":[{\"round\":\"a\",\"year\":2006,\"investor\":[\"ny-angels\"]},{\"round\":\"b\",\"year\":2007,\"investor\":[\"sequoia\"]},{\"round\":\"c\",\"year\":2009,\"investor\":[\"kleiner\"]}]}";
    client().prepareIndex(INDEX_NAME, TYPE_NAME, "1").setSource(input).execute().actionGet();
    flushAndRefresh();

    String query = "{\"tree\":{\"boolean\":{\"slop\":1,\"clause\":[{\"twig\":{\"root\":\"funding\",\"child\":[{\"node\":{\"attribute\":\"investor\",\"query\":\"kleiner\"}}]}},{\"twig\":{\"root\":\"funding\",\"child\":[{\"node\":{\"attribute\":\"investor\",\"query\":\"sequoia\"}}]}}]}}}";

    SearchResponse response = client().prepareSearch(INDEX_NAME).setTypes(TYPE_NAME).setQuery(query).get();
    ElasticsearchAssertions.assertNoFailures(response);
    SearchHits hits = response.getHits();
    assertThat(hits.getTotalHits(), equalTo(1l));
  }

  @Test
  public void testNumericQuery() throws IOException {
    this.init();

    String input = "{\"quarterly-revenues\":[100,200,300,400,500,600]}";
    client().prepareIndex(INDEX_NAME, TYPE_NAME, "1").setSource(input).execute().actionGet();
    input = "{\"quarterly-revenues\":[600,500,400,300,200,100]}";
    client().prepareIndex(INDEX_NAME, TYPE_NAME, "2").setSource(input).execute().actionGet();
    flushAndRefresh();

    String query = "{\"tree\":{\"node\":{\"attribute\":\"quarterly-revenues\",\"range\":[1,2],\"query\":\"xsd:long(200)\"}}}";

    SearchResponse response = client().prepareSearch(INDEX_NAME).setTypes(TYPE_NAME).setQuery(query).get();
    ElasticsearchAssertions.assertNoFailures(response);
    SearchHits hits = response.getHits();
    assertThat(hits.getTotalHits(), equalTo(1l));
  }

}
