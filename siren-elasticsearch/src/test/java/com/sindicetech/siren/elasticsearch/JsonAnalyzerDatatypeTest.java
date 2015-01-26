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

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Test;

import java.io.IOException;

@ElasticsearchIntegrationTest.ClusterScope(scope=ElasticsearchIntegrationTest.Scope.SUITE, numDataNodes=1)
public class JsonAnalyzerDatatypeTest extends SirenTestCase {

  @Override
  protected Settings nodeSettings(int nodeOrdinal) {
    return ImmutableSettings.settingsBuilder()
      .put("index.analysis.analyzer.lowerWhitespaceAnalyzer.type", "custom")
      .put("index.analysis.analyzer.lowerWhitespaceAnalyzer.tokenizer", "whitespace")
      .putArray("index.analysis.analyzer.lowerWhitespaceAnalyzer.filter", new String[] { "lowercase" })

      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#string.index_analyzer", "lowerWhitespaceAnalyzer")
      .put("siren.analysis.datatype.http://json.org/field.index_analyzer", "whitespace")
      .put("siren.analysis.datatype.custom.index_analyzer", "lowerWhitespaceAnalyzer")
      .put("siren.analysis.datatype.http://www.w3.org/2001/XMLSchema#long.index_analyzer", "long")

      .put(super.nodeSettings(nodeOrdinal)).build();
  }

  @Test
  public void testCoreDatatype() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .field("field", "value")
      .endObject();

    index(INDEX_NAME, TYPE_NAME, "1", builder);

    GetResponse response = get(INDEX_NAME, TYPE_NAME, "1");
    assertTrue(response.isExists());
  }

  @Test
  public void testNumericDatatype() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .field("field", 345)
      .endObject();

    index(INDEX_NAME, TYPE_NAME, "1", builder);

    GetResponse response = get(INDEX_NAME, TYPE_NAME, "1");
    assertTrue(response.isExists());
  }

  @Test
  public void testCustomDatatype() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .startObject("field")
          .field("_value_", "value")
          .field("_datatype_", "custom")
        .endObject()
      .endObject();

    index(INDEX_NAME, TYPE_NAME, "1", builder);

    GetResponse response = get(INDEX_NAME, TYPE_NAME, "1");
    assertTrue(response.isExists());
  }

}
