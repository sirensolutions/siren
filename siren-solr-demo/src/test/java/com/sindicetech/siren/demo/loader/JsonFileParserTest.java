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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.junit.Test;

import com.sindicetech.siren.demo.loader.JsonFileParser;

public class JsonFileParserTest {
	private static final String JSON_ARRAY = "[{\"simple\":\"object\"},{\"nested\":{\"a\":\"b\",\"c\":\"d\"},\"ff\":44}]";
	private static final String JSON_ONE = "{\"simple\":\"object\", \"nested\":{\"a\":\"b\",\"c\":\"d\"}}";

	@Test
	public void testOne() throws JsonParseException, IOException {
		JsonFileParser jp = new JsonFileParser(new ByteArrayInputStream(JSON_ONE.getBytes()));
		assertTrue(jp.hasNext());
		JsonNode node =  jp.next();
		assertEquals("object", node.get("simple").asText());
		assertEquals("b",node.get("nested").get("a").asText());
		assertFalse(jp.hasNext());
	}
	@Test
	public void testArray() throws JsonParseException, IOException{
		JsonFileParser jp = new JsonFileParser(new ByteArrayInputStream(JSON_ARRAY.getBytes()));
		assertTrue(jp.hasNext());
		JsonNode node =  jp.next();
		assertEquals("object", node.get("simple").asText());
		assertTrue(jp.hasNext());
		node =  jp.next();
		assertEquals("b",node.get("nested").get("a").asText());
		assertEquals(44 ,node.get("ff").asInt());
		assertFalse(jp.hasNext());
	}
	
}
