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
package com.sindicetech.siren.elasticsearch;

import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.node.info.PluginInfo;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.notNullValue;

@ElasticsearchIntegrationTest.ClusterScope(scope=ElasticsearchIntegrationTest.Scope.SUITE, numDataNodes=2)
public class SirenPluginTest extends SirenTestCase {

  @Test
  public void testPluginLoaded() {
    NodesInfoResponse nodesInfoResponse = client().admin().cluster().prepareNodesInfo().clear().setPlugins(true).get();
    assertTrue(nodesInfoResponse.getNodes().length != 0);
    assertThat(nodesInfoResponse.getNodes()[0].getPlugins().getInfos(), notNullValue());
    assertThat(nodesInfoResponse.getNodes()[0].getPlugins().getInfos().size(), not(0));

    boolean pluginFound = false;

    for (PluginInfo pluginInfo : nodesInfoResponse.getNodes()[0].getPlugins().getInfos()) {
      if (pluginInfo.getName().equals("siren-plugin")) {
        pluginFound = true;
        break;
      }
    }

    assertThat(pluginFound, is(true));
  }

}
