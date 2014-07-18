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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sindicetech.siren.solr.facet.FacetDatatype;
import com.sindicetech.siren.solr.facet.SirenFacetEntry;
import com.sindicetech.siren.solr.facet.SirenFieldFacetExtractor;

public class TestSirenFieldFacetExtractor {
  static ObjectMapper mapper;
  static SirenFieldFacetExtractor extractor;

  @BeforeClass
  public static void beforeClass() throws Exception {
    mapper = new ObjectMapper();
    extractor = new SirenFieldFacetExtractor(null);
  }
  
  private void printToFieldName(List<SirenFacetEntry> list) {
    System.out.print("[");
    Iterator<SirenFacetEntry> it = list.iterator();
    
    while (it.hasNext()) {
      SirenFacetEntry entry = it.next();
      System.out.print(entry.toFieldName());
      if (it.hasNext()) {
        System.out.print(", ");
      }
    }
    System.out.println("]");
  }
  
  @Test
  public void testPathGenerating() throws JsonProcessingException, IOException {
    JsonNode node = mapper.readTree("{\"knows\": [{\"name\":\"josef\"}, {\"name\":\"szymon\"}]}");
    
    List<SirenFacetEntry> list = new ArrayList<SirenFacetEntry>();
    extractor.generateFacetsForLeaves(node, "FIELDNAME", null, "", list);
    //printToFieldName(list);
    
    assertEquals(2, list.size());

    
    node = mapper.readTree("{\"knows\": [{\"name\": null}]}");
    list.clear();
    
    extractor.generateFacetsForLeaves(node, "FIELDNAME", null, "", list);
    assertEquals(1, list.size());
    assertEquals(FacetDatatype.NULL, list.get(0).datatype);
    assertEquals("knows.name", list.get(0).path);
    assertEquals("null", list.get(0).value);
    //printToFieldName(list);
    
    
    node = mapper.readTree("{\"knows\": {\"name\": true}}");
    list.clear();
    
    extractor.generateFacetsForLeaves(node, "FIELDNAME", null, "", list);
    assertEquals(1, list.size());
    assertEquals(FacetDatatype.BOOLEAN, list.get(0).datatype);
    assertEquals("knows.name", list.get(0).path);
    assertEquals(true, list.get(0).value);
    assertEquals("boolean.FIELDNAME.knows.name", list.get(0).toFieldName());
    //printToFieldName(list);
    

    node = mapper.readTree("{\"knows\": {\"age\": 32}}");
    list.clear();
    
    extractor.generateFacetsForLeaves(node, "FIELDNAME", null, "", list);
    assertEquals(1, list.size());
    assertEquals(FacetDatatype.LONG, list.get(0).datatype);
    assertEquals("knows.age", list.get(0).path);
    assertEquals(32, list.get(0).value);
    assertEquals("long.FIELDNAME.knows.age", list.get(0).toFieldName());
    
    
    //printToFieldName(list);
  }

}

