/*
 * Copyright 2019, the zchunk-java contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.zchunk.compressedint;

import static java.math.BigInteger.ONE;

import java.math.BigInteger;

public final class CompressedIntUtil {

  public static final int COMPRESSED_INT_LAST_BYTE_FLAG = 0b10000000;

  /**
   * The maximum value is 0xffffffffffffffff (which equals signed -1).
   */
  public static final long MAX_VALUE = 0xffffffffffffffffL;

  /**
   * This bitmask is used to read in an unsigned long as positive value.
   */
  public static final BigInteger UNSIGNED_LONG_MASK = ONE.shiftLeft(Long.SIZE).subtract(ONE);

  /**
   * Since an (unsigned) long (64 bits / 8 byte) is the maximum length we allow at this point,
   * the max length of a compressed integer is 10 bytes.
   * 10 bytes are needed to encode -1L (or 0xffffffffffffffff) as compressed int.
   */
  public static final int MAX_COMPRESSED_INT_LENGTH = (Long.SIZE / 7) + 1;

  private CompressedIntUtil() {
    // private util
  }

  /**
   * Compress a long to a compressed int. The long will be interpreted as positive, which may be confusing.
   *
   * <p>E.g. All positive numbers will come out as expected. All negative numbers will exceed that value.
   * The highest number you can convert is -{@code -1L}, which is interpreted as {@code 18446744073709551615 (2^64 - 1)}.</p>
   *
   * @return a byte array which will never exceed the length of MAX_COMPRESSED_INT_LENGTH and will never have leading all-zero bytes.
   */
  public static byte[] compress(final long unsignedIntValue) {
    long modValue = unsignedIntValue;
    final byte[] tmp = new byte[MAX_COMPRESSED_INT_LENGTH];
    int byteIndex = 0;

    while (true) {
      // get the rightmost 7 bits
      final byte currentByte = (byte) (modValue & 0b01111111);
      // unsigned(!) shift by seven bits.
      modValue >>>= 7L;
      tmp[byteIndex] = currentByte;

      if (modValue == 0L) {
        // this is the last byte we encoded. Make sure we set the last-byte-flag.
        tmp[byteIndex] |= COMPRESSED_INT_LAST_BYTE_FLAG;
        break;
      }

      byteIndex++;
    }

    final byte[] out = new byte[byteIndex + 1];
    System.arraycopy(tmp, 0, out, 0, byteIndex + 1);

    return out;
  }

  /**
   * Decompresses an unsignedint's bytevalue.
   *
   * <p>The result is stored as {@link BigInteger}, because values might exceed overflow {@link Long#MAX_VALUE} and end up as
   * negative. With BigInteger, no such information is lost.</p>
   *
   * @param compressedUnsignedInt
   *     the bytearray holding a compressed int to decompress.
   * @return a {@link BigInteger} object holding the uncompressed unsigned int value.
   * @throws IllegalArgumentException
   *     if parameter {@code compressedUnsignedInt::length} exceeds the value of {@link #MAX_COMPRESSED_INT_LENGTH}.
   */
  public static BigInteger decompress(final byte[] compressedUnsignedInt) {
    if (compressedUnsignedInt.length > MAX_COMPRESSED_INT_LENGTH) {
      throw new IllegalArgumentException("Compressed int too big!");
    }

    long result = 0L;
    int shift = 0;

    for (final byte b : compressedUnsignedInt) {
      final byte leadingZero = (byte) (b & ~COMPRESSED_INT_LAST_BYTE_FLAG);

      result |= leadingZero << shift;
      shift += 7;
    }

    return BigInteger.valueOf(result).and(UNSIGNED_LONG_MASK);
  }

}
