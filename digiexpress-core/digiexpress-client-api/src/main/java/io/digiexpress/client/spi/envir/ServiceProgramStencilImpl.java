package io.digiexpress.client.spi.envir;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceEnvir.ProgramMessage;
import io.digiexpress.client.api.ServiceEnvir.ProgramStatus;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramSource;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramStencil;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Data
public class ServiceProgramStencilImpl implements ServiceProgramStencil {
  private final String id;
  private final List<ProgramMessage> errors = new ArrayList<>();
  private final ServiceProgramSource source;
  private ProgramStatus status = ProgramStatus.CREATED;
  @JsonIgnore
  private transient Sites delegate;

  public ServiceProgramStencilImpl(ServiceProgramSource source) {
    super();
    this.source = source;
    this.id = source.getId();
  }
  
  @Override
  public Sites getDelegate(ServiceClientConfig config) {
    if(this.delegate == null) {
      final var sites = config.getCompression().decompressionStencil(source.getBody());
      this.delegate = sites;
      this.status = ProgramStatus.UP;
    }
    return this.delegate;
  }
  
}
