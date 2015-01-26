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
import org.elasticsearch.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindicetech.siren.analysis.ExtendedJsonTokenizer;
import com.sindicetech.siren.analysis.filter.DatatypeAnalyzerFilter;
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
public class ExtendedJsonAnalyzer extends Analyzer {

  // index-time analyzers to be registered as datatypes
  protected final CharArrayMap<Analyzer> indexAnalyzers;

  // query-time analyzers to be registered as datatypes
  protected final CharArrayMap<Analyzer> queryAnalyzers;

  protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  public static final String NAME = "extended";

  public ExtendedJsonAnalyzer() {
    this.indexAnalyzers = new CharArrayMap<Analyzer>(Version.CURRENT.luceneVersion, 64, false);
    this.queryAnalyzers = new CharArrayMap<Analyzer>(Version.CURRENT.luceneVersion, 64, false);
  }

  /**
   * Assign an {@link org.apache.lucene.analysis.Analyzer} to that key. That analyzer is used by
   * the {@link com.sindicetech.siren.analysis.ExtendedJsonTokenizer} to process tokens, and by the
   * {@link com.sindicetech.siren.qparser.keyword.ExtendedKeywordQueryParser}.
   *
   * @param datatype
   *          The datatype key
   * @param a
   *          the associated {@link org.apache.lucene.analysis.Analyzer} that will be used both for indexing and querying
   */
  public void registerDatatype(final char[] datatype, final Analyzer a) {
    this.registerDatatype(datatype, a, a);
  }

  /**
   * Assign an index-time and query-time {@link org.apache.lucene.analysis.Analyzer}s to that key. The index-time
   * analyzer is used by the {@link com.sindicetech.siren.analysis.ExtendedJsonTokenizer} to process tokens, and the query-time
   * analyzer by the {@link com.sindicetech.siren.qparser.keyword.ExtendedKeywordQueryParser}.
   *
   * @param datatype
   *          The datatype key
   * @param indexAnalyzer
   *          the index-time {@link org.apache.lucene.analysis.Analyzer}
   * @param queryAnalyzer
   *          the query-time {@link org.apache.lucene.analysis.Analyzer}
   */
  public void registerDatatype(final char[] datatype, final Analyzer indexAnalyzer, final Analyzer queryAnalyzer) {
    if (!indexAnalyzers.containsKey(datatype)) {
      indexAnalyzers.put(datatype, indexAnalyzer);
    }
    if (!queryAnalyzers.containsKey(datatype)) {
      queryAnalyzers.put(datatype, queryAnalyzer);
    }
  }

  @Override
  protected TokenStreamComponents createComponents(String field, Reader reader) {
    final ExtendedJsonTokenizer source = new ExtendedJsonTokenizer(reader);

    final DatatypeAnalyzerFilter tt = new DatatypeAnalyzerFilter(source);
    for (final Entry<Object, Analyzer> e : indexAnalyzers.entrySet()) {
      tt.register((char[]) e.getKey(), e.getValue());
    }
    TokenStream sink = new PositionAttributeFilter(tt);
    sink = new SirenPayloadFilter(sink);
    return new TokenStreamComponents(source, sink);
  }

  public CharArrayMap<Analyzer> getIndexAnalyzers() {
    return indexAnalyzers;
  }

  public CharArrayMap<Analyzer> getQueryAnalyzers() {
    return queryAnalyzers;
  }
}
