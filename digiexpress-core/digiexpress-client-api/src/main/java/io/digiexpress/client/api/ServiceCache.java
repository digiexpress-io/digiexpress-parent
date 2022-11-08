package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.Optional;

import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceEnvir.Program;

public interface ServiceCache {

  ServiceCache withName(String name);
  void flush(String id);

  CacheEntry save(Program<?> src);
  <T> Optional<Program<T>> get(String id);
  

  interface CacheEntry extends Serializable {
    String getId();
    ConfigType getType();
    <T> Program<T> getProgram();
  }
}
