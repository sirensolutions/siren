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
package com.sindicetech.siren.solr.facet;

import java.io.IOException;

import org.apache.solr.update.processor.UpdateRequestProcessor;


/**
 * <p>Thrown when a problem occurs during facet extraction or processing.
 * 
 * <p>Subclass of {@link IOException} because only that exception can be thrown from
 * {@link UpdateRequestProcessor#processAdd(org.apache.solr.update.AddUpdateCommand)}.
 * 
 * @experimental Can change in the next release.
 */
public class FacetException extends IOException {
  public FacetException(String string, Throwable e) {
    super(string, e);
  }

  private static final long serialVersionUID = 1L;
}
