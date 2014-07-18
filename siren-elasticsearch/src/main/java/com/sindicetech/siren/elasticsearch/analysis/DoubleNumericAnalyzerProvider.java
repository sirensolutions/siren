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

import com.sindicetech.siren.analysis.DoubleNumericAnalyzer;

/**
 * Instantiates a single instance of the {@link com.sindicetech.siren.analysis.LongNumericAnalyzer} and
 * provides it.
 */
public class DoubleNumericAnalyzerProvider extends AbstractIndexAnalyzerProvider<DoubleNumericAnalyzer> {

  private final DoubleNumericAnalyzer analyzer;

  private static final int DEFAULT_PRECISION_STEP = 8;

  public static final String NAME = "double";

  @Inject
  public DoubleNumericAnalyzerProvider(Index index, @IndexSettings Settings indexSettings,
                                       @Assisted String name, @Assisted Settings settings) {
    super(index, indexSettings, name, settings);
    this.analyzer = new DoubleNumericAnalyzer(DEFAULT_PRECISION_STEP);
  }

  @Override
  public DoubleNumericAnalyzer get() {
    return this.analyzer;
  }

}
