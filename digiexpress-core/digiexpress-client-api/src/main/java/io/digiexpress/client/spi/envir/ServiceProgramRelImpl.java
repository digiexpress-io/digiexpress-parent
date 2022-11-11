package io.digiexpress.client.spi.envir;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.ImmutableServiceReleaseDocument;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceEnvir.ProgramMessage;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramRel;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramSource;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Data
public class ServiceProgramRelImpl implements ServiceProgramRel {
  private static final long serialVersionUID = 7447157833915305419L;
  private final String id;
  private final List<ProgramMessage> errors = new ArrayList<>();
  private final ServiceProgramSource source;
  private ServiceProgramStatus status = ServiceProgramStatus.CREATED;
  @JsonIgnore
  private transient ServiceReleaseDocument delegate;

  public ServiceProgramRelImpl(ServiceProgramSource source) {
    super();
    this.source = source;
    this.id = source.getId();
  }
  
  @Override
  public ServiceReleaseDocument getDelegate(ServiceClientConfig config) {
    if(this.delegate == null) {
      var service = config.getCompression().decompressionRelease(source.getBody());
      if(service.getId() == null) {
        log.warn(String.format("release has no id: '{}', using source id and and hash", service.getName()));
        service = ImmutableServiceReleaseDocument.builder().from(service).id(source.getId()).version(source.getHash()).build();
      }
      
      this.delegate = service;
      this.status = ServiceProgramStatus.UP;
    }
    return this.delegate;
  }
}
