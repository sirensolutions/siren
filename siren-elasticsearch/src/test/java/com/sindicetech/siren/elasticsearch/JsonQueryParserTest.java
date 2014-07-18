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
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.hamcrest.ElasticsearchAssertions;
import org.junit.Test;

import com.sindicetech.siren.elasticsearch.analysis.ConciseJsonAnalyzerProvider;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;

@ElasticsearchIntegrationTest.ClusterScope(scope=ElasticsearchIntegrationTest.Scope.SUITE, numDataNodes=1)
public class JsonQueryParserTest extends SirenTestCase {

  @Override
  protected Settings nodeSettings(int nodeOrdinal) {
    return ImmutableSettings.settingsBuilder()
      .put(ConciseJsonAnalyzerProvider.WILDCARD_ATTRIBUTE_SETTING, true)

      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#string.index_analyzer", "whitespace")
      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#string.search_analyzer", "standard")
      .put("siren.analysis.datatype.http://json.org/field.index_analyzer", "whitespace")
      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#long.index_analyzer", "long")
      .put("siren.analysis.qname.xsd", "http://www.w3.org/2001/XMLSchema#")
      .put("siren.analysis.qname.json", "http://json.org/")
      .put(super.nodeSettings(nodeOrdinal)).build();
  }

  @Test
  public void testConciseTreeQueryParser() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .field("field", "value")
      .endObject();

    index(INDEX_NAME, TYPE_NAME, "1", builder);
    flushAndRefresh();

    builder = XContentFactory.jsonBuilder()
      .startObject()
        .startObject("tree")
          .startObject("node")
            .field("query", "Value") // must be lowercase if the query-time analyzer is correctly configured
          .endObject()
        .endObject()
      .endObject();

    SearchResponse response = client().prepareSearch(INDEX_NAME).setTypes(TYPE_NAME).setQuery(builder).get();
    ElasticsearchAssertions.assertNoFailures(response);
    SearchHits hits = response.getHits();
    assertThat(hits.getTotalHits(), equalTo(1l));

    builder = XContentFactory.jsonBuilder()
      .startObject()
        .startObject("tree")
          .startObject("node")
            .field("query", "xsd:string('Value')") // qnames must be correctly picked up so that querying doesn't fail
          .endObject()
        .endObject()
      .endObject();

    response = client().prepareSearch(INDEX_NAME).setTypes(TYPE_NAME).setQuery(builder).get();
    ElasticsearchAssertions.assertNoFailures(response);
  }

  @Test
  public void testNumericDatatype() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .field("field", 42)
      .endObject();

    index(INDEX_NAME, TYPE_NAME, "1", builder);
    flushAndRefresh();

    builder = XContentFactory.jsonBuilder()
      .startObject()
        .startObject("tree")
          .startObject("node")
            .field("query", "http://www.w3.org/2001/XMLSchema#long(42)")
          .endObject()
        .endObject()
      .endObject();

    SearchResponse response = client().prepareSearch(INDEX_NAME).setTypes(TYPE_NAME).setQuery(builder).get();
    ElasticsearchAssertions.assertNoFailures(response);
    SearchHits hits = response.getHits();
    assertThat(hits.getTotalHits(), equalTo(1l));
  }

  @Test
  public void testConciseTreeQueryParserQnames() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .field("field", "value")
      .endObject();

    index(INDEX_NAME, TYPE_NAME, "1", builder);
    flushAndRefresh();

    builder = XContentFactory.jsonBuilder()
      .startObject()
        .startObject("tree")
          .startObject("node")
            .field("query", "xsd:string('Value')") // qnames must be correctly picked up so that querying doesn't fail
          .endObject()
        .endObject()
      .endObject();

    SearchResponse response = client().prepareSearch(INDEX_NAME).setTypes(TYPE_NAME).setQuery(builder).get();
    ElasticsearchAssertions.assertNoFailures(response);
    SearchHits hits = response.getHits();
    assertThat(hits.getTotalHits(), equalTo(1l));
  }

  @Test
  public void testMixedQuery() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .field("category", "drama")
        .field("year", "2010")
        .field("message", "test")
      .endObject();

    index(INDEX_NAME, TYPE_NAME, "1", builder);
    flushAndRefresh();

    String query = "{\"bool\":{\"must\":{\"tree\":{\"boolean\":{\"clause\":[{\"occur\":\"MUST\",\"node\":{\"query\":\"Drama\"}}]}}},\"must\":{\"match\":{\"message\":\"test\"}}}}";

    SearchResponse response = client().prepareSearch(INDEX_NAME).setTypes(TYPE_NAME).setQuery(query).get();
    ElasticsearchAssertions.assertNoFailures(response);
    SearchHits hits = response.getHits();
    assertThat(hits.getTotalHits(), equalTo(1l));
  }

}
