package io.resys.thena.tasks.dev.app.mig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class MigrationsDefaults {
  public static final ObjectMapper om = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
  public static final String folder = "src/test/resources/migration/"; 
  
  public static TableLog summary(String... cols) {    
    return new TableLog(cols);
  }
  
}
