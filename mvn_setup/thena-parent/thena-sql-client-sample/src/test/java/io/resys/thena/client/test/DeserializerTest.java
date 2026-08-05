package io.resys.thena.client.test;

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
