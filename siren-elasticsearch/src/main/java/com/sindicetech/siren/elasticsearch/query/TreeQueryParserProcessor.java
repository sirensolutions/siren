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

import org.elasticsearch.index.query.IndexQueryParserModule;

/**
 * Binds query parser names to the {@link TreeQueryParserFactoryProvider}.
 */
public class TreeQueryParserProcessor extends IndexQueryParserModule.QueryParsersProcessor {

  @Override
  public void processXContentQueryParsers(XContentQueryParsersBindings bindings) {
    bindings.binder().addBinding(TreeQueryParser.NAME).toProvider(TreeQueryParserFactoryProvider.class);
  }
}
