package io.digiexpress.client.spi.envir;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceEnvir.ProgramMessage;
import io.digiexpress.client.api.ServiceEnvir.ProgramStatus;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramService;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Data
public class ServiceProgramServiceImpl implements ServiceProgramService {
  private static final long serialVersionUID = 7447157833915305419L;
  private final String id;
  private final List<ProgramMessage> errors = new ArrayList<>();
  private final ServiceProgramSource source;
  private ProgramStatus status = ProgramStatus.CREATED;
  @JsonIgnore
  private transient ServiceDefinitionDocument delegate;

  public ServiceProgramServiceImpl(ServiceProgramSource source) {
    super();
    this.source = source;
    this.id = source.getId();
  }
  
  @Override
  public ServiceDefinitionDocument getDelegate(ServiceClientConfig config) {
    if(this.delegate == null) {
      final var service = config.getCompression().decompressionService(source.getBody());
      this.delegate = service;
      this.status = ProgramStatus.UP;
    }
    return this.delegate;
  }
}
