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

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParserFactory;
import org.elasticsearch.index.settings.IndexSettings;

import java.util.Properties;

/**
 * Factory for creating {@link TreeQueryParser}s
 */
public class TreeQueryParserFactory implements QueryParserFactory {
  private Properties qnames;
  
  @Inject
  public TreeQueryParserFactory(@IndexSettings Settings settings, MapperService mapperService) {
    this.qnames = AbstractQueryParserIndexComponent.extractQnames(settings);
  }

  @Override
  public QueryParser create(String name, @IndexSettings Settings settings) {
    if (TreeQueryParser.NAME.equals(name)) {
      TreeQueryParser parser = new TreeQueryParser();
      parser.setQnames(qnames);
      return parser;
    }

    throw new IllegalArgumentException("Unexpected parser name: " + name);
  }
}
