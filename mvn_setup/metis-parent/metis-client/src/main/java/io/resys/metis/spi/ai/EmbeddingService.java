package io.resys.metis.spi.ai;

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

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.ai.embedding.EmbeddingModel;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class EmbeddingService {

  public static final String QUERY_EMBED_BUSY = "another query is already embedding";

  private static final Duration DEFAULT_QUERY_TIMEOUT = Duration.ofSeconds(2);

  private final EmbeddingModel embeddingModel;
  private final Cache<String, float[]> queryCache;
  private final Duration defaultQueryTimeout;
  private final Semaphore queryEmbed;
  private final ConcurrentHashMap<String, CompletableFuture<float[]>> inFlight = new ConcurrentHashMap<>();

  public EmbeddingService(EmbeddingModel embeddingModel) {
    this(embeddingModel, DEFAULT_QUERY_TIMEOUT, 1);
  }

  public EmbeddingService(EmbeddingModel embeddingModel, Duration defaultQueryTimeout) {
    this(embeddingModel, defaultQueryTimeout, 1);
  }

  public EmbeddingService(EmbeddingModel embeddingModel, Duration defaultQueryTimeout, int queryEmbedConcurrency) {
    this.embeddingModel = embeddingModel;
    this.defaultQueryTimeout = sane(defaultQueryTimeout);
    this.queryCache = Caffeine.newBuilder().maximumSize(1_000).build();
    this.queryEmbed = new Semaphore(Math.max(1, queryEmbedConcurrency));
  }

  public float[] embed(String text) {
    return embeddingModel.embed(text);
  }

  public float[] embedQuery(String query) {
    return embedQuery(query, defaultQueryTimeout);
  }

  public float[] embedQuery(String query, Duration timeout) {
    final var cached = queryCache.getIfPresent(query);
    if (cached != null) {
      return cached;
    }
    final var running = inFlight.get(query);
    if (running != null) {
      return join(running, timeout);
    }
    if (!queryEmbed.tryAcquire()) {
      final var coalesced = inFlight.get(query);
      if (coalesced != null) {
        return join(coalesced, timeout);
      }
      throw new IllegalStateException(QUERY_EMBED_BUSY);
    }
    final var alreadyDone = queryCache.getIfPresent(query);
    if (alreadyDone != null) {
      queryEmbed.release();
      return alreadyDone;
    }
    final var alreadyRunning = inFlight.get(query);
    if (alreadyRunning != null) {
      queryEmbed.release();
      return join(alreadyRunning, timeout);
    }

    final var pending = new CompletableFuture<float[]>();
    final var winner = inFlight.putIfAbsent(query, pending);
    if (winner != null) {
      queryEmbed.release();
      return join(winner, timeout);
    }
    CompletableFuture.supplyAsync(() -> embeddingModel.embed(query)).whenComplete((vector, error) -> {
      try {
        if (vector != null) {
          queryCache.put(query, vector);
          pending.complete(vector);
        } else {
          pending.completeExceptionally(error != null ? error : new IllegalStateException("Query embedding failed"));
        }
      } finally {
        inFlight.remove(query, pending);
        queryEmbed.release();
      }
    });
    return join(pending, timeout);
  }

  private float[] join(CompletableFuture<float[]> pending, Duration timeout) {
    try {
      return pending.orTimeout(sane(timeout).toMillis(), TimeUnit.MILLISECONDS).join();
    } catch (CompletionException error) {
      throw new IllegalStateException("Query embedding failed: " + error.toString(), error);
    }
  }

  private static Duration sane(Duration timeout) {
    return timeout == null || timeout.isZero() || timeout.isNegative()
        ? DEFAULT_QUERY_TIMEOUT
        : timeout;
  }
}
