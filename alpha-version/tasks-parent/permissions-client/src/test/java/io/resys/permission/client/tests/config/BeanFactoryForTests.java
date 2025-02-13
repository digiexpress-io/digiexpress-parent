package io.resys.permission.client.tests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.thena.jackson.VertexExtModule;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

@Dependent
public class BeanFactoryForTests {
  
  @Produces
  public ObjectMapper objectMapper() {
    final var modules = new com.fasterxml.jackson.databind.Module[] {
      new JavaTimeModule(), 
      new Jdk8Module(), 
      new GuavaModule(),
      new VertxModule(),
      new VertexExtModule()
      };
    DatabindCodec.mapper().registerModules(modules);
    DatabindCodec.prettyMapper().registerModules(modules);
    
    return DatabindCodec.mapper(); 
  }
}
