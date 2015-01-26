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

import org.apache.lucene.codecs.PostingsFormat;
import org.elasticsearch.index.codec.postingsformat.PostingsFormatProvider;

import com.sindicetech.siren.index.codecs.siren10.Siren10AForPostingsFormat;

/**
 *  Boilerplate for registering SIREn's postings format.
 *
 */
public class SirenPostingsProvider implements PostingsFormatProvider {

  private PostingsFormat postingsFormat = new Siren10AForPostingsFormat();

  @Override
  public PostingsFormat get() {
    return postingsFormat;
  }

  @Override
  public String name() {
    return Siren10AForPostingsFormat.NAME;
  }

}
