package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.Optional;

import io.digiexpress.client.api.ClientEntity.ConfigType;
import io.digiexpress.client.api.AssetEnvir.ServiceProgram;

public interface ClientCache {

  ClientCache withName(String name);
  void flush(String id);

  CacheEntry save(ServiceProgram src);
  <T> Optional<ServiceProgram> get(String id);
  

  interface CacheEntry extends Serializable {
    String getId();
    ConfigType getType();
    <T extends ServiceProgram> T getProgram();
  }
}
