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
package com.sindicetech.siren.solr;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorChain;

/**
 * Taken from solr-core UpdateProcessorTestBase.
 * 
 * https://github.com/apache/lucene-solr/blob/lucene_solr_4_6/solr/core/src/test/org/apache/solr/update/processor/UpdateProcessorTestBase.java
 * 
 */
public class UpdateProcessorTestBase extends SolrServerTestCase {
  /**
   * Convenience method for building up SolrInputFields
   */
  final SolrInputField field(String name, float boost, Object... values) {
    SolrInputField f = new SolrInputField(name);
    for (Object v : values) {
      f.addValue(v, 1.0F);
    }
    f.setBoost(boost);
    return f;
  }

  /**
   * Convenience method for building up SolrInputFields with default boost
   */
  protected final SolrInputField f(String name, Object... values) {
    return field(name, 1.0F, values);
  }
  
  /**
   * Convenience method for building up SolrInputDocuments
   */
  protected final SolrInputDocument doc(SolrInputField... fields) {
    SolrInputDocument d = new SolrInputDocument();
    for (SolrInputField f : fields) {
      d.put(f.getName(), f);
    }
    return d;
  }
  
  /**
   * Runs a document through the specified chain, and returns the final
   * document used when the chain is completed (NOTE: some chains may
   * modify the document in place
   */
  protected SolrInputDocument processAdd(final String chain,
                                         final SolrInputDocument docIn)
    throws IOException {

    return processAdd(chain, new ModifiableSolrParams(), docIn);
  }

  /**
   * Runs a document through the specified chain, and returns the final
   * document used when the chain is completed (NOTE: some chains may
   * modify the document in place
   */
  protected SolrInputDocument processAdd(final String chain,
                                         final SolrParams requestParams,
                                         final SolrInputDocument docIn)
    throws IOException {

    SolrCore core = h.getCore();
    UpdateRequestProcessorChain pc = core.getUpdateProcessingChain(chain);
    assertNotNull("No Chain named: " + chain, pc);

    SolrQueryResponse rsp = new SolrQueryResponse();

    SolrQueryRequest req = new LocalSolrQueryRequest(core, requestParams);
    try {
      AddUpdateCommand cmd = new AddUpdateCommand(req);
      cmd.solrDoc = docIn;

      UpdateRequestProcessor processor = pc.createProcessor(req, rsp);
      processor.processAdd(cmd);

      return cmd.solrDoc;
    } finally {
      req.close();
    }
  }  

}