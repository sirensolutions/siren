/**
 * Copyright (c) 2014, Sindice Limited. All Rights Reserved.
 *
 * This file is part of the SIREn project.
 *
 * SIREn is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * SIREn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sindicetech.siren.index.codecs;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.lucene46.Lucene46Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sindicetech.siren.index.codecs.siren10.Siren10AForPostingsFormat;
import com.sindicetech.siren.index.codecs.siren10.Siren10VIntPostingsFormat;
import com.sindicetech.siren.util.SirenTestCase;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class RandomSirenCodec extends Lucene46Codec {

  final Random random;

  private final HashSet<String> sirenFields = new HashSet<String>();

  PostingsFormat defaultTestFormat;

  private static final int[] BLOCK_SIZES = new int[] {1, 2, 16, 32, 64, 128, 256, 512, 1024};

  public enum PostingsFormatType {
    RANDOM, SIREN_10
  }

  protected static final Logger logger = LoggerFactory.getLogger(RandomSirenCodec.class);

  public RandomSirenCodec(final Random random) {
    this(random, PostingsFormatType.RANDOM);
  }

  public RandomSirenCodec(final Random random, final PostingsFormatType formatType) {
    this.addSirenFields(SirenTestCase.DEFAULT_TEST_FIELD);
    this.random = random;
    this.defaultTestFormat = RandomSirenCodec.getPostingsFormat(random, formatType);
    Codec.setDefault(this);
  }

  public RandomSirenCodec(final Random random, final PostingsFormat format) {
    this.addSirenFields(SirenTestCase.DEFAULT_TEST_FIELD);
    this.random = random;
    this.defaultTestFormat = format;
    Codec.setDefault(this);
  }

  public void addSirenFields(final String... fields) {
    sirenFields.addAll(Arrays.asList(fields));
  }

  @Override
  public PostingsFormat getPostingsFormatForField(final String field) {
    if (sirenFields.contains(field)) {
      return defaultTestFormat;
    }
    else {
      return super.getPostingsFormatForField(field);
    }
  }

  @Override
  public String toString() {
    return "RandomSirenCodec";
  }

  private static PostingsFormat getPostingsFormat(final Random random, final PostingsFormatType formatType) {
    switch (formatType) {
      case RANDOM:
        return newRandomPostingsFormat(random);

      case SIREN_10:
        return newSiren10PostingsFormat(random);

      default:
        throw new InvalidParameterException();
    }
  }

  private static PostingsFormat newSiren10PostingsFormat(final Random random) {
    final int blockSize = newRandomBlockSize(random);
    final int i = random.nextInt(2);
    switch (i) {

      case 0:
        return new Siren10VIntPostingsFormat(blockSize);

      case 1:
        return new Siren10AForPostingsFormat(blockSize);

      default:
        throw new InvalidParameterException();
    }
  }

  private static PostingsFormat newRandomPostingsFormat(final Random random) {
    final int i = random.nextInt(1);
    switch (i) {

      case 0:
        return newSiren10PostingsFormat(random);

      default:
        throw new InvalidParameterException();
    }
  }

  private static int newRandomBlockSize(final Random random) {
    return BLOCK_SIZES[random.nextInt(BLOCK_SIZES.length)];
  }

}
