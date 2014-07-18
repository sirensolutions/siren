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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.NullArgumentException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ValueNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindicetech.siren.analysis.NumericAnalyzer;
import com.sindicetech.siren.analysis.NumericAnalyzer.NumericParser;
import com.sindicetech.siren.solr.schema.Datatype;
import com.sindicetech.siren.solr.schema.ExtendedTreeField;
import com.sindicetech.siren.solr.schema.TrieDatatype;

/**
 * <p>Given a json document ({@link SolrInputDocument}), generates a {@link SirenFacetEntry}
 * for each of its leaves.
 * 
 * @experimental Can change in the next release.
 */
public class SirenFieldFacetExtractor implements FacetExtractor {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private IndexSchema schema;
  private static final ObjectMapper mapper = new ObjectMapper(); // ObjectMapper is thread-safe

  /**
   * Constructs SirenFieldFacetExtractor without setting the IndexSchema.
   * 
   * {@link #setSchema(IndexSchema)} MUST be called before calling
   * {@link #extractFacets(SolrInputDocument)}
   * 
   */
  public SirenFieldFacetExtractor() {
  }

  /**
   * Constructs SirenFieldFacetExtractor and sets IndexSchema.
   */
  public SirenFieldFacetExtractor(IndexSchema schema) {
    this.schema = schema;
  }

  public void setSchema(IndexSchema schema) {
    this.schema = schema;
  }

  public IndexSchema getSchema() {
    return schema;
  }

  /**
   * The main entry point of this class. Generates a list of {@link SirenFacetEntry} for 
   * the given {@link SolrInputDocument} by performing a DFS through the doc. 
   * 
   * @param doc The document for which to generate facet entries
   * @throws IllegalStateException if IndexSchema was not set (either in constructor or via {@link #setSchema(IndexSchema)}
   */
  @Override
  public List<SirenFacetEntry> extractFacets(SolrInputDocument doc) throws FacetException {
    if (schema == null) {
      throw new IllegalStateException(
          "Schema field is null - probably the default constructor was used without calling setSchema() later.");
    }

    List<SirenFacetEntry> facets = new ArrayList<SirenFacetEntry>();

    for (String fieldName : doc.getFieldNames()) {
      FieldType ft = schema.getFieldOrNull(fieldName).getType();
      if (ft instanceof ExtendedTreeField) {
        String sirenField = (String) doc.getFieldValue(fieldName);
        try {
          JsonNode sirenNode = mapper.readTree(sirenField);

          generateFacetsForLeaves(sirenNode, fieldName, (ExtendedTreeField) ft, "", facets);
          
        } catch (JsonProcessingException e) {
          throw new FacetException("Could not parse siren field " + fieldName + ": " + e.getMessage(), e);
        } catch (IOException e) {
          throw new FacetException("I/O problem while parsing siren field " + fieldName + ": " + e.getMessage(), e);
        }
      }
    }

    return facets;
  }

  /**
   * The entry point of the generateFacetsForLeaves() methods.
   * 
   * DFS through the sirenNode JsonNode. Generates a {@link SirenFacetEntry} for each
   * leaf.
   * 
   * @param sirenNode The Json to walk through.
   * @param fieldName The name of the ExtendedTreeField of the original SolrDocument the value of which is sirenNode.
   * @param path The path currently visited by the DFS algorithms. Should be an empty String "" in the initial call.
   * @param facets The entries generated for the leaves. Should be an not null list.
   * 
   * @throws NullArgumentException if path or facets are null.
   */
  protected void generateFacetsForLeaves(JsonNode sirenNode, String fieldName, ExtendedTreeField field, String path,
      List<SirenFacetEntry> facets) {
    if (facets == null) {
      throw new NullArgumentException("Parameter facets must not be null");
    }
    if (path == null) {
      throw new NullArgumentException("Parameter path must not be null");
    }

    if (sirenNode.isValueNode()) {
      generateFacetsForLeaves((ValueNode) sirenNode, fieldName, field, path, facets);
    }

    if (sirenNode.isArray()) {
      generateFacetsForLeaves((ArrayNode) sirenNode, fieldName, field, path, facets);
    }

    if (sirenNode.isObject()) {
      generateFacetsForLeaves((ObjectNode) sirenNode, fieldName, field, path, facets);
    }
  }

