/**
 * A SIREn JSON query parser wrapper for elastic search.
 *
 * This package contains classes that wrap and register SIREn's query parser:
 * {@link com.sindicetech.siren.elasticsearch.query.TreeQueryParser})
 * 
 * The parser is registered using the corresponding {@link com.sindicetech.siren.elasticsearch.query.TreeQueryParserIndexComponent}
 * which is bound in the {@link com.sindicetech.siren.elasticsearch.query.TreeParserModule} which
 * is registered in {@link com.sindicetech.siren.elasticsearch.plugin.SirenPlugin}.
 */
package com.sindicetech.siren.elasticsearch.query;
