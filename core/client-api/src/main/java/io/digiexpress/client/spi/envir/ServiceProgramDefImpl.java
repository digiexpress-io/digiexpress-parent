package io.digiexpress.client.spi.envir;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ServiceEnvir.ProgramMessage;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramStatus;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramDef;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Data
public class ServiceProgramDefImpl implements ServiceProgramDef {
  private static final long serialVersionUID = 7447157833915305419L;
  private final String id;
  private final List<ProgramMessage> errors = new ArrayList<>();
  private final ServiceProgramSource source;
  private ServiceProgramStatus status = ServiceProgramStatus.CREATED;
  @JsonIgnore
  private transient ServiceDefinition delegate;

  public ServiceProgramDefImpl(ServiceProgramSource source) {
    super();
    this.source = source;
    this.id = source.getId();
  }
  
  @Override
  public ServiceDefinition getDelegate(ClientConfig config) {
    if(this.delegate == null) {
      final var service = config.getArchiver().decompressionService(source.getBody());
      this.delegate = service;
      this.status = ServiceProgramStatus.UP;
    }
    return this.delegate;
  }
}
