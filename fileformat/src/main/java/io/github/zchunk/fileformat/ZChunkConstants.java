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

package io.github.zchunk.fileformat;

import static java.security.Security.getProviders;

import io.github.zchunk.compressedint.CompressedIntUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ZChunkConstants {

  private ZChunkConstants() {
    // util
  }

  public static class Header {

    public static final byte[] FILE_MAGIC = new byte[]{'\0', 'Z', 'C', 'K', '1'};

    private static final int MAX_CHECKSUM_SIZE = getLargestHashSize();
    public static int MAX_LEAD_SIZE = FILE_MAGIC.length
        // checksum type (ci)
        + CompressedIntUtil.MAX_COMPRESSED_INT_LENGTH
        // header size (ci)
        + CompressedIntUtil.MAX_COMPRESSED_INT_LENGTH
        + MAX_CHECKSUM_SIZE;

    private static int getLargestHashSize() {
      final String digestClassName = MessageDigest.class.getSimpleName();
      final String aliasPrefix = "Alg.Alias." + digestClassName + ".";

      final Stream<@Nullable MessageDigest> messageDigestStream = Arrays.stream(getProviders())
          .flatMap(prov -> {
            final Set<String> algorithms = new HashSet<>(0);

            prov.getServices().stream()
                .filter(s -> digestClassName.equalsIgnoreCase(s.getType()))
                .map(Service::getAlgorithm)
                .collect(Collectors.toCollection(() -> algorithms));

            prov.keySet().stream()
                .map(Object::toString)
                .filter(k -> k.startsWith(aliasPrefix))
                .map(k -> String.format("\"%s\" -> \"%s\"", k.substring(aliasPrefix.length()), prov.get(k).toString()))
                .collect(Collectors.toCollection(() -> algorithms));

            return algorithms.stream();
          })
          .map(ZChunkConstants::instanceOrNull);

      return toNonNullStream(messageDigestStream)
          .mapToInt(MessageDigest::getDigestLength)
          .max().orElse(512 / 8);
    }

  }

  @SuppressWarnings("nullness")
  private static Stream<@NonNull MessageDigest> toNonNullStream(final Stream<@Nullable MessageDigest> messageDigestStream) {
    return messageDigestStream.filter(Objects::nonNull);
  }

  private static @Nullable MessageDigest instanceOrNull(final String algo) {
    try {
      return MessageDigest.getInstance(algo);
    } catch (final NoSuchAlgorithmException e) {
      return null;
    }
  }
}
