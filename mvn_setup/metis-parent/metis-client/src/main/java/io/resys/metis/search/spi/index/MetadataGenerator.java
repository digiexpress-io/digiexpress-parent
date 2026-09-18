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

import java.util.List;

import io.resys.metis.spi.ai.StructuredChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class MetadataGenerator {

  private static final String PROMPT = """
      You generate search metadata for a municipality digital services portal.
      Given the service details below, produce citizen-friendly search helpers that
      people might type when looking for this service.
      Write all output in the language for locale code "%s".
      Provide a concise description (under 120 words), 5-10 single-word or short synonyms,
      and 5-10 natural related phrases citizens might search for.

      %s
      """;

  private final StructuredChatService chat;

  public String generate(SiteSearchDocument document) {
    final var prompt = PROMPT.formatted(document.locale(), document.llmContext());
    final var subject = "workflow: " + document.workflowId() + ", locale: " + document.locale();

    return chat.generate(prompt, GeneratedMetadata.class, subject)
        .map(metadata -> format(metadata, document))
        .orElseGet(() -> {
          log.warn("Metis falling back to description-only metadata for workflow: {}, locale: {}",
              document.workflowId(), document.locale());
          return document.description();
        });
  }

  private String format(GeneratedMetadata metadata, SiteSearchDocument document) {
    final var sb = new StringBuilder();
    final var description = metadata.description() != null && !metadata.description().isBlank()
        ? metadata.description()
        : document.description();
    if (description != null && !description.isBlank()) {
      sb.append(description).append('\n');
    }
    appendList(sb, "Synonyms", metadata.synonyms());
    appendList(sb, "Related", metadata.relatedPhrases());
    return sb.toString().trim();
  }

  private void appendList(StringBuilder sb, String label, List<String> items) {
    if (items == null || items.isEmpty()) {
      return;
    }
    sb.append(label).append(": ").append(String.join(", ", items)).append('\n');
  }
}
