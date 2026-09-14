package io.resys.metis.tests;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.resys.metis.api.ImmutableMetisConfig;
import io.resys.metis.api.MetisClient;
import io.resys.metis.api.MetisStatus.CapabilityState;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.spi.MetisClientImpl;

public class MetisClientImplTest {

  @Test
  void searchDisabledWhenTheSearchClientIsMissing() {
    final var metis = client(null);

    Assertions.assertFalse(metis.isSearchEnabled());
    Assertions.assertThrows(IllegalStateException.class, metis::search);

    final var search = capability(metis);
    Assertions.assertEquals("site-search", search.getId());
    Assertions.assertFalse(search.getEnabled());
    Assertions.assertEquals(CapabilityState.DISABLED, search.getState());
  }

  @Test
  void searchIsReadyWhenTheIndexHasDocuments() {
    final var searchClient = Mockito.mock(MetisSearchClient.class);
    Mockito.when(searchClient.isIndexReadyForPortal()).thenReturn(true);
    Mockito.when(searchClient.countIndexedDocuments()).thenReturn(12L);

    final var search = capability(client(searchClient));
    Assertions.assertTrue(search.getEnabled());
    Assertions.assertEquals(CapabilityState.READY, search.getState());
    Assertions.assertEquals("12 documents indexed", search.getDetail());
  }

  @Test
  void searchIsNotReadyUntilAReindexHasCompleted() {
    final var searchClient = Mockito.mock(MetisSearchClient.class);
    Mockito.when(searchClient.isIndexReadyForPortal()).thenReturn(false);
    Mockito.when(searchClient.countIndexedDocuments()).thenReturn(12L);

    final var search = capability(client(searchClient));
    Assertions.assertTrue(search.getEnabled());
    Assertions.assertEquals(CapabilityState.NOT_READY, search.getState());
    Assertions.assertTrue(search.getDetail().contains("keyword search"));
  }

  @Test
  void searchIsNotReadyWhenTheIndexIsEmpty() {
    final var searchClient = Mockito.mock(MetisSearchClient.class);
    Mockito.when(searchClient.isIndexReadyForPortal()).thenReturn(true);
    Mockito.when(searchClient.countIndexedDocuments()).thenReturn(0L);

    final var search = capability(client(searchClient));
    Assertions.assertTrue(search.getEnabled());
    Assertions.assertEquals(CapabilityState.NOT_READY, search.getState());
    Assertions.assertTrue(search.getDetail().contains("empty"));
  }

  @Test
  void searchReportsErrorWhenTheIndexCannotBeRead() {
    final var searchClient = Mockito.mock(MetisSearchClient.class);
    Mockito.when(searchClient.isIndexReadyForPortal()).thenThrow(new IllegalStateException("db is down"));

    final var search = capability(client(searchClient));
    Assertions.assertTrue(search.getEnabled());
    Assertions.assertEquals(CapabilityState.ERROR, search.getState());
    Assertions.assertTrue(search.getDetail().contains("db is down"));
  }

  private static MetisClient client(MetisSearchClient search) {
    return new MetisClientImpl(ImmutableMetisConfig.builder()
        .provider("ollama")
        .chatModelId("llama3.2")
        .embeddingModelId("bge-m3")
        .build(), search);
  }

  private static io.resys.metis.api.MetisStatus.MetisCapabilityStatus capability(MetisClient metis) {
    final var status = metis.getStatus().await().indefinitely();
    Assertions.assertTrue(status.getEnabled());
    Assertions.assertEquals("ollama", status.getProvider());
    Assertions.assertEquals(1, status.getCapabilities().size());
    return status.getCapabilities().get(0);
  }
}
