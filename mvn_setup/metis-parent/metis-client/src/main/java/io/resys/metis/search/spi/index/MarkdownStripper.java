package io.resys.metis.search.spi.index;

/*-
 * #%L
 * metis-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

public final class MarkdownStripper {

  private MarkdownStripper() {
  }

  public static String strip(String markdown) {
    if (markdown == null || markdown.isBlank()) {
      return "";
    }
    return markdown
        .replaceAll("!\\[[^\\]]*]\\([^)]*\\)", " ")
        .replaceAll("\\[([^\\]]+)]\\([^)]*\\)", "$1")
        .replaceAll("(?m)^#{1,6}\\s+", "")
        .replaceAll("[*_`]", "")
        .replaceAll("(?m)^[-*+]\\s+", "")
        .replaceAll("\\s+", " ")
        .trim();
  }
}
