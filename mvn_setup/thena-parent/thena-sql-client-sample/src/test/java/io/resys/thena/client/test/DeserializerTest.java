package io.resys.thena.client.test;

/*-
 * #%L
 * thena-sql-client-sample
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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.client.sample.ImmutablePersistenceUnit;
import io.resys.thena.client.sample.ImmutableWorld;
import io.resys.thena.client.sample.Batch2DbQuery.World;
import io.resys.thena.client.sample.entities.BatchConsumer;
import io.resys.thena.client.sample.entities.ImmutableBatch;
import io.resys.thena.client.sample.entities.ImmutableBatchConsumer;
import io.vertx.core.json.JsonObject;

public class DeserializerTest {


  @Test
  public void testFromString() throws StreamReadException, DatabindException, IOException {
    final var world = ImmutableWorld.builder()
        .putBatch("xxx", ImmutableBatch.builder()
            .appId("appId")
            .batchName("batchName")
            .id("id")
            .build())
        .build();

    final var encoded = JsonObject.mapFrom(world).encode();
    final var decoded = new ObjectMapper().readValue(encoded.getBytes(), World.class);

    Assertions.assertEquals(1, decoded.getBatch().size());

  }

  private static BatchConsumer batchConsumer(String id) {
    return ImmutableBatchConsumer.builder()
        .id(id)
        .appId("appId")
        .consumerName("consumerName-" + id)
        .batchName("batchName")
        .comment("comment")
        .createdAt(OffsetDateTime.now())
        .createdBy("createdBy")
        .qualifiedJavaName("io.resys.Test")
        .build();
  }

  @Test
  public void testSplit_singleChunkWhenUnderMaxSize() {
    final var world = ImmutableWorld.builder()
        .putBatchConsumer("1", batchConsumer("1"))
        .putBatchConsumer("2", batchConsumer("2"))
        .build();

    final var unit = ImmutablePersistenceUnit.builder().build().addAllToInserts(world).build();
    final var chunks = unit.split(10);

    Assertions.assertEquals(1, chunks.size());
    Assertions.assertEquals(2, chunks.get(0).getBatchConsumersInserts().size());
  }

  @Test
  public void testSplit_ordersDeletesBeforeInsertsAcrossChunks() {
    final var deletes = List.of(batchConsumer("d1"), batchConsumer("d2"), batchConsumer("d3"));
    final var inserts = List.of(batchConsumer("i1"), batchConsumer("i2"), batchConsumer("i3"), batchConsumer("i4"));

    final var unit = ImmutablePersistenceUnit.builder()
        .addAllBatchConsumersDeletes(deletes)
        .addAllBatchConsumersInserts(inserts)
        .build();

    // 3 deletes + 4 inserts = 7 entries, maxSize=3 -> ceil(7/3) = 3 chunks
    final var chunks = unit.split(3);
    Assertions.assertEquals(3, chunks.size());

    // every chunk stays within the raw-index cap
    for (final var chunk : chunks) {
      final var chunkSize = chunk.getBatchConsumersDeletes().size()
          + chunk.getBatchConsumersInserts().size()
          + chunk.getBatchConsumersUpdates().size();
      Assertions.assertTrue(chunkSize <= 3, "chunk exceeded maxSize: " + chunkSize);
    }

    // reassembling deletes/inserts across chunks (in order) reproduces the original lists
    final var reassembledDeletes = new ArrayList<BatchConsumer>();
    final var reassembledInserts = new ArrayList<BatchConsumer>();
    for (final var chunk : chunks) {
      reassembledDeletes.addAll(chunk.getBatchConsumersDeletes());
      reassembledInserts.addAll(chunk.getBatchConsumersInserts());
    }
    Assertions.assertEquals(deletes, reassembledDeletes);
    Assertions.assertEquals(inserts, reassembledInserts);

    // once a later chunk starts carrying inserts, no subsequent chunk may carry deletes again
    var sawInserts = false;
    for (final var chunk : chunks) {
      if (!chunk.getBatchConsumersInserts().isEmpty()) {
        sawInserts = true;
      }
      if (sawInserts) {
        Assertions.assertTrue(chunk.getBatchConsumersDeletes().isEmpty(),
            "delete-phase entry found after insert-phase entries had already started");
      }
    }

    // commit metadata should only land on the final chunk
    for (int i = 0; i < chunks.size() - 1; i++) {
      Assertions.assertTrue(chunks.get(i).getCommitMessages().isEmpty());
    }
  }

  @Test
  public void testSplit_exactMultipleOfMaxSizeProducesNoEmptyTrailingChunk() {
    final var inserts = List.of(batchConsumer("i1"), batchConsumer("i2"), batchConsumer("i3"), batchConsumer("i4"));

    final var unit = ImmutablePersistenceUnit.builder()
        .addAllBatchConsumersInserts(inserts)
        .addCommitMessage("did it")
        .build();

    final var chunks = unit.split(2);

    Assertions.assertEquals(2, chunks.size());
    Assertions.assertEquals(List.of(inserts.get(0), inserts.get(1)), chunks.get(0).getBatchConsumersInserts());
    Assertions.assertEquals(List.of(inserts.get(2), inserts.get(3)), chunks.get(1).getBatchConsumersInserts());
    Assertions.assertEquals(List.of("did it"), chunks.get(1).getCommitMessages());
  }
}
