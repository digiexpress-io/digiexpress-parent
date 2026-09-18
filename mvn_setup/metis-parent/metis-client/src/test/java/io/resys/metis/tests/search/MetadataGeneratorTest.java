package io.resys.metis.tests.search;

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
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.resys.metis.search.spi.index.GeneratedMetadata;
import io.resys.metis.search.spi.index.MetadataGenerator;
import io.resys.metis.search.spi.index.SiteSearchDocument;
import io.resys.metis.spi.ai.StructuredChatService;

public class MetadataGeneratorTest {

  @Test
  void chatFailureFallsBackToTheWorkflowDescription() {
    final var chat = Mockito.mock(StructuredChatService.class);
    Mockito.when(chat.generate(Mockito.anyString(), Mockito.eq(GeneratedMetadata.class), Mockito.anyString()))
        .thenReturn(Optional.empty());

    final var metadata = new MetadataGenerator(chat).generate(document());
    Assertions.assertEquals("Varaa kirja kirjastosta verkossa.", metadata);
  }

  @Test
  void aBlankLlmDescriptionKeepsTheWorkflowDescription() {
    final var chat = Mockito.mock(StructuredChatService.class);
    Mockito.when(chat.generate(Mockito.anyString(), Mockito.eq(GeneratedMetadata.class), Mockito.anyString()))
        .thenReturn(Optional.of(new GeneratedMetadata("  ", List.of("kirja"), List.of("lainaa kirja"))));

    final var metadata = new MetadataGenerator(chat).generate(document());
    Assertions.assertTrue(metadata.startsWith("Varaa kirja kirjastosta verkossa."));
    Assertions.assertTrue(metadata.contains("Synonyms: kirja"));
    Assertions.assertTrue(metadata.contains("Related: lainaa kirja"));
  }

  private static SiteSearchDocument document() {
    return new SiteSearchDocument(
        "wf-library",
        "topic-library",
        "fi",
        "Varaa kirja kirjastosta",
        "Kirjasto",
        "Varaa kirja kirjastosta verkossa.",
        "search",
        "extra",
        "llm",
        "hash");
  }
}
