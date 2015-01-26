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

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.PreBuiltAnalyzerProviderFactory;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;

/**
 * Boilerplate for registering SIREn's JSON analyzers.
 */
public class AnalyzerIndexComponent extends AbstractIndexComponent {

  @Inject
  public AnalyzerIndexComponent(Index index, Settings settings, IndicesAnalysisService indicesAnalysisService) {
    super(index, settings);

    // Register the json analyzer in the analysis service

    indicesAnalysisService.analyzerProviderFactories().put(
      ExtendedJsonAnalyzer.NAME,
      new PreBuiltAnalyzerProviderFactory(ExtendedJsonAnalyzer.NAME, AnalyzerScope.INDEX, new ExtendedJsonAnalyzer()));

    // Register the concise json analyzer in the analysis service

    boolean wildcardAttributeEnabled = indexSettings.getAsBoolean(ConciseJsonAnalyzerProvider.WILDCARD_ATTRIBUTE_SETTING,
      ConciseJsonAnalyzerProvider.DEFAULT_WIDLCARD_ATTRIBUTE);
    ConciseJsonAnalyzer conciseAnalyzer = new ConciseJsonAnalyzer();
    conciseAnalyzer.setGenerateTokensWithoutPath(wildcardAttributeEnabled);

    indicesAnalysisService.analyzerProviderFactories().put(
      ConciseJsonAnalyzer.NAME,
      new PreBuiltAnalyzerProviderFactory(ConciseJsonAnalyzer.NAME, AnalyzerScope.INDEX, conciseAnalyzer));
  }

}
