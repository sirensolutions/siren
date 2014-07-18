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
package com.sindicetech.siren.demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene46.Lucene46Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.sindicetech.siren.analysis.ConciseJsonTokenizer;
import com.sindicetech.siren.analysis.filter.DatatypeAnalyzerFilter;
import com.sindicetech.siren.analysis.filter.PathEncodingFilter;
import com.sindicetech.siren.analysis.filter.PositionAttributeFilter;
import com.sindicetech.siren.analysis.filter.SirenPayloadFilter;
import com.sindicetech.siren.index.codecs.siren10.Siren10AForPostingsFormat;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * This class shows how to configure the SIREn codec for indexing JSON data
 * into a particular field.
 */
public class SimpleIndexer {

  private final Directory dir;
  private final IndexWriter writer;

  public static final String DEFAULT_ID_FIELD = "id";
  public static final String DEFAULT_SIREN_FIELD = "siren-field";

  public SimpleIndexer(final File path) throws IOException {
    dir = FSDirectory.open(path);
    writer = this.initializeIndexWriter();
  }

  public void close() throws IOException {
    writer.close();
    dir.close();
  }

  private IndexWriter initializeIndexWriter() throws IOException {
    final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
      this.initializeAnalyzer());

    // Register the SIREn codec
    config.setCodec(new Siren10Codec());

    return new IndexWriter(dir, config);
  }

  private Analyzer initializeAnalyzer() {
    return new Analyzer() {

      @Override
      protected TokenStreamComponents createComponents(final String fieldName,
                                                       final Reader reader) {
        final Version matchVersion = Version.LUCENE_46;
        final ConciseJsonTokenizer src = new ConciseJsonTokenizer(reader);
        TokenStream tok = new DatatypeAnalyzerFilter(src,
          new StandardAnalyzer(matchVersion),
          new StandardAnalyzer(matchVersion));
        tok = new LowerCaseFilter(matchVersion, tok);
        // The PathEncodingFilter is mandatory only for the ConciseJsonTokenizer
        PathEncodingFilter pathFilter = new PathEncodingFilter(tok);
        // here we tell the path filter to preserve the original tokens,
        // it will index the value with and without prepending the path
        pathFilter.setPreserveOriginal(true);
        // The PositionAttributeFilter and SirenPayloadFilter are mandatory
        // and must be always the last filters in your token stream
        tok = new PositionAttributeFilter(pathFilter);
        tok = new SirenPayloadFilter(tok);
        return new TokenStreamComponents(src, tok);
      }

    };
  }

  public void addDocument(final String id, final String json)
  throws IOException {
    final Document doc = new Document();

    doc.add(new StringField(DEFAULT_ID_FIELD, id, Store.YES));

    final FieldType sirenFieldType = new FieldType();
    sirenFieldType.setIndexed(true);
    sirenFieldType.setTokenized(true);
    sirenFieldType.setOmitNorms(true);
    sirenFieldType.setStored(false);
    sirenFieldType.setStoreTermVectors(false);

    doc.add(new Field(DEFAULT_SIREN_FIELD, json, sirenFieldType));

    writer.addDocument(doc);
  }

  public void commit() throws IOException {
    writer.commit();
  }

  /**
   * Simple example of a SIREn codec that will use the SIREn posting format
   * for a given field.
   */
  private class Siren10Codec extends Lucene46Codec {

    PostingsFormat defaultTestFormat = new Siren10AForPostingsFormat();

    public Siren10Codec() {
      Codec.setDefault(this);
    }

    @Override
    public PostingsFormat getPostingsFormatForField(final String field) {
      if (field.equals(DEFAULT_SIREN_FIELD)) {
        return defaultTestFormat;
      }
      else {
        return super.getPostingsFormatForField(field);
      }
    }

    @Override
    public String toString() {
      return "Siren10Codec[" + defaultTestFormat.toString() + "]";
    }

  }

}
