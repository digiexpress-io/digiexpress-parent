package io.digiexpress.client.spi.builders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.CompressionMapper;
import io.digiexpress.client.api.ServiceClient.ServiceEnvirBuilder;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseValue;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.api.ServiceEnvir.ProgramSource;
import io.digiexpress.client.spi.support.EnvirException;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceEnvirBuilderImpl implements ServiceEnvirBuilder {

  // hash to source
  private final Map<String, ProgramSource> hash_to_source = new HashMap<>();
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
    // TODO Auto-generated method stub
    return null;
  }
  
  private void addSrc(ServiceReleaseValue value, ServiceReleaseDocument release) {
    final List<String> active;
    if(active_to_hash.containsKey(release.getActiveFrom())) {
      active = active_to_hash.get(release.getActiveFrom());
    } else {
      active = new ArrayList<>();
      active_to_hash.put(release.getActiveFrom(), active);
    }
    
    if(hash_to_source.containsKey(value.getBodyHash())) {
      log.info(
          "Skipping release asset(id/type/hash): '" + value.getId() + "/" + value.getBodyType() + "/" + value.getBodyHash() + "'"+   
          " because it's already defined!");
      return;
    }
    
    final var src = ImmutableProgramSource.builder().body(value.getBody()).type(value.getBodyType()).hash(value.getBodyHash()).build();
    hash_to_source.put(value.getId(), src);    
  }
  
  @lombok.Data @lombok.Builder
  private static class ImmutableProgramSource implements ProgramSource {
    private static final long serialVersionUID = -3967877009056482722L;
    private final String hash;
    private final String body;
    private final ConfigType type;
    
    @Override
    public Sites toStencil(CompressionMapper mapper) {
      return mapper.decompressionStencil(body);
    }
    @Override
    public Form toForm(CompressionMapper mapper) {
      return mapper.decompressionDialob(body);
    }
    @Override
    public AstTag toHdes(CompressionMapper mapper) {
      return mapper.decompressionHdes(body);
    }
    @Override
    public ServiceDefinitionDocument toService(CompressionMapper mapper) {
      return mapper.decompressionService(body);
    }
  }
}
