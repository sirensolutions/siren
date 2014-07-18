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
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArrayMap;

import com.sindicetech.siren.analysis.ConciseJsonTokenizer;
import com.sindicetech.siren.analysis.filter.DatatypeAnalyzerFilter;
import com.sindicetech.siren.analysis.filter.PathEncodingFilter;
import com.sindicetech.siren.analysis.filter.PositionAttributeFilter;
import com.sindicetech.siren.analysis.filter.SirenPayloadFilter;

import java.io.Reader;
import java.util.Map.Entry;

/**
 * Wraps Siren's ExtendedJsonTokenizer as Lucene's {@link org.apache.lucene.analysis.Analyzer}.
 *
 * An analog of {@link com.sindicetech.siren.analysis.ExtendedJsonAnalyzer}
 * <p>
 * to specify a data type to analyzers mapping, add an array of types concatenated with class name by '|' to property
 * siren_analyzer.types in config/elasticsearch.yml
 * </p>
 * <p>
 * Example:
 * </p>
 * <p>
 * siren_analyzer.types: [
 * "http://www.w3.org/2001/XMLSchema#boolean|org.apache.lucene.analysis.core.WhitespaceAnalyzer",
 * "http://www.w3.org/2001/XMLSchema#string|org.apache.lucene.analysis.standard.StandardAnalyzer" ]
 * </p>
 */
public class ConciseJsonAnalyzer extends ExtendedJsonAnalyzer {

  private boolean generateTokensWithoutPath;

  public static final String NAME = "concise";

  public ConciseJsonAnalyzer() {}

  public void setGenerateTokensWithoutPath(boolean withoutPath) {
    this.generateTokensWithoutPath = withoutPath;
  }

  @Override
  protected TokenStreamComponents createComponents(String field, Reader reader) {
    final ConciseJsonTokenizer source = new ConciseJsonTokenizer(reader);

    final DatatypeAnalyzerFilter tt = new DatatypeAnalyzerFilter(source);
    for (final Entry<Object, Analyzer> e : indexAnalyzers.entrySet()) {
      tt.register((char[]) e.getKey(), e.getValue());
    }

    PathEncodingFilter pathEncodingFilter = new PathEncodingFilter(tt);
    pathEncodingFilter.setPreserveOriginal(this.generateTokensWithoutPath);

    TokenStream sink = new PositionAttributeFilter(pathEncodingFilter);
    sink = new SirenPayloadFilter(sink);
    return new TokenStreamComponents(source, sink);
  }

  public CharArrayMap<Analyzer> getIndexAnalyzers() {
    return indexAnalyzers;
  }
}
