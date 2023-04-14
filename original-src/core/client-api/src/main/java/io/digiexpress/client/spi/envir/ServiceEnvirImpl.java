package io.digiexpress.client.spi.envir;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.ClientEntity.ConfigType;
import io.digiexpress.client.api.ClientEntity.RefIdValue;
import io.digiexpress.client.api.AssetEnvir;
import io.digiexpress.client.spi.support.EnvirException;
import io.digiexpress.client.spi.support.ServiceAssert;

public class ServiceEnvirImpl implements AssetEnvir {

  private final ClientConfig config;
  private final Map<String, ServiceProgramSource> hash_to_source;
  private final Map<String, ServiceProgramSource> id_to_source;
  private final Map<LocalDateTime, List<String>> active_to_hash;
  
  public ServiceEnvirImpl(
      ClientConfig config, 
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
    ServiceAssert.notNull(hash, () -> "hash must be defined!");
    
    final var src = this.hash_to_source.get(hash);
    if(src == null) {
      throw EnvirException.notFoundHash(hash, () -> String.join(",", this.hash_to_source.values().stream().map(e -> e.getHash()).collect(Collectors.toList())));
    }
    return config.getCache().get(src.getId()).orElseGet(() -> create(src));
  }
  @Override
  public ServiceProgram getById(String objectId) {
    ServiceAssert.notNull(objectId, () -> "objectId must be defined!");
    
    final var src = this.id_to_source.get(objectId);
    if(src == null) {
      throw EnvirException.notFoundId(objectId, () -> String.join(",", this.hash_to_source.values().stream().map(e -> e.getId()).collect(Collectors.toList())));
    }
    return config.getCache().get(src.getId()).orElseGet(() -> create(src));
  }
  @Override
  public ServiceProgramDef getDef(LocalDateTime targetDate) {
    ServiceAssert.notNull(targetDate, () -> "targetDate must be defined!");
    
    final var activeFrom = active_to_hash.keySet().stream().sorted().collect(Collectors.toList());
    LocalDateTime found = null; 
    for(final var candidate : activeFrom) {
      if(candidate.compareTo(targetDate) <= 0) {
        found = candidate;
      }
    }
    
    if(found == null) {
      throw EnvirException.notFoundDef(targetDate, () -> {
        final var others = activeFrom.stream().sorted().map(e -> e.toString()).collect(Collectors.toList());
        return "Possible candidates: " + String.join(",", others);
      });
    }
    
    final var service = active_to_hash.get(found).stream()
      .map(e -> hash_to_source.get(e))
      .filter(e -> e.getType() == ConfigType.PROJECT)
      .map(e -> getById(e.getId()))
      .findFirst().orElse(null);
    ServiceAssert.notNull(service, () -> "Can't resolve service definition for (target date): '" + targetDate + "'!");
    return (ServiceProgramDef) service;
  }
  @Override
  public ServiceProgram getByRefId(RefIdValue ref) {
    ServiceAssert.notNull(ref, () -> "ref must be defined!");
    final var id = ref.getTagName() + "/" + ref.getType();
    return getById(id);
  }
  private ServiceProgram create(ServiceProgramSource source) {
    final ServiceProgram program;
    switch (source.getType()) {
    case STENCIL: program = new ServiceProgramStencilImpl(source); break;
    case PROJECT: program = new ServiceProgramDefImpl(source); break;
    case DIALOB: program = new ServiceProgramDialobImpl(source); break;
    case HDES: program = new ServiceProgramHdesImpl(source); break;
    case RELEASE: program = new ServiceProgramRelImpl(source); break;
    default: throw EnvirException.notSupportedSource(source, () -> "");
    }
    config.getCache().save(program);
    return program;
  }
  @Override
  public ServiceProgramRel getRel(LocalDateTime targetDate) {
    ServiceAssert.notNull(targetDate, () -> "targetDate must be defined!");
    
    final var activeFrom = active_to_hash.keySet().stream().sorted().collect(Collectors.toList());
    LocalDateTime found = null; 
    for(final var candidate : activeFrom) {
      if(candidate.compareTo(targetDate) <= 0) {
        found = candidate;
      }
    }
    
    if(found == null) {
      throw EnvirException.notFoundDef(targetDate, () -> {
        final var others = activeFrom.stream().sorted().map(e -> e.toString()).collect(Collectors.toList());
        return "Possible candidates: " + String.join(",", others);
      });
    }
    
    final var service = active_to_hash.get(found).stream()
      .map(e -> hash_to_source.get(e))
      .filter(e -> e.getType() == ConfigType.RELEASE)
      .map(e -> getById(e.getId()))
      .findFirst().orElse(null);
    ServiceAssert.notNull(service, () -> "Can't resolve service rel for (target date): '" + targetDate + "'!");
    return (ServiceProgramRel) service;
  }
  @Override
  public ServiceProgramStencil getStecil(LocalDateTime targetDate) {
    ServiceAssert.notNull(targetDate, () -> "targetDate must be defined!");
    
    final var activeFrom = active_to_hash.keySet().stream().sorted().collect(Collectors.toList());
    LocalDateTime found = null; 
    for(final var candidate : activeFrom) {
      if(candidate.compareTo(targetDate) <= 0) {
        found = candidate;
      }
    }
    
    if(found == null) {
      throw EnvirException.notFoundDef(targetDate, () -> {
        final var others = activeFrom.stream().sorted().map(e -> e.toString()).collect(Collectors.toList());
        return "Possible candidates: " + String.join(",", others);
      });
    }
    
    final var service = active_to_hash.get(found).stream()
      .map(e -> hash_to_source.get(e))
      .filter(e -> e.getType() == ConfigType.STENCIL)
      .map(e -> getById(e.getId()))
      .findFirst().orElse(null);
    ServiceAssert.notNull(service, () -> "Can't resolve stencil for (target date): '" + targetDate + "'!");
    return (ServiceProgramStencil) service;
  }
  @Override
  public ServiceProgramHdes getHdes(LocalDateTime targetDate) {
    ServiceAssert.notNull(targetDate, () -> "targetDate must be defined!");
    
    final var activeFrom = active_to_hash.keySet().stream().sorted().collect(Collectors.toList());
    LocalDateTime found = null; 
    for(final var candidate : activeFrom) {
      if(candidate.compareTo(targetDate) <= 0) {
        found = candidate;
      }
    }
    
    if(found == null) {
      throw EnvirException.notFoundDef(targetDate, () -> {
        final var others = activeFrom.stream().sorted().map(e -> e.toString()).collect(Collectors.toList());
        return "Possible candidates: " + String.join(",", others);
      });
    }
    
    final var service = active_to_hash.get(found).stream()
      .map(e -> hash_to_source.get(e))
      .filter(e -> e.getType() == ConfigType.HDES)
      .map(e -> getById(e.getId()))
      .findFirst().orElse(null);
    ServiceAssert.notNull(service, () -> "Can't resolve hdes for (target date): '" + targetDate + "'!");
    return (ServiceProgramHdes) service;
  }
  @Override
  public ServiceProgramDialob getForm(String objectId) {
    ServiceAssert.notNull(objectId, () -> "objectId must be defined!");
    
    final var service = getById(objectId + "/" + ConfigType.DIALOB);
    return (ServiceProgramDialob) service;
  }
}
