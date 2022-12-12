package io.digiexpress.client.spi.envir;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.AssetEnvir.ProgramMessage;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramStatus;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramSource;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramStencil;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Data
public class ServiceProgramStencilImpl implements ServiceProgramStencil {
  private final String id;
  private final List<ProgramMessage> errors = new ArrayList<>();
  private final ServiceProgramSource source;
  private ServiceProgramStatus status = ServiceProgramStatus.CREATED;
  @JsonIgnore
  private transient Sites delegate;

  public ServiceProgramStencilImpl(ServiceProgramSource source) {
    super();
    this.source = source;
    this.id = source.getId();
  }
  
  @Override
  public Sites getDelegate(ClientConfig config) {
    if(this.delegate == null) {
      final var sites = config.getArchiver().decompressionStencil(source.getBody());
      this.delegate = sites;
      this.status = ServiceProgramStatus.UP;
    }
    return this.delegate;
  }
  
}
