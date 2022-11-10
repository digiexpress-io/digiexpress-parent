package io.digiexpress.client.spi.envir;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceClient.ServiceEnvirBuilder;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseValue;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramSource;
import io.digiexpress.client.spi.support.EnvirException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ServiceEnvirBuilderImpl implements ServiceEnvirBuilder {
  private final ServiceClientConfig config;
  
  // hash to source
  private final Map<String, ServiceProgramSource> hash_to_source = new HashMap<>();
  private final Map<LocalDateTime, List<String>> active_to_hash = new HashMap<>();
  
  @Override
  public ServiceEnvirBuilder add(ServiceReleaseDocument release) {
    if(active_to_hash.containsKey(release.getActiveFrom())) {
      throw EnvirException.isDefined(release, () -> "");
    }
    release.getValues().forEach(value -> addSrc(value, release));
    return this;
  }

  @Override
  public ServiceEnvir build() {
    return new ServiceEnvirImpl(config, hash_to_source, active_to_hash);
  }
  
  private void addSrc(ServiceReleaseValue value, ServiceReleaseDocument release) {
    final List<String> active;
    if(active_to_hash.containsKey(release.getActiveFrom())) {
      active = active_to_hash.get(release.getActiveFrom());
    } else {
      active = new ArrayList<>();
      active_to_hash.put(release.getActiveFrom(), active);
    }
    active.add(value.getBodyHash());
    
    if(hash_to_source.containsKey(value.getBodyHash())) {
      log.info(
          "Skipping release asset(id/type/hash): '" + value.getId() + "/" + value.getBodyType() + "/" + value.getBodyHash() + "'"+   
          " because it's already defined!");
      return;
    }
    
    final var src = ImmutableProgramSource.builder()
        .id(value.getId() + "/" + value.getBodyType())
        .body(value.getBody())
        .type(value.getBodyType())
        .hash(value.getBodyHash())
        .build();
    hash_to_source.put(value.getBodyHash(), src);

  }
  
  @lombok.Data @lombok.Builder
  private static class ImmutableProgramSource implements ServiceProgramSource {
    private static final long serialVersionUID = -3967877009056482722L;
    private final String id;
    private final String hash;
    private final String body;
    private final ConfigType type;
  }
}
