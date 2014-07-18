/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * This file is part of the SIREn project.
 *
 * SIREn is not an open-source software. It is owned by Sindice Limited. SIREn
 * is licensed for evaluation purposes only under the terms and conditions of
 * the Sindice Limited Development License Agreement. Any form of modification
 * or reverse-engineering of SIREn is forbidden. SIREn is distributed without
 * any warranty.
 */
package com.sindicetech.siren.solr.qparser.tree;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryParserHelper;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SyntaxError;

import com.sindicetech.siren.qparser.tree.ConciseTreeQueryParser;
import com.sindicetech.siren.qparser.tree.ExtendedTreeQueryParser;
import com.sindicetech.siren.solr.qparser.SirenQParser;
import com.sindicetech.siren.solr.schema.ConciseTreeField;
import com.sindicetech.siren.solr.schema.ExtendedTreeField;

import java.util.Map;

/**
 * Implementation of {@link com.sindicetech.siren.solr.qparser.SirenQParser} for the
 * {@link com.sindicetech.siren.qparser.tree.ConciseTreeQueryParser}.
 *
 * <p>
 *
 * The {@link TreeQParser} is in charge of parsing a SIREn's Concise Tree query
 * request.
 */
public class TreeQParser extends SirenQParser {

  public TreeQParser(final String qstr, final SolrParams localParams,
                     final SolrParams params, final SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }

  @Override
  protected Query parse(final String field, final String qstr,
                        final Map<String, Analyzer> datatypeConfig)
  throws SyntaxError {
    ExtendedTreeQueryParser parser;

    FieldType fieldType = req.getSchema().getField(field).getType();
    if (fieldType instanceof ConciseTreeField) {
      parser = new ConciseTreeQueryParser();
    } else if (fieldType instanceof ExtendedTreeField) {
      parser = new ExtendedTreeQueryParser();
    } else {
      throw new RuntimeException(String.format("Field %s is of type %s which is neither %s nor %s which are the only " +
          "supported.",
          field, fieldType.getClass().getName(), ConciseTreeField.class.getName(), ExtendedTreeField.class.getName()));
    }

    parser.setDefaultOperator(this.getDefaultOperator());
    parser.getKeywordQueryParser().setQNames(qnames);
    parser.getKeywordQueryParser().setDatatypeAnalyzers(datatypeConfig);

    try {
      return parser.parse(qstr, field);
    }
    catch (final QueryNodeException e) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
    }
  }

}
