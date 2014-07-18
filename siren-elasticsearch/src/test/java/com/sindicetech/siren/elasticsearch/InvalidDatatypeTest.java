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

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.engine.IndexFailedEngineException;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Test;

import java.io.IOException;

@ElasticsearchIntegrationTest.ClusterScope(scope=ElasticsearchIntegrationTest.Scope.SUITE, numDataNodes=1)
public class InvalidDatatypeTest extends SirenTestCase {

  @Test(expected = IndexFailedEngineException.class)
  public void testCoreDatatype() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
      .startObject()
        .field("field", "value")
      .endObject();

    // must fail since no json:field and xmlstring datatype defined
    index(INDEX_NAME, TYPE_NAME, "1", builder);
  }

  @Test(expected = IndexFailedEngineException.class)
  public void testCustomDatatype() throws IOException {
    this.init();

    XContentBuilder builder = XContentFactory.jsonBuilder()
    .startObject()
      .startObject("field")
        .field("_value_", "value")
        .field("_datatype_", "int")
      .endObject()
    .endObject();

    // must fail since no int datatype defined
    index(INDEX_NAME, TYPE_NAME, "1", builder);
  }

}
