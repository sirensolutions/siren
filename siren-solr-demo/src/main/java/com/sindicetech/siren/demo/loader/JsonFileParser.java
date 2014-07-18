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

package com.sindicetech.siren.demo.loader;

import org.codehaus.jackson.*;
import org.codehaus.jackson.map.MappingJsonFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * class intended to parse json files, not thread safe hasNext() should be call
 * before before next() to check if an object is available
 */
public class JsonFileParser {

  private volatile boolean isFinished = false;
  private final JsonParser jp;

  public JsonFileParser(final InputStream in) throws JsonParseException, IOException {
    jp = createParser(in);
    setParserToFirstObject();
  }

  private void setParserToFirstObject() throws JsonParseException, IOException {
    JsonToken token = jp.nextToken();
    if (token == JsonToken.START_ARRAY) {
      jp.nextToken();
      // either object starts here or it is incorrect JSON
    } else if (token != JsonToken.START_OBJECT) {
      try {
        jp.close();
      } catch (IOException e1) {
        // nothing to fix
      }
      throw new JsonParseException("Incorrect JSON", new JsonLocation(jp, 0, 0, 0));
    }

  }

  private JsonParser createParser(InputStream in) throws JsonParseException, IOException {
    return new MappingJsonFactory().createJsonParser(in);
  }

  public boolean hasNext() {
    return !isFinished;
  }

  /**
   * returns next object
   *
   * @return next object
   * @throws JsonProcessingException
   * @throws IOException
   */
  public JsonNode next() throws JsonProcessingException, IOException {
    JsonNode node = jp.readValueAsTree();
    JsonToken token = jp.nextToken();
    if (token == null || token == JsonToken.END_ARRAY) {
      isFinished = true;
    }
    return node;
  }

}
