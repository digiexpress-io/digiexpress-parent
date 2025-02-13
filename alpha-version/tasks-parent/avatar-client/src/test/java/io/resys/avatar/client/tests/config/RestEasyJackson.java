package io.resys.avatar.client.tests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.jackson.ObjectMapperCustomizer;
import io.resys.thena.jackson.VertexExtModule;
import io.vertx.core.json.jackson.VertxModule;
import jakarta.inject.Singleton;



@Singleton
public class RestEasyJackson implements ObjectMapperCustomizer {

  @Override
  public int priority() {
      return MINIMUM_PRIORITY; //this is needed in order to ensure that your SimpleModule's Serializer will be applied last and thus override the one coming from the `JavaTimeModule`
  }

  @Override
  public void customize(ObjectMapper mapper) {
    final var modules = new com.fasterxml.jackson.databind.Module[] {
        new JavaTimeModule(), 
        new Jdk8Module(), 
        new GuavaModule(),
        new VertxModule(),
        new VertexExtModule()
      };
      
    
    mapper.registerModules(modules);
    // without this, local dates will be serialized as int array
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
