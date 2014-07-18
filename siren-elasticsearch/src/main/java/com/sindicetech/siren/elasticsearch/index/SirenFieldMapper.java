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
package com.sindicetech.siren.elasticsearch.index;

import static org.elasticsearch.index.mapper.core.TypeParsers.parseField;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.codec.docvaluesformat.DocValuesFormatProvider;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatProvider;
import org.elasticsearch.index.fielddata.FieldDataType;
import org.elasticsearch.index.mapper.DocumentMapper;
import org.elasticsearch.index.mapper.InternalMapper;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.MapperParsingException;
import org.elasticsearch.index.mapper.ParseContext;
import org.elasticsearch.index.mapper.RootMapper;
import org.elasticsearch.index.mapper.core.AbstractFieldMapper;
import org.elasticsearch.index.similarity.SimilarityProvider;

/**
 * <p>
 * When a document is posted for indexing, copies the "_source" field to the new
 * {@value #SIREN_SOURCE} field in the {@link #preParse(ParseContext)} method.
 *
 * <p>
 * The rest of the class is only boilerplate to get a rootMapper registered:
 * <ul>
 * <li>{@link com.sindicetech.siren.elasticsearch.query.TreeQueryParserIndexComponent} registers the {@link TypeParser}
 * <li>the {@link TypeParser}, upon seeing the {@value #NAME} field, creates a {@link Builder} which
 * produces an instance of this {@link SirenFieldMapper} which is added as a rootMapper in
 * {@link DocumentMapper}.
 * </ul>
 *
 */
public class SirenFieldMapper extends AbstractFieldMapper<Void> implements InternalMapper, RootMapper {

  public static final String SIREN_SOURCE = "_siren_source";
  public static String NAME = "_siren";

  public static class Builder extends AbstractFieldMapper.Builder<Builder, SirenFieldMapper> {

    public Builder() {
      super(NAME, new FieldType(Defaults.FIELD_TYPE));
      builder = this;
      indexName = NAME;
    }

    @Override
    public SirenFieldMapper build(BuilderContext context) {
      // In case the mapping overrides these
      fieldType.setIndexed(true);
      fieldType.setTokenized(true);

      return new SirenFieldMapper(name, fieldType, indexAnalyzer, searchAnalyzer, true, true,
          postingsProvider, docValuesProvider, similarity, normsLoading, fieldDataSettings,
          context.indexSettings());
    }
  }

  public static class TypeParser implements Mapper.TypeParser {
    String name;

    @Override
    public Mapper.Builder<?, ?> parse(String name, Map<String, Object> node, ParserContext parserContext)
        throws MapperParsingException {
      SirenFieldMapper.Builder builder = new Builder();
      parseField(builder, builder.name, node, parserContext);
      return builder;
    }
  }

  protected SirenFieldMapper(org.elasticsearch.index.mapper.FieldMapper.Names names, float boost,
                             FieldType fieldType, Boolean docValues, NamedAnalyzer indexAnalyzer,
                             NamedAnalyzer searchAnalyzer, PostingsFormatProvider postingsFormat,
                             DocValuesFormatProvider docValuesFormat, SimilarityProvider similarity,
                             org.elasticsearch.index.mapper.FieldMapper.Loading normsLoading, Settings fieldDataSettings,
                             Settings indexSettings,
                             org.elasticsearch.index.mapper.core.AbstractFieldMapper.MultiFields multiFields,
                             org.elasticsearch.index.mapper.core.AbstractFieldMapper.CopyTo copyTo) {
    super(names, boost, fieldType, docValues, indexAnalyzer, searchAnalyzer, postingsFormat,
        docValuesFormat, similarity, normsLoading, fieldDataSettings, indexSettings, multiFields,
        copyTo);
  }

  protected SirenFieldMapper(org.elasticsearch.index.mapper.FieldMapper.Names names, float boost,
                             FieldType fieldType, Boolean docValues, NamedAnalyzer indexAnalyzer,
                             NamedAnalyzer searchAnalyzer, PostingsFormatProvider postingsFormat,
                             DocValuesFormatProvider docValuesFormat, SimilarityProvider similarity,
                             org.elasticsearch.index.mapper.FieldMapper.Loading normsLoading, Settings fieldDataSettings,
                             Settings indexSettings) {
    super(names, boost, fieldType, docValues, indexAnalyzer, searchAnalyzer, postingsFormat,
        docValuesFormat, similarity, normsLoading, fieldDataSettings, indexSettings);
  }

  public SirenFieldMapper() {
    this(new Names(NAME, NAME, NAME, NAME), 1.0f, new FieldType(Defaults.FIELD_TYPE), null, null,
        null, null, null, null, null, ImmutableSettings.EMPTY, ImmutableSettings.EMPTY);
  }

  protected SirenFieldMapper(String name, FieldType fieldType, NamedAnalyzer indexAnalyzer,
                             NamedAnalyzer searchAnalyzer, boolean enabled, boolean autoBoost,
                             PostingsFormatProvider postingsProvider, DocValuesFormatProvider docValuesProvider,
                             SimilarityProvider similarity, Loading normsLoading, @Nullable Settings fieldDataSettings,
                             Settings indexSettings) {
    super(new Names(name, name, name, name), 1.0f, fieldType, null, indexAnalyzer, searchAnalyzer,
        postingsProvider, docValuesProvider, similarity, normsLoading, fieldDataSettings,
        indexSettings);
  }

  @Override
  public Void value(Object value) {
    return null;
  }

  @Override
  public void preParse(ParseContext context) throws IOException {
    //copy _source field to a new SIREN_SOURCE field
    FieldType fieldType = new FieldType();
    fieldType.setIndexed(true);
    IndexableField field = new Field(SIREN_SOURCE, new String(context.source().toBytes(),
        Charset.forName("UTF-8")), fieldType);
    context.doc().add(field);
  }

  @Override
  public void postParse(ParseContext context) throws IOException {
    super.parse(context);
  }

  @Override
  public boolean includeInObject() {
    return true;
  }

  @Override
  public FieldType defaultFieldType() {
    return org.elasticsearch.index.mapper.core.AbstractFieldMapper.Defaults.FIELD_TYPE;
  }

  @Override
  public FieldDataType defaultFieldDataType() {
    return new FieldDataType("string");
  }

  @Override
  protected void parseCreateField(ParseContext context, List<Field> fields) throws IOException {
  }

  @Override
  protected String contentType() {
    return NAME;
  }

}
