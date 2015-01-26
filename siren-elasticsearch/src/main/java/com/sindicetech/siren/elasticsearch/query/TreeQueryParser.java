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
package com.sindicetech.siren.elasticsearch.query;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.jackson.core.JsonFactory;
import org.elasticsearch.common.jackson.core.JsonGenerator;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParsingException;

import com.sindicetech.siren.elasticsearch.analysis.ConciseJsonAnalyzer;
import com.sindicetech.siren.elasticsearch.analysis.ExtendedJsonAnalyzer;
import com.sindicetech.siren.elasticsearch.index.SirenFieldMapper;
import com.sindicetech.siren.qparser.keyword.config.ExtendedKeywordQueryConfigHandler;
import com.sindicetech.siren.qparser.tree.ConciseTreeQueryParser;
import com.sindicetech.siren.qparser.tree.ExtendedTreeQueryParser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Wrapper of SIREn's {@link com.sindicetech.siren.qparser.tree.ExtendedTreeQueryParser} for ElasticSearch that will use
 * {@link SirenFieldMapper#SIREN_SOURCE} as default search field
 */
public class TreeQueryParser implements QueryParser {

  public static final String NAME = "tree";

  private final JsonFactory jsonFactory = new JsonFactory();

  private Properties qnames;

  @Override
  public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
    XContentParser parser = parseContext.parser();
    String query = this.getQueryString(parser);
    return this.parse(SirenFieldMapper.SIREN_SOURCE, query, parseContext.analysisService(), parseContext.mapperService(), parseContext.index());
  }

  /**
   * The query parser context starts after the opening brace of our query object. The end condition of the loop
   * is based on the closing brace of the query object. While this means reading past our scope, this does not have
   * consequences on subsequent query parsing stages.
   */
  protected String getQueryString(XContentParser parser) throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonGenerator jsonGenerator = jsonFactory.createGenerator(stringWriter);
    jsonGenerator.writeStartObject();

    Token token;
    int depth = 0, array = 0;
    while (!(depth + array < 0)) { // stop when we have read the closing brace that is out of our context
      token = parser.nextToken();
      switch (token) {
        case START_OBJECT:
          jsonGenerator.writeStartObject();
          depth++;
          break;
        case END_OBJECT:
          jsonGenerator.writeEndObject();
          depth--;
          break;
        case START_ARRAY:
          jsonGenerator.writeStartArray();
          array++;
          break;
        case END_ARRAY:
          jsonGenerator.writeEndArray();
          array--;
          break;
        case FIELD_NAME:
          jsonGenerator.writeFieldName(parser.text());
          break;
        case VALUE_BOOLEAN:
          jsonGenerator.writeBoolean(Boolean.parseBoolean(parser.text()));
          break;
        case VALUE_NULL:
          jsonGenerator.writeNull();
          break;
        case VALUE_NUMBER:
          jsonGenerator.writeNumber(parser.text());
          break;
        case VALUE_STRING:
          jsonGenerator.writeString(parser.text());
          break;
        default:
          break;
      }
    }

    // no need to write the end object on our json generator since it has already been written when reading the
    // "out-of-context" closing brace
    jsonGenerator.close();
    return stringWriter.toString();
  }

  protected Query parse(final String field, final String query, final AnalysisService analysisService, final MapperService mapperService, final Index index) {
    try {
      ExtendedTreeQueryParser jsonParser = this.getJsonQueryParser(mapperService);

      registerCustomDatatypes(jsonParser, analysisService);
      if (qnames != null) {
        jsonParser.getKeywordQueryParser().setQNames(qnames);
      }
      return jsonParser.parse(query, field);
    }
    catch (QueryNodeException e) {
      throw new QueryParsingException(index, "Error parsing JSON query", e);
    }
  }

  protected ExtendedTreeQueryParser getJsonQueryParser(MapperService mapperService) {
    ExtendedTreeQueryParser parser;
    Analyzer analyzer = ((NamedAnalyzer)mapperService.name(SirenFieldMapper.SIREN_SOURCE).mapper().indexAnalyzer()).analyzer();
    if (analyzer instanceof ExtendedJsonAnalyzer) {
      ExtendedJsonAnalyzer jsonAnalyzer = (ExtendedJsonAnalyzer) analyzer;
      if (analyzer instanceof ConciseJsonAnalyzer) {
        parser = new ConciseTreeQueryParser();
      } else {
        parser = new ExtendedTreeQueryParser();
      }
    } else {
      throw new RuntimeException("No query parser corresponding to analyzer of class " + analyzer.getClass());
    }
    return parser;
  }

  /**
   * Helper method for now.
   *
   * TODO: when GH-9 is implemented (thread-local parsers) then this would be done for each thread-local parser only once.
   *
   * @param jsonParser
   * @param analysisService
   */
  private void registerCustomDatatypes(ExtendedTreeQueryParser jsonParser, AnalysisService analysisService) {
    Map<String, Analyzer> map = jsonParser.getKeywordQueryParser().getQueryConfigHandler()
      .get(ExtendedKeywordQueryConfigHandler.KeywordConfigurationKeys.DATATYPES_ANALYZERS);
    if (map == null) { // this is perhaps not necessary....?
      map = new HashMap<String, Analyzer>();
      jsonParser.getKeywordQueryParser().getQueryConfigHandler()
        .set(ExtendedKeywordQueryConfigHandler.KeywordConfigurationKeys.DATATYPES_ANALYZERS, map);
    }
    ExtendedJsonAnalyzer sirenAnalyzer = (ExtendedJsonAnalyzer) analysisService.analyzer(ExtendedJsonAnalyzer.NAME).analyzer();
    for (final Entry<Object, Analyzer> e : sirenAnalyzer.getQueryAnalyzers().entrySet()) {
      map.put(new String((char[]) e.getKey()), e.getValue());
    }
  }

  public void setQnames(Properties qnames) {
    this.qnames = qnames;
  }

  @Override
  public String[] names() {
    return new String[] { NAME };
  }
}
