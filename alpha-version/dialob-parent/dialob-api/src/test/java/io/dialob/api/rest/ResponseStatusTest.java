package io.dialob.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResponseStatusTest {

  @Test
  public void testJsonSerialization() throws Exception {
    Assertions.assertEquals("{\"ok\":true}", new ObjectMapper().writeValueAsString(ImmutableResponse.builder().ok(true).build()));
    Assertions.assertEquals("{\"ok\":false}", new ObjectMapper().writeValueAsString(ImmutableResponse.builder().ok(false).build()));
    Assertions.assertEquals(ImmutableResponse.builder().ok(true).build(), new ObjectMapper().readValue("{\"ok\":true}", Response.class));
    Assertions.assertEquals(ImmutableResponse.builder().build(), new ObjectMapper().readValue("{}", Response.class));
  }

}
