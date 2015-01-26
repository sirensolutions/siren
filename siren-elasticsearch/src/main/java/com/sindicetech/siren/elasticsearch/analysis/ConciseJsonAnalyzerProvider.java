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
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractIndexAnalyzerProvider;
import org.elasticsearch.index.settings.IndexSettings;

/**
 * Instantiates a single instance of the {@link ConciseJsonAnalyzer} and
 * provides it.
 */
public class ConciseJsonAnalyzerProvider extends AbstractIndexAnalyzerProvider<ConciseJsonAnalyzer> {

  public static final boolean DEFAULT_WIDLCARD_ATTRIBUTE = false;

  public static final String WILDCARD_ATTRIBUTE_SETTING = "siren.analysis.concise.attribute_wildcard.enabled";

  private final ConciseJsonAnalyzer analyzer;

  @Inject
  public ConciseJsonAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                     @Assisted String name, @Assisted Settings settings) {
    super(index, indexSettings, name, settings);
    boolean wildcardAttributeEnabled = indexSettings.getAsBoolean(WILDCARD_ATTRIBUTE_SETTING, DEFAULT_WIDLCARD_ATTRIBUTE);
    this.analyzer = new ConciseJsonAnalyzer();
    this.analyzer.setGenerateTokensWithoutPath(wildcardAttributeEnabled);
  }

  @Override
  public ConciseJsonAnalyzer get() {
    return this.analyzer;
  }

}
