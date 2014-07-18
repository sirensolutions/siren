/**
 * This module and package implement the experimental nested faceting feature.
 *
 * <h2>Configuring Siren Nested Faceting</h2>
 *
 * <p>To switch this feature on it is necessary to register an update processor as follows:
 * (also described in {@link com.sindicetech.siren.solr.facet.SirenFacetProcessorFactory}
 *
 * <h3>Set up the processor factory with datatype mappings</h3>
 * (<code>solrconfig.xml</code>)
 <pre style="overflow:auto;">{@code
<!-- Generate facet fields -->
<updateRequestProcessorChain name="generate-facets-processor">
<processor class="siren.SirenFacetProcessorFactory">
<lst name="datatypeMapping">
<str name="valueClass">http://www.w3.org/2001/XMLSchema#long</str>
<str name="fieldType">tlong</str>
</lst>
<lst name="datatypeMapping">
<str name="valueClass">http://www.w3.org/2001/XMLSchema#double</str>
<str name="fieldType">tdouble</str>
</lst>
<lst name="datatypeMapping">
<str name="valueClass">http://www.w3.org/2001/XMLSchema#boolean</str>
<str name="valueClass">http://www.w3.org/2001/XMLSchema#string</str>
<str name="fieldType">string</str>
<int name="maxFieldSize">80</int>&
lt;!-- Fields with strings longer than 80 chars will be skipped when generating facet fields. Works only for the string fieldType -->
</lst>
</processor>
</updateRequestProcessorChain>
 }</pre>
 *
 * <h3>Specify field types</h3>
 * (<code>schema.xml</code>)
 *
 <pre style="overflow:auto;">{@code
<fieldType name="tlong" class="solr.TrieLongField" precisionStep="8" omitNorms="true" />
<fieldType name="tdouble" class="solr.TrieDoubleField" precisionStep="8" omitNorms="true" />
<fieldType name="string" class="solr.StrField" sortMissingLast="true" omitNorms="true"/>
}</pre>
 *
 * <p>The mapping specifies which solr field type (fieldType) should be used for which xsd datatype (valueClass). Any used
 * fieldType <em>must be specified in <code>schema.xml</code></em>. For the above configuration it is tlong, tdouble, and string:
 *
 * <h3>Register the update processor</h3>
 * <code>(solrconfig.xml)</code>
 <pre style="overflow:auto;">{@code
<!-- Update Request Handler -->
<requestHandler name="/update" class="solr.UpdateRequestHandler" >
<lst name="defaults">
<str name="update.chain">generate-facets-processor</str>
</lst>
</requestHandler>
 }</pre>
 *
 * <p>The facet update processor will generate new facet fields for each siren field and for each path
 * in a document during indexing.
 *
 * <p> Example
 *
 <pre style="overflow:auto;">{@code
  {
    name : "john",
    age : 32,
    address : {
      street : "Sunset Blvd.",
      city: "LA"
    },
    children : [
      { name : "John Jr.", age : 8 },
      { name : "Kate", age: 5 }
    ]
  }
}</pre>
 * <p> For this document stored in a ExtendedTreeField called json, the following facet fields will be generated: <br><br>
 *
 * string.json.name<br>
 * long.json.age<br>
 * string.json.street<br>
 * string.json.city<br>
 * string.json.children.name<br>
 * long.json.children.age<br>
 *
 * <p>If another document comes that has a different type for some of the fields:
 <pre style="overflow:auto;">{@code
  {
    name : "orla",
    age : "35",
  }
  }</pre>
 *
 * <p>then a new field will be generated for that path:<br><br>
 *
 * string.json.age<br>
 *
 * <p>To summarize, the nested facet field name is composed as follows: [datatype].[sirenFieldName].[path]<br>
 *
 * <h3>Register {@link com.sindicetech.siren.solr.facet.FacetsPathsRequestHandler}</h3>
 * (<code>solrconfig.xml</code>)
 *
 <pre style="overflow:auto;">{@code
<requestHandler name="/fprh"
lass="com.sindicetech.siren.solr.facets.FacetsPathsRequestHandler"/>
 }</pre>
 *
 * <p>This is necessary to be able to query the facet fields.
 *
 * <h3>How to query facets</h3>
 *
 * <p> The list of generated facet fields can be retrieved by issuing a GET request to the handler.
 *
   <p><code>http://localhost:8983/solr/collection1/fprh&wt=json</code>
 *
 * <p>An example response:
 <pre style="overflow:auto;">{@code
 {
 "responseHeader": {
 "status": 0,
 "QTime": 11
 },
 "fields": [
 "boolean.json.providerships.is_past",
 "boolean.json.relationships.is_past",
 "double.json.acquisition.price_amount",
 "double.json.acquisitions.price_amount",
 "long.json.acquisition.acquired_day",
 "long.json.acquisition.acquired_month",
 "long.json.acquisition.acquired_year",
 "string.json.acquisition",
 "string.json.acquisition.acquiring_company.name",
 "string.json.acquisition.acquiring_company.permalink",
 ]
 }
}</pre>
 */
package com.sindicetech.siren.solr.facet;
