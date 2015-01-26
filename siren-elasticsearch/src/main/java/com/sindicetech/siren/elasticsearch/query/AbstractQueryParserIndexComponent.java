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

import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;

import java.util.Map.Entry;
import java.util.Properties;

public abstract class AbstractQueryParserIndexComponent extends AbstractIndexComponent {

  protected AbstractQueryParserIndexComponent(Index index, Settings indexSettings) {
    super(index, indexSettings);
  }

  public static Properties extractQnames(Settings settings) {
    Settings qnameSettings = settings.getByPrefix("siren.analysis.qname.");

    Properties result = new Properties();

    ImmutableMap<String, String> qnameMap = qnameSettings.getAsMap();
    for (Entry<String, String> entry : qnameMap.entrySet()) {
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
