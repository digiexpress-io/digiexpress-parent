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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.client.sample.ImmutableWorld;
import io.resys.thena.client.sample.Batch2DbQuery.World;
import io.resys.thena.client.sample.entities.ImmutableBatch;
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
}
