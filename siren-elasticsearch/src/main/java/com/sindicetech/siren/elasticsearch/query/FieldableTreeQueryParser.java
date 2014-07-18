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

import org.apache.lucene.search.Query;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentParser.Token;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParsingException;
import com.sindicetech.siren.elasticsearch.index.SirenFieldMapper;

import java.io.IOException;

/**
 * An extension of {@link TreeQueryParser} which allows the specification
 * of a field to be queried.
 * 
 * If this kind of query parser is used then the following sequence of Marvel sense commands should work:
 * 
POST /t

PUT /t/movie/_mapping?pretty
{ "movie" : { "properties" :
         {
         "info" : {
                  "analyzer" : "json",
                  "postings_format" : "Siren10AFor",
                  "store" : "no",
                  "type" : "string"
         }
            }
         }
}

PUT /t/movie/1?pretty
{"id":1, "title":"other search engine", "info":"{\"article\":\"This is an article  about something different. then SIREn\"}"}

POST /t/movie/_search?pretty
{"query":{"json":{
  "jquery":"{\"node\":{\"query\":\"SIREn\"}}",
  "jfield":"info"
    }
  }
}
 * ... where I am not entirely certain about correctness of the last search query, it should be pretty close though. 
 * 
 */
public abstract class FieldableTreeQueryParser extends TreeQueryParser {

  private static final String FIELD = "jfield";
  private static final String QUERY = "jquery";

  @Override
  public Query parse(QueryParseContext parseContext) throws IOException, QueryParsingException {
    XContentParser parser = parseContext.parser();

    String queryField = SirenFieldMapper.SIREN_SOURCE;
    String query = null;

    while (parser.nextToken() != null) {
      if (parser.currentToken() == Token.FIELD_NAME) {
        switch (parser.currentName()) {
          case FIELD:
            queryField = parser.text();
            break;

          case QUERY:
            parser.nextToken();
            if (parser.currentToken() != Token.START_OBJECT) {
              throw new QueryParsingException(parseContext.index(), "The value of the field " + QUERY
                + " must be a JSON object containing a SIREn query");
            }

            query = this.getQueryString(parser);

            parser.nextToken();
            if (parser.currentToken() != Token.END_OBJECT) {
              throw new QueryParsingException(parseContext.index(), "JSON query object not properly closed");
            }

          default:
            throw new QueryParsingException(parseContext.index(), "Invalid query object: unexpected field '" +
              parser.currentName() + '"');

        }
      } else {
        throw new QueryParsingException(parseContext.index(), "Invalid query object");
      }
    }

    return this.parse(queryField, query, parseContext.analysisService(), parseContext.mapperService(), parseContext.index());
  }

}
