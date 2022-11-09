package io.digiexpress.client.spi.envir;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.spi.support.EnvirException;

public class ServiceEnvirImpl implements ServiceEnvir {

  private final ServiceClientConfig config;
  private final Map<String, ServiceProgramSource> hash_to_source;
  private final Map<String, ServiceProgramSource> id_to_source;
  private final Map<LocalDateTime, List<String>> active_to_hash;
  
  public ServiceEnvirImpl(
      ServiceClientConfig config, 
      Map<String, ServiceProgramSource> hash_to_source,
      Map<LocalDateTime, List<String>> active_to_hash) {
    super();
    this.config = config;
    this.hash_to_source = Collections.unmodifiableMap(hash_to_source);
    this.active_to_hash = Collections.unmodifiableMap(active_to_hash);
    this.id_to_source = Collections.unmodifiableMap(
        hash_to_source.values().stream()
        .collect(Collectors.toMap(e -> e.getId(), e -> e)));
  }
  @Override
  public Map<String, ServiceProgramSource> getSources() {
    return id_to_source;
  }

  @Override
  public ServiceProgram getByHash(String hash) {
    final var src = this.hash_to_source.get(hash);
    if(src == null) {
      throw EnvirException.notFoundHash(hash, () -> String.join(",", this.hash_to_source.values().stream().map(e -> e.getHash()).collect(Collectors.toList())));
    }
    return config.getCache().get(src.getId()).orElseGet(() -> create(src));
  }
  @Override
  public ServiceProgram getById(String objectId) {
    final var src = this.id_to_source.get(objectId);
    if(src == null) {
      throw EnvirException.notFoundId(objectId, () -> String.join(",", this.hash_to_source.values().stream().map(e -> e.getId()).collect(Collectors.toList())));
    }
    return config.getCache().get(src.getId()).orElseGet(() -> create(src));
  }
  @Override
  public ServiceProgramService getDef(LocalDateTime targetDate) {
    // TODO Auto-generated method stub
    return null;
  }
  
  private ServiceProgram create(ServiceProgramSource source) {
    final ServiceProgram program;
    switch (source.getType()) {
    case STENCIL: program = new ServiceProgramStencilImpl(source); break;
    case SERVICE: program = new ServiceProgramServiceImpl(source); break;
    case DIALOB: program = new ServiceProgramDialobImpl(source); break;
    case HDES: program = new ServiceProgramHdesImpl(source); break;
    default: throw EnvirException.notSupportedSource(source, () -> "");
    }
    config.getCache().save(program);
    return program;
  }
}
