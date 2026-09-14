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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.resys.metis.search.api.ImmutableMetisSearchQueryConfig;
import io.resys.metis.search.api.ImmutableMetisSearchResult;
import io.resys.metis.search.api.MetisSearchResult;
import io.resys.metis.search.spi.query.FtsSearchService;
import io.resys.metis.search.spi.query.HybridSearchService;
import io.resys.metis.search.spi.query.VectorSearchService;

public class HybridSearchServiceTest {

  @Test
  void embeddingFailureWithEmptyPrimaryReturnsTrigramHits() {
    final var fts = Mockito.mock(FtsSearchService.class);
    Mockito.when(fts.primaryHits(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(List.of());
    Mockito.when(fts.rankedResults(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(List.of(ftsHit("wf-library", 0.4)));

    final var vector = Mockito.mock(VectorSearchService.class);
    Mockito.when(vector.rankedResults(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenThrow(new IllegalStateException("another query is already embedding"));

    final var results = hybrid(fts, vector).rankedResults("kirjsto", "fi", 8);
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals("wf-library", results.get(0).getWorkflowId());
  }

  @Test
  void weakVectorWithEmptyPrimaryReturnsNothing() {
    final var fts = Mockito.mock(FtsSearchService.class);
    Mockito.when(fts.primaryHits(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(List.of());
    Mockito.when(fts.rankedResults(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(List.of(ftsHit("wf-library", 0.4)));

    final var vector = Mockito.mock(VectorSearchService.class);
    Mockito.when(vector.rankedResults(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(List.of(vectorHit("wf-parking", 0.38)));

    final var results = hybrid(fts, vector).rankedResults("qwerty", "fi", 8);
    Assertions.assertTrue(results.isEmpty());
  }

  @Test
  void embeddingFailureWithPrimaryHitsReturnsKeywordRanking() {
    final var fts = Mockito.mock(FtsSearchService.class);
    Mockito.when(fts.primaryHits(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(List.of(ftsHit("wf-library", 0.9), ftsHit("wf-feedback", 0.2)));
    Mockito.when(fts.rankedResults(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenReturn(List.of(ftsHit("wf-library", 0.9), ftsHit("wf-feedback", 0.2)));

    final var vector = Mockito.mock(VectorSearchService.class);
    Mockito.when(vector.rankedResults(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
        .thenThrow(new IllegalStateException("embedding timed out"));

    final var results = hybrid(fts, vector).rankedResults("kirjasto", "fi", 8);
    Assertions.assertEquals(2, results.size());
    Assertions.assertEquals("wf-library", results.get(0).getWorkflowId());
  }

  private static HybridSearchService hybrid(FtsSearchService fts, VectorSearchService vector) {
    return new HybridSearchService(vector, fts, ImmutableMetisSearchQueryConfig.builder().build());
  }

  private static MetisSearchResult ftsHit(String workflowId, double score) {
    return ImmutableMetisSearchResult.builder()
        .workflowId(workflowId)
        .title(workflowId)
        .locale("fi")
        .score(score)
        .ftsScore(score)
        .build();
  }

  private static MetisSearchResult vectorHit(String workflowId, double score) {
    return ImmutableMetisSearchResult.builder()
        .workflowId(workflowId)
        .title(workflowId)
        .locale("fi")
        .score(score)
        .vectorScore(score)
        .build();
  }
}
