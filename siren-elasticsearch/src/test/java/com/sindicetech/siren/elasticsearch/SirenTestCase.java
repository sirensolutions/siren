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

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.test.ElasticsearchIntegrationTest;

import java.io.IOException;

public abstract class SirenTestCase extends ElasticsearchIntegrationTest {

  protected static final String INDEX_NAME = "test";
  protected static final String TYPE_NAME = "json";

  @Override
  protected Settings nodeSettings(int nodeOrdinal) {
    return ImmutableSettings.settingsBuilder()
      .put("path.data", "./target/elasticsearch-test/data/")
      .put(super.nodeSettings(nodeOrdinal)).build();
  }

  /**
   * Register the siren field
   */
  protected void init() throws IOException {
    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .startObject("json")
          .startObject("properties")
            .startObject("_siren_source")
              .field("analyzer", "concise")
              .field("postings_format", "Siren10AFor")
              .field("store", "no")
              .field("type", "string")
            .endObject()
          .endObject()
          .startObject("_siren")
          .endObject()
        .endObject()
      .endObject();

    prepareCreate(INDEX_NAME).addMapping("json", builder).get();
    ensureGreen();
  }

}
