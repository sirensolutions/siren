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
package com.sindicetech.siren.elasticsearch.plugin;

import java.util.Collection;

import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.codec.CodecModule;
import org.elasticsearch.index.query.IndexQueryParserModule;
import org.elasticsearch.plugins.AbstractPlugin;

import com.sindicetech.siren.elasticsearch.analysis.AnalyzerBinderProcessor;
import com.sindicetech.siren.elasticsearch.analysis.AnalyzerModule;
import com.sindicetech.siren.elasticsearch.analysis.NumericAnalyzerBinderProcessor;
import com.sindicetech.siren.elasticsearch.analysis.RegisterDatatypeModule;
import com.sindicetech.siren.elasticsearch.index.SirenFieldModule;
import com.sindicetech.siren.elasticsearch.index.SirenPostingsProvider;
import com.sindicetech.siren.elasticsearch.query.TreeParserModule;
import com.sindicetech.siren.elasticsearch.query.TreeQueryParserProcessor;
import com.sindicetech.siren.index.codecs.siren10.Siren10AForPostingsFormat;

/**
 * Registers all ElasticSearch SIREn's modules.
 */
public class SirenPlugin extends AbstractPlugin {

  @Override
  public String name() {
    return "siren-plugin";
  }

  @Override
  public String description() {
    return "SIREn plugin";
  }

  @Override
  public Collection<Class<? extends Module>> indexModules() {
    Collection<Class<? extends Module>> modules = Lists.newArrayList();
    modules.add(SirenFieldModule.class);
    modules.add(AnalyzerModule.class);
    modules.add(RegisterDatatypeModule.class);
    modules.add(TreeParserModule.class);
    return modules;
  }

  public void onModule(AnalysisModule module) {
    module.addProcessor(new AnalyzerBinderProcessor());
    module.addProcessor(new NumericAnalyzerBinderProcessor());
  }

  public void onModule(CodecModule module) {
    module.addPostingFormat(Siren10AForPostingsFormat.NAME, SirenPostingsProvider.class);
  }

  public void onModule(IndexQueryParserModule module) {
    module.addProcessor(new TreeQueryParserProcessor());
  }

}
