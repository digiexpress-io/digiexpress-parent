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

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.embedding.EmbeddingModel;

import io.resys.metis.spi.ai.EmbeddingService;

public class EmbeddingServiceTest {

  private static final Duration TIMEOUT = Duration.ofSeconds(5);

  @Test
  void twoDifferentQueriesEmbedWhenPermitsAreTwo() throws Exception {
    final var gate = new Gate(2);
    final var model = blockingModel(gate);
    final var embeddings = new EmbeddingService(model, TIMEOUT, 2);

    final var pool = Executors.newFixedThreadPool(2);
    try {
      final var first = pool.submit(() -> embeddings.embedQuery("kirjasto", TIMEOUT));
      final var second = pool.submit(() -> embeddings.embedQuery("hammashoito", TIMEOUT));
      Assertions.assertTrue(gate.started.await(5, TimeUnit.SECONDS));
      gate.release.countDown();
      Assertions.assertArrayEquals(vector("kirjasto"), first.get(5, TimeUnit.SECONDS));
      Assertions.assertArrayEquals(vector("hammashoito"), second.get(5, TimeUnit.SECONDS));
      Assertions.assertEquals(2, gate.calls.get());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void aSecondDifferentQueryFailsFastWhenPermitsAreOne() throws Exception {
    final var gate = new Gate(1);
    final var model = blockingModel(gate);
    final var embeddings = new EmbeddingService(model, TIMEOUT, 1);

    final var pool = Executors.newFixedThreadPool(2);
    try {
      final var first = pool.submit(() -> embeddings.embedQuery("kirjasto", TIMEOUT));
      Assertions.assertTrue(gate.started.await(5, TimeUnit.SECONDS));
      final var second = pool.submit(() -> embeddings.embedQuery("hammashoito", TIMEOUT));
      final var error = Assertions.assertThrows(ExecutionException.class, () -> second.get(5, TimeUnit.SECONDS));
      Assertions.assertTrue(error.getCause().getMessage().contains(EmbeddingService.QUERY_EMBED_BUSY));
      gate.release.countDown();
      Assertions.assertArrayEquals(vector("kirjasto"), first.get(5, TimeUnit.SECONDS));
      Assertions.assertEquals(1, gate.calls.get());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void theSameQueryInFlightSharesOneEmbed() throws Exception {
    final var gate = new Gate(1);
    final var model = blockingModel(gate);
    final var embeddings = new EmbeddingService(model, TIMEOUT, 1);

    final var pool = Executors.newFixedThreadPool(2);
    try {
      final var first = pool.submit(() -> embeddings.embedQuery("kirjasto", TIMEOUT));
      Assertions.assertTrue(gate.started.await(5, TimeUnit.SECONDS));
      final var second = pool.submit(() -> embeddings.embedQuery("kirjasto", TIMEOUT));
      gate.release.countDown();
      Assertions.assertArrayEquals(vector("kirjasto"), first.get(5, TimeUnit.SECONDS));
      Assertions.assertArrayEquals(vector("kirjasto"), second.get(5, TimeUnit.SECONDS));
      Assertions.assertEquals(1, gate.calls.get());
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void aCachedQueryDoesNotEmbedAgain() {
    final var model = Mockito.mock(EmbeddingModel.class);
    Mockito.when(model.embed("kirjasto")).thenReturn(vector("kirjasto"));
    final var embeddings = new EmbeddingService(model, TIMEOUT, 1);

    Assertions.assertArrayEquals(vector("kirjasto"), embeddings.embedQuery("kirjasto", TIMEOUT));
    Assertions.assertArrayEquals(vector("kirjasto"), embeddings.embedQuery("kirjasto", TIMEOUT));
    Mockito.verify(model, Mockito.times(1)).embed("kirjasto");
  }

  private EmbeddingModel blockingModel(Gate gate) {
    final var model = Mockito.mock(EmbeddingModel.class);
    Mockito.when(model.embed(Mockito.anyString())).thenAnswer(invocation -> {
      gate.calls.incrementAndGet();
      gate.started.countDown();
      if (!gate.release.await(5, TimeUnit.SECONDS)) {
        throw new IllegalStateException("release latch timed out");
      }
      return vector(invocation.getArgument(0));
    });
    return model;
  }

  private static float[] vector(String text) {
    return new float[] { text.hashCode() };
  }

  private static final class Gate {
    private final CountDownLatch started;
    private final CountDownLatch release = new CountDownLatch(1);
    private final AtomicInteger calls = new AtomicInteger();

    private Gate(int startedCount) {
      this.started = new CountDownLatch(startedCount);
    }
  }
}
