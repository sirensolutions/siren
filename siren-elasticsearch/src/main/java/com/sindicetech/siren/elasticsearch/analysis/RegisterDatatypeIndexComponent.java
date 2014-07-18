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
package com.sindicetech.siren.elasticsearch.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.NamedAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registers the datatypes and custom datatypes with {@link ExtendedJsonAnalyzer} and
 * {@link ConciseJsonAnalyzer}.
 */
public class RegisterDatatypeIndexComponent extends AbstractIndexComponent {

  @Inject
  protected RegisterDatatypeIndexComponent(final Index index, final Settings indexSettings,
                                           final AnalysisService analysisService) {
    super(index, indexSettings);

    Map<String, String[]> mappings = this.getDatatypeMappings(indexSettings);
    checkAndRegisterDatatypes(analysisService, mappings);
  }

  /**
   * Extract the datatype mappings from the settings. A datatype mapping is composed of a datatype identifier and of
   * an array of analyzer names. The first element of the array indicates the name of the index-time analyzer. the
   * second element of the array indicates the name of the query-time analyzer. When the array is contains a single name,
   * it means that the same analyzer should be used for both index-time and query-time analysis.
   */
  private Map<String, String[]> getDatatypeMappings(final Settings settings) {
    Map<String, String[]> mapping = new HashMap<String, String[]>();

    Settings datatypesSettings = settings.getByPrefix("siren.analysis.datatype.");
    List<String> datatypes = this.getDatatypes(datatypesSettings);
    for (String datatype : datatypes) {
      String[] analyzers = this.getAnalyzers(datatypesSettings.getByPrefix(datatype + '.'));
      mapping.put(datatype, analyzers);
    }

    return mapping;
  }

  /**
   * Extract a list of datatype identifiers from the settings.
   */
  private List<String> getDatatypes(final Settings datatypesSettings) {
    List<String> datatypes = new ArrayList<String>();
    for (String key : datatypesSettings.getAsMap().keySet()) {
      datatypes.add(key.substring(0, key.lastIndexOf('.')));
    }
    return datatypes;
  }

  /**
   * Extract the analyzer names from a datatype's settings
   */
  private String[] getAnalyzers(final Settings datatypeSettings) {
    String[] analyzers = new String[datatypeSettings.getAsMap().size()];
    analyzers[0] = datatypeSettings.get("index_analyzer").trim();
    if (analyzers.length == 2) {
      analyzers[1] = datatypeSettings.get("search_analyzer").trim();
    }
    return analyzers;
  }

  /**
   * Register datatypes in the {@link ExtendedJsonAnalyzer}s.
   *
   * Retrieve the custom datatypes and throw an exception if for some of them analysisService returns null (i.e.
   * they are invalid analyzer names specified in elasticsearch.yml).
   *
   * @throws IllegalArgumentException if an unknown analyzer is used for a datatype.
   */
  private void checkAndRegisterDatatypes(final AnalysisService analysisService, final Map<String, String[]> mappings) {
    for (String datatype : mappings.keySet()) {
      String[] analyzers = mappings.get(datatype);

      if (analyzers.length == 1) {
        Analyzer analyzer = this.getAnalyzer(analysisService, analyzers[0]);

        // register the datatype in the json analyzers
        this.getJsonAnalyzer(analysisService, ExtendedJsonAnalyzer.NAME).registerDatatype(datatype.toCharArray(), analyzer);
        this.getJsonAnalyzer(analysisService, ConciseJsonAnalyzer.NAME).registerDatatype(datatype.toCharArray(), analyzer);
      }
      else {
        Analyzer indexAnalyzer = this.getAnalyzer(analysisService, analyzers[0]);
        Analyzer queryAnalyzer = this.getAnalyzer(analysisService, analyzers[1]);

        // register the datatype in the json analyzers
        this.getJsonAnalyzer(analysisService, ExtendedJsonAnalyzer.NAME).registerDatatype(datatype.toCharArray(), indexAnalyzer, queryAnalyzer);
        this.getJsonAnalyzer(analysisService, ConciseJsonAnalyzer.NAME).registerDatatype(datatype.toCharArray(), indexAnalyzer, queryAnalyzer);
      }
    }
  }

  private Analyzer getAnalyzer(final AnalysisService analysisService, final String name) {
    NamedAnalyzer analyzer = (NamedAnalyzer) analysisService.analyzer(name);
    if (analyzer == null || analyzer.analyzer() == null) {
      throw new IllegalArgumentException("Unknown analyzer: " + name);
    }
    return analyzer.analyzer();
  }

  private ExtendedJsonAnalyzer getJsonAnalyzer(final AnalysisService analysisService, final String name) {
    return (ExtendedJsonAnalyzer) this.getAnalyzer(analysisService, name);
  }

}

