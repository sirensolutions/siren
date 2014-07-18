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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.IndexSchema;
import org.junit.BeforeClass;

import com.sindicetech.siren.solr.UpdateProcessorTestBase;
import com.sindicetech.siren.solr.facet.FacetDatatype;
import com.sindicetech.siren.solr.facet.SirenFacetProcessorFactory;
import com.sindicetech.siren.solr.facet.TypeMapping;

/**
 * Tests for generating Facets for Siren JSON fields.
 * 
 * WARNING: be careful about implementing new tests as the schema is NOT cleaned in between 
 * individual tests.
 */
public class TestSirenFacetProcessorFactory extends UpdateProcessorTestBase {

  @BeforeClass
  private static void initManagedSchemaCore() throws Exception {
    /* Create temporary solrHome folder in the target/ folder for this test because it
     * creates the managed-schema file which we don't want in src/main/resources/...
     * and it renames the original schema-*.xml to schema-*.xml.bak - also undesirable.
     * 
     * The {@link SolrServerTestCase}, extended by UpdateProcessorTestBase, takes care of
     * keeping the schema clean between individual test runs. 
     * 
     * We don't remove the temporary solrHome folder because it will be cleaned later by 
     * mvn clean anyway (and perhaps more reliably, e.g. on Windows, etc.).
     */
    File tempSolrHome = new File("target" + File.separator + 
    TestSirenFacetProcessorFactory.class.getSimpleName() + System.currentTimeMillis());
    
    FileUtils.copyDirectory(new File(SOLR_HOME), tempSolrHome);
    // make the dir easier to find by time, otherwise it would have last modified time of 
    // the original SOLR_HOME
    tempSolrHome.setLastModified(System.currentTimeMillis());
    
    initCore("solrconfig-facets.xml", "schema-facets.xml", tempSolrHome.getAbsolutePath());
  }

  public void testStringField() throws Exception {
    String json = "{\"knows\": [{\"name\":\"josef\"}, {\"name\":\"szymon\"}]}";

    IndexSchema schema = h.getCore().getLatestSchema();

    SolrInputDocument d = processAdd("generate-facets-processor",
        doc(f("id", "1"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNotNull(schema.getFieldOrNull("string.json.knows.name"));
    assertEquals("string", schema.getFieldType("string.json.knows.name").getTypeName());

    json = "{\"knows\": [{\"name\": null}]}";

    d = processAdd("generate-facets-processor", doc(f("id", "2"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNotNull(schema.getFieldOrNull("string.json.knows.name"));
    assertEquals("string", schema.getFieldType("string.json.knows.name").getTypeName());

    json = "{\"knows\": {\"name\": true}}";

    d = processAdd("generate-facets-processor", doc(f("id", "3"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNotNull(schema.getFieldOrNull("string.json.knows.name"));
    assertEquals("string", schema.getFieldType("string.json.knows.name").getTypeName());

    json = "{\"age\": 1}";

    d = processAdd("generate-facets-processor", doc(f("id", "4"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNotNull(schema.getFieldOrNull("long.json.age"));
    assertEquals("tlong", schema.getFieldType("long.json.age").getTypeName());

    json = "{\"length\": 18.9}";

    d = processAdd("generate-facets-processor", doc(f("id", "5"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNotNull(schema.getFieldOrNull("double.json.length"));
    assertEquals("tdouble", schema.getFieldType("double.json.length").getTypeName());

    // no facets should be generated for fields with values longer than SirenFacetProcessor.MAX_FACET_VALUE_LENGTH
    String tooLongValue = "Every night and every morn Some to misery are born, " +
    "Every morn and every night Some are born to sweet delight. Some are born to sweet" +
        " delight, Some are born to endless night.";
    json = "{\"description\": \""+tooLongValue+"\"}";

    SirenFacetProcessorFactory factory = (SirenFacetProcessorFactory)h.getCore().getUpdateProcessingChain("generate-facets-processor").getFactories()[0];
    TypeMapping stringTypeMapping = factory.getTypeMappingValueClass(FacetDatatype.STRING.xsdDatatype);
    assertTrue("Bad test. Test value has to be longer than maxFieldSize for TypeMapping of string = " +
        stringTypeMapping.maxFieldSize + " but its length is only " + tooLongValue.length() + ". You should check the size of " +
        "SirenFacetProcessor.DEFAULT_MAX_FACET_VALUE_LENGTH or solrconfig*.xml for the maxFieldSize setting of the string typeMapping " +
        " of the updateRequestProcessorChain",
    tooLongValue.length() > stringTypeMapping.maxFieldSize);

    d = processAdd("generate-facets-processor", doc(f("id", "6"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNull(schema.getFieldOrNull("string.json.description"));
    assertNull(d.getFieldValue("string.json.description"));

  }

  public void testCustomDatatypeField() throws Exception {
    String json = "{\"rating\": {\"_datatype_\": \"http://www.w3.org/2001/XMLSchema#double\", \"_value_\":\"5.4\"}}";

    IndexSchema schema = h.getCore().getLatestSchema();

    SolrInputDocument d = processAdd("generate-facets-processor",
        doc(f("id", "1"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNotNull(schema.getFieldOrNull("double.json.rating"));
    assertEquals("tdouble", schema.getFieldType("double.json.rating").getTypeName());
    assertTrue((5.4 - (double)d.getFieldValue("double.json.rating")) < 0.01);

    getWrapper().add(d); // add so that we can test facets by querying
    this.commit();

    SolrQuery q = new SolrQuery();
    q.setRequestHandler("keyword");
    q.setParam("nested", "{!lucene} *:*");
    q.setFacet(true);
    q.addFacetField("double.json.rating");
    QueryResponse r = getWrapper().getServer().query(q);
    // we know there is only one facet field with one value
    assertEquals(1, r.getFacetFields().get(0).getValues().get(0).getCount());

    json = "{\"rating\": {\"_datatype_\": \"http://www.w3.org/2001/XMLSchema#float\", \"_value_\":\"-8.4\"}}";
    schema = h.getCore().getLatestSchema();
    d = processAdd("generate-facets-processor",
        doc(f("id", "2"), f("json", json)));
    assertNotNull(d);
    schema = h.getCore().getLatestSchema();
    assertNotNull(schema.getFieldOrNull("double.json.rating"));
    assertEquals("tdouble", schema.getFieldType("double.json.rating").getTypeName());
    assertTrue((-8.4 + (double)d.getFieldValue("double.json.rating")) < 0.01);
    
    getWrapper().add(d); // add so that we can test facets by querying
    this.commit();
    
    r = getWrapper().getServer().query(q);

    // there is only one facet field with two different values each with a single count
    assertEquals(1, r.getFacetFields().get(0).getValues().get(0).getCount());
    assertEquals(1, r.getFacetFields().get(0).getValues().get(1).getCount());
  }
}
