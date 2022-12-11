package io.digiexpress.client.spi.envir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceEnvir.ProgramMessage;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramHdes;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramSource;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramStatus;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Data
public class ServiceProgramHdesImpl implements ServiceProgramHdes {
  private static final long serialVersionUID = -3297896329693016040L;
  private final String id;
  private final ServiceProgramSource source;
  private final List<ProgramMessage> errors = new ArrayList<>();
  private ServiceProgramStatus status = ServiceProgramStatus.CREATED;
  @JsonIgnore
  private transient AstTag delegate;
  @JsonIgnore
  private transient ProgramEnvir compiled;
  @JsonIgnore
  private transient Map<String, String> flow_id_to_name;
  
  
  public ServiceProgramHdesImpl(ServiceProgramSource source) {
    super();
    this.source = source;
    this.id = source.getId();
  }
  
  @Override
  public AstTag getDelegate(ServiceClientConfig config) {
    if(this.delegate == null) {
      final var tag = config.getCompression().decompressionHdes(source.getBody());
      this.delegate = tag;
      this.status = ServiceProgramStatus.PARSED;
    }
    return this.delegate;
  }

  @Override
  public Optional<ProgramEnvir> getCompiled(ServiceClientConfig config) {
    if(this.compiled == null) {
      try {
        final var tag = getDelegate(config);
        final Map<AstBodyType, Integer> order = Map.of(
            AstBodyType.DT, 1,
            AstBodyType.FLOW_TASK, 2,
            AstBodyType.FLOW, 3);
        
        final var assets = new ArrayList<>(tag.getValues());
        assets.sort((AstTagValue o1, AstTagValue o2) -> Integer.compare(order.get(o1.getBodyType()), order.get(o2.getBodyType())));
        
        final var builder = config.getHdes().envir();
        for(final var asset : assets) {
          final var id = asset.getId() == null ? UUID.randomUUID().toString() : asset.getId();
          final var entity = ImmutableStoreEntity.builder().id(id).hash(asset.getHash()).body(asset.getCommands()).bodyType(asset.getBodyType()).build();
          switch (asset.getBodyType()) {
          case FLOW:
            builder.addCommand().flow(entity).id(id).build();            
            break;
          case DT:
            builder.addCommand().decision(entity).id(id).build();            
            break;
          case FLOW_TASK:
            builder.addCommand().service(entity).id(id).build();            
            break;
          default: continue;
          }
        }
        this.compiled = builder.build();
        this.status = ServiceProgramStatus.UP;
        
        final Map<String, String> flows = compiled.getFlowsByName().values().stream()
          .collect(Collectors.toMap(e -> e.getId(), e -> e.getAst().get().getName()));
        this.flow_id_to_name = Collections.unmodifiableMap(flows);
        
      } catch(Exception e) {
        log.error(e.getMessage(), e);
        this.status = ServiceProgramStatus.ERROR;
      }
    }
    return Optional.ofNullable(this.compiled);
  }

  @Override
  public String getFlowName(String flowId, ServiceClientConfig config) {
    final var compiled = getCompiled(config);
    ServiceAssert.isTrue(compiled.isPresent(), () -> "can't compile hdes envir!");
    ServiceAssert.notNull(flowId, () -> "flowId must be defined!");
    ServiceAssert.isTrue(this.flow_id_to_name.containsKey(flowId), () -> "no flow with id: '" + flowId + "'!");
    return this.flow_id_to_name.get(flowId);
  }
}
