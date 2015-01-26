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

import java.util.Collection;

/**
 * <p>A simple holder object for type map configuration of SirenFacetProcessorFactory.
 * 
 * @experimental Can change in the next release.
 */
public class TypeMapping {
  public String fieldType;
  public Integer maxFieldSize;
  public Collection<String> valueClasses;
  
  public TypeMapping(String fieldType, Integer maxFieldSize, Collection<String> valueClasses) {
    this.fieldType = fieldType;
    this.maxFieldSize = maxFieldSize;
    this.valueClasses = valueClasses;
  }
}
