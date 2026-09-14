package io.resys.metis.search.spi.query;

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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.resys.metis.search.api.ImmutableMetisSearchResult;
import io.resys.metis.search.api.MetisSearchQueryConfig;
import io.resys.metis.search.api.MetisSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HybridSearchService {

  private final VectorSearchService vectorSearchService;
  private final FtsSearchService ftsSearchService;
  private final MetisSearchQueryConfig config;

  public List<MetisSearchResult> rankedResults(String query, String locale, int limit) {
    final var candidateLimit = Math.max(limit * 3, 30);
    final var primaryFts = ftsSearchService.primaryHits(query, locale, candidateLimit);
    final var ftsRows = ftsSearchService.rankedResults(query, locale, candidateLimit);
    final var vector = vectorCall(query, locale, candidateLimit);

    if (vector.failed()) {
      return keywordOnly(ftsRows, limit);
    }

    var vectorRows = vector.rows();
    if (primaryFts.isEmpty()) {
      vectorRows = dropWeakVectorWithoutKeyword(vectorRows);
      if (vectorRows.isEmpty()) {
        return List.of();
      }
    }

    if (vectorRows.isEmpty()) {
      return keywordOnly(primaryFts, limit);
    }

    final var k = config.getRrfK();
    final Map<String, Fusion> fused = new LinkedHashMap<>();
    for (int rank = 0; rank < vectorRows.size(); rank++) {
      final var item = vectorRows.get(rank);
      final var contribution = config.getVectorWeight() / (k + rank + 1);
      fused.computeIfAbsent(item.getWorkflowId(), id -> new Fusion(item))
          .add(contribution, item.getVectorScore(), null);
    }
    for (final var item : primaryFts) {
      fused.putIfAbsent(item.getWorkflowId(), new Fusion(item));
    }
    for (int rank = 0; rank < ftsRows.size(); rank++) {
      final var item = ftsRows.get(rank);
      final var existing = fused.get(item.getWorkflowId());
      if (existing != null) {
        existing.add(config.getFtsWeight() / (k + rank + 1), null, item.getFtsScore());
      }
    }

    return fused.values().stream()
        .map(Fusion::toResult)
        .filter(result -> result.getScore() > 0)
        .sorted(Comparator.comparingDouble(MetisSearchResult::getScore).reversed())
        .limit(limit)
        .toList();
  }

  private List<MetisSearchResult> keywordOnly(List<MetisSearchResult> ftsRows, int limit) {
    final var k = config.getRrfK();
    final Map<String, Fusion> fused = new LinkedHashMap<>();
    for (int rank = 0; rank < ftsRows.size(); rank++) {
      final var item = ftsRows.get(rank);
      fused.computeIfAbsent(item.getWorkflowId(), id -> new Fusion(item))
          .add(config.getFtsWeight() / (k + rank + 1), null, item.getFtsScore());
    }
    return fused.values().stream()
        .map(Fusion::toResult)
        .filter(result -> result.getScore() > 0)
        .sorted(Comparator.comparingDouble(MetisSearchResult::getScore).reversed())
        .limit(limit)
        .toList();
  }

  private VectorOutcome vectorCall(String query, String locale, int limit) {
    try {
      return VectorOutcome.ok(vectorSearchService.rankedResults(query, locale, limit));
    } catch (RuntimeException error) {
      log.warn("Metis vector search skipped, using keyword ranking, because of: {}", error.toString());
      return VectorOutcome.unavailable();
    }
  }

  private List<MetisSearchResult> dropWeakVectorWithoutKeyword(List<MetisSearchResult> vectorRows) {
    if (vectorRows.isEmpty()) {
      return vectorRows;
    }
    final var best = vectorRows.get(0).getVectorScore();
    if (best == null || best < config.getMinVectorScoreWithoutKeyword()) {
      return List.of();
    }
    return vectorRows;
  }

  private record VectorOutcome(List<MetisSearchResult> rows, boolean failed) {
    static VectorOutcome ok(List<MetisSearchResult> rows) {
      return new VectorOutcome(rows, false);
    }

    static VectorOutcome unavailable() {
      return new VectorOutcome(List.of(), true);
    }
  }

  private static final class Fusion {
    private final MetisSearchResult item;
    private double score;
    private Double vectorScore;
    private Double ftsScore;

    private Fusion(MetisSearchResult item) {
      this.item = item;
    }

    private void add(double contribution, Double vectorScore, Double ftsScore) {
      this.score += contribution;
      if (vectorScore != null) {
        this.vectorScore = vectorScore;
      }
      if (ftsScore != null) {
        this.ftsScore = ftsScore;
      }
    }

    private MetisSearchResult toResult() {
      return ImmutableMetisSearchResult.builder()
          .from(item)
          .score(score)
          .vectorScore(vectorScore)
          .ftsScore(ftsScore)
          .build();
    }
  }
}