  /**
   * The generateFacetsForLeaves() method for processing ValueNode, that is leaves.
   * Ends recursion and generates a new {@link SirenFacetEntry}.
   */
  private void generateFacetsForLeaves(ValueNode value, String fieldName, ExtendedTreeField field, String path,
      List<SirenFacetEntry> facets) {
    SirenFacetEntry entry = new SirenFacetEntry();
    entry.fieldName = fieldName;
    entry.path = path;

    if (value.isNull()) {
      entry.value = value.asText();
      entry.datatype = FacetDatatype.NULL;
    } else if (value.isInt()) {
      entry.value = value.asInt();
      entry.datatype = FacetDatatype.LONG;
    } else if (value.isLong()) {
      entry.value = value.asLong();
      entry.datatype = FacetDatatype.LONG;
    } else if (value.isDouble()) {
      entry.value = value.asDouble();
      entry.datatype = FacetDatatype.DOUBLE;
    } else if (value.isBoolean()) {
      entry.value = value.asBoolean();
      entry.datatype = FacetDatatype.BOOLEAN;
    } else {
      entry.value = value.asText();
      entry.datatype = FacetDatatype.STRING;
    }

    facets.add(entry);
  }

  /**
   * The generateFacetsForLeaves() method for processing json arrays, simply delegates for each array element.
   */
  private void generateFacetsForLeaves(ArrayNode array, String fieldName, ExtendedTreeField field, String path,
      List<SirenFacetEntry> facets) {
    Iterator<JsonNode> iterator = array.getElements();

    while (iterator.hasNext()) {
      JsonNode node = iterator.next();

      generateFacetsForLeaves(node, fieldName, field, path, facets);
    }
  }

  /**
   * The generateFacetsForLeaves() method for processing json objects, delegates for each field and constructs path.
   */
  private void generateFacetsForLeaves(ObjectNode object, String fieldName, ExtendedTreeField sirenField, String path,
      List<SirenFacetEntry> facets) {
    Iterator<Entry<String, JsonNode>> iterator = object.getFields();

    while (iterator.hasNext()) {
      Entry<String, JsonNode> entry = iterator.next();
      String field = entry.getKey();
      JsonNode value = entry.getValue();
      
      if (field.equals("_datatype_") || field.equals("_value_")) {
        generateFacetsForCustomDatatypeLeaf(object, fieldName, sirenField, path, facets);
        return;
      }

      generateFacetsForLeaves(value, fieldName, sirenField, path.isEmpty() ? field : path + "." + field, facets);
    }
  }

  private void generateFacetsForCustomDatatypeLeaf(ObjectNode object, String fieldName,
      ExtendedTreeField sirenField, String path, List<SirenFacetEntry> facets) {
    Iterator<Entry<String, JsonNode>> iterator = object.getFields();

    String datatype = null;
    String value = null;
    
    while (iterator.hasNext()) {
      Entry<String, JsonNode> entry = iterator.next();
      if ("_datatype_".equals(entry.getKey())) {
        datatype = entry.getValue().asText();
      } else if ("_value_".equals(entry.getKey())) {
        value = entry.getValue().asText();
      } else {
        logger.warn("Unexpected field {} in custom datatype object: {}", entry.getKey(), object.asText());
        continue;
      }      
    }
    
    if (datatype == null || value == null) {
      logger.warn("Unexpected form of custom datatype object: {}. Not generating facets for this nested object.", object.asText()); 
      return;
    }

    SirenFacetEntry entry = new SirenFacetEntry();
    entry.fieldName = fieldName;
    entry.path = path;

    Datatype customDatatype = sirenField.getDatatypes().get(datatype);
    if (customDatatype instanceof TrieDatatype) {
      NumericAnalyzer analyzer = (NumericAnalyzer) customDatatype.getAnalyzer();
      NumericParser parser = analyzer.getNumericParser();
      try {
        Number number = parser.parse(new StringReader(value));
        if ((number instanceof Float) || (number instanceof Double)) {
          entry.datatype = FacetDatatype.DOUBLE;
          entry.value = number.doubleValue();
        } else if ((number instanceof Integer) || (number instanceof Long)) {
          entry.datatype = FacetDatatype.LONG;
          entry.value = number.longValue();          
        } else {
          logger.warn("Unknown number type {} in custom datatype in nested object {}. Not creating facet field.", number.getClass().getCanonicalName(), object.asText());
          return;
        }
      } catch (IOException e) {
        logger.warn("Problem parsing custom datatype {} in nested object {}: " + e.getMessage(), datatype, object.asText());
        return;
      }
    } else {
      entry.datatype = FacetDatatype.STRING;
      entry.value = value;
    }
    
    facets.add(entry);
  }
}
