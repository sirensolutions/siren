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

import static org.apache.solr.common.SolrException.ErrorCode.SERVER_ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.AddSchemaFieldsUpdateProcessorFactory;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;

/**
 * <p>A simple factory that simply instantiates the {@link SirenFacetProcessor}.
 * 
 * <p>It reads configuration of the following format (to be added to solrconfig*.xml):
 *
 <pre style="overflow:auto;">{@code
<!-- Generate facet fields -->
<updateRequestProcessorChain name="generate-facets-processor">
<processor class="siren.SirenFacetProcessorFactory">
<lst name="datatypeMapping">
<str name="valueClass">http://www.w3.org/2001/XMLSchema#long</str>
<str name="fieldType">tlong</str>
</lst>
<lst name="datatypeMapping">
<str name="valueClass">http://www.w3.org/2001/XMLSchema#double</str>
<str name="fieldType">tdouble</str>
</lst>
<lst name="datatypeMapping">
<str name="valueClass">http://www.w3.org/2001/XMLSchema#boolean</str>
<str name="valueClass">http://www.w3.org/2001/XMLSchema#string</str>
<str name="fieldType">string</str>
<int name="maxFieldSize">80</int>&
lt;!-- Fields with strings longer than 80 chars will be skipped when generating facet fields. Works only for the string fieldType -->
</lst>
</processor>
</updateRequestProcessorChain>
}</pre>
 * 
 * <p>and passes it on to the {@link SirenFacetProcessor} as a list of {@link TypeMapping}s.
 *
 * <p>The <code>solrconfig*.xml</code> file has to also register the processor for use during update requests:
 *
 <pre style="overflow:auto;">{@code
<!-- Update Request Handler -->
<requestHandler name="/update" class="solr.UpdateRequestHandler" >
<lst name="defaults">
<str name="update.chain">generate-facets-processor</str>
</lst>
</requestHandler>
}</pre>
 *
 * @experimental Can change in the next release.
 */
public class SirenFacetProcessorFactory extends UpdateRequestProcessorFactory {
  private static final String TYPE_MAPPING_PARAM = "typeMapping";
  private static final String VALUE_CLASS_PARAM = "valueClass";
  private static final String FIELD_TYPE_PARAM = "fieldType";
  private static final String MAX_FIELD_SIZE_PARAM = "maxFieldSize";

  static List<TypeMapping> typeMappings;

  @SuppressWarnings("rawtypes")
  @Override
  public void init(NamedList args) {
    typeMappings = parseTypeMappings(args);

    super.init(args);
  }

  /**
   * 
   * Taken from {@link AddSchemaFieldsUpdateProcessorFactory}'s parseTypeMappings().
   * 
   * @param args
   * @return
   */
  @SuppressWarnings("rawtypes")
  private List<TypeMapping> parseTypeMappings(NamedList args) {
    List<TypeMapping> typeMappings = new ArrayList<TypeMapping>();
    List typeMappingsParams = args.getAll(TYPE_MAPPING_PARAM);
    for (Object typeMappingObj : typeMappingsParams) {
      if (null == typeMappingObj) {
        throw new SolrException(SERVER_ERROR, "'" + TYPE_MAPPING_PARAM
            + "' init param cannot be null");
      }
      if (!(typeMappingObj instanceof NamedList)) {
        throw new SolrException(SERVER_ERROR, "'" + TYPE_MAPPING_PARAM
            + "' init param must be a <lst>");
      }
      NamedList typeMappingNamedList = (NamedList) typeMappingObj;

      Object fieldTypeObj = typeMappingNamedList.remove(FIELD_TYPE_PARAM);
      if (null == fieldTypeObj) {
        throw new SolrException(SERVER_ERROR, "Each '" + TYPE_MAPPING_PARAM
            + "' <lst/> must contain a '" + FIELD_TYPE_PARAM + "' <str>");
      }
      if (!(fieldTypeObj instanceof CharSequence)) {
        throw new SolrException(SERVER_ERROR, "'" + FIELD_TYPE_PARAM
            + "' init param must be a <str>");
      }
      if (null != typeMappingNamedList.get(FIELD_TYPE_PARAM)) {
        throw new SolrException(SERVER_ERROR, "Each '" + TYPE_MAPPING_PARAM
            + "' <lst/> may contain only one '" + FIELD_TYPE_PARAM + "' <str>");
      }
      String fieldType = fieldTypeObj.toString();

      Object maxFieldSizeObj = typeMappingNamedList.remove(MAX_FIELD_SIZE_PARAM);
      if (maxFieldSizeObj != null) {
        if (!(maxFieldSizeObj instanceof Integer)) {
          throw new SolrException(SERVER_ERROR, "'" + FIELD_TYPE_PARAM
              + "' init param must be an <int>");          
        }
      } 
      
      Collection<String> valueClasses = typeMappingNamedList.removeConfigArgs(VALUE_CLASS_PARAM);
      if (valueClasses.isEmpty()) {
        throw new SolrException(SERVER_ERROR, "Each '" + TYPE_MAPPING_PARAM
            + "' <lst/> must contain at least one '" + VALUE_CLASS_PARAM + "' <str>");
      }
      typeMappings.add(new TypeMapping(fieldType, (Integer) maxFieldSizeObj, valueClasses));

      if (0 != typeMappingNamedList.size()) {
        throw new SolrException(SERVER_ERROR, "Unexpected '" + TYPE_MAPPING_PARAM
            + "' init sub-param(s): '" + typeMappingNamedList.toString() + "'");
      }
      args.remove(TYPE_MAPPING_PARAM);
    }
    
    checkMappingExists(typeMappings, FacetDatatype.BOOLEAN.xsdDatatype);
    checkMappingExists(typeMappings, FacetDatatype.DOUBLE.xsdDatatype);
    checkMappingExists(typeMappings, FacetDatatype.LONG.xsdDatatype);
    checkMappingExists(typeMappings, FacetDatatype.STRING.xsdDatatype);
    
    return typeMappings;
  }
  
  private void checkMappingExists(List<TypeMapping> typeMappings, String type) {
    if (!containsMappingFor(typeMappings, type)) {
      throw new SolrException(SERVER_ERROR, "Siren facet configuration must contain mapping for valueClass " + type);      
    }    
  }

  private static boolean containsMappingFor(List<TypeMapping> mappings, String valueClass) {
    for (TypeMapping mapping : mappings) {
      if (mapping.valueClasses.contains(valueClass)) {
        return true;
      }
    }
    
    return false;
  }
  
  public TypeMapping getTypeMappingValueClass(String valueClass) {
    for (TypeMapping mapping : typeMappings) {
      if (mapping.valueClasses.contains(valueClass)) {
        return mapping;
      }
    }

    return null;
  }
  
  @Override
  public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp,
      UpdateRequestProcessor next) {

    return new SirenFacetProcessor(next, typeMappings);
  }

}
