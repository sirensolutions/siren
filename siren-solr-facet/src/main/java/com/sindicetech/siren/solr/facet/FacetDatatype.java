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

import java.net.URI;
import java.net.URISyntaxException;

import com.sindicetech.siren.util.XSDDatatype;

/**
 * <p>An auxiliary enum to help to translate between {@link XSDDatatype} and
 * the datatypes and datatype names used in Siren facet field names.
 * @experimental Can change in the next release.
 */
public enum FacetDatatype {
  NULL(XSDDatatype.XSD_STRING), // string on purpose
  BOOLEAN(XSDDatatype.XSD_BOOLEAN), DOUBLE(XSDDatatype.XSD_DOUBLE), LONG(XSDDatatype.XSD_LONG), STRING(
      XSDDatatype.XSD_STRING);

  public String xsdDatatype;
  public String xsdDatatypeLocalPart;

  FacetDatatype(String xsdDatatype) {
    this.xsdDatatype = xsdDatatype;
    try {
      this.xsdDatatypeLocalPart = new URI(xsdDatatype).getFragment();
    } catch (URISyntaxException e) {
      throw new RuntimeException(String.format(
          "There is a bug, value %s of %s is not a valid URI: %s", xsdDatatype,
          XSDDatatype.class.getCanonicalName(), e.getMessage()), e);
    }
  }
}

