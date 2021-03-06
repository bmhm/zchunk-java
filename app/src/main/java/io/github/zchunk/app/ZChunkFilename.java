/*
 *  Copyright 2018 The zchunk-java contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.zchunk.app;

import java.io.File;

public final class ZChunkFilename {

  private ZChunkFilename() {
    // util class
  }

  /**
   * Returns a new file for the current directory replacing .zck with .zdict.
   *
   * @param zchunkFile
   *     the input file.
   * @return the file name without directory.
   */
  public static File getDictFile(final File zchunkFile) {
    if (!zchunkFile.getName().endsWith(".zck")) {
      throw new UnsupportedOperationException("Cannot acquire target file name");
    }

    final String newName = zchunkFile.getName().replaceAll("^(.*)\\.zck$", "$1.zdict");

    return new File(newName);
  }

  public static File getNormalFile(final File zchunkFile) {
    if (!zchunkFile.getName().endsWith(".zck")) {
      throw new UnsupportedOperationException("Cannot acquire target file name");
    }

    final String newName = zchunkFile.getName().replaceAll("^(.*)\\.zck$", "$1");

    return new File(newName);
  }
}
