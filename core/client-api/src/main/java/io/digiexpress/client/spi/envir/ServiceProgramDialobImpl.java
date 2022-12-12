package io.digiexpress.client.spi.envir;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.dialob.api.form.Form;
import io.dialob.client.api.DialobDocument;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.program.DialobProgram;
import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.AssetEnvir.ProgramMessage;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramStatus;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramDialob;
import io.digiexpress.client.api.AssetEnvir.ServiceProgramSource;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@lombok.Data
public class ServiceProgramDialobImpl implements ServiceProgramDialob {

  private static final long serialVersionUID = 6731073474000035479L;
  private final String id;
  private final List<ProgramMessage> errors = new ArrayList<>();
  private final ServiceProgramSource source;
  private ServiceProgramStatus status = ServiceProgramStatus.CREATED;
  
  @JsonIgnore
  private transient Form delegate;
  @JsonIgnore
  private transient DialobProgram compiled;
  
  public ServiceProgramDialobImpl(ServiceProgramSource source) {
    super();
    this.source = source;
    this.id = source.getId();
  }
  
  @Override
  public Form getDelegate(ClientConfig config) {
    if(this.delegate == null) {
      final var form = config.getArchiver().decompressionDialob(source.getBody());
      this.delegate = form;
      this.status = ServiceProgramStatus.PARSED;
    }
    return this.delegate;
  }

  @Override
  public Optional<DialobProgram> getCompiled(ClientConfig config) {
    if(this.compiled == null) {
      try {
        final var form = getDelegate(config);
        final var doc = ImmutableFormDocument.builder().data(form)
            .id(form.getId())
            .created(Instant.ofEpochMilli(form.getMetadata().getCreated().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime())
            .updated(Instant.ofEpochMilli(form.getMetadata().getLastSaved().getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime())
            .name(form.getName())
            .description("release")
            .version(form.getRev())
            .type(DialobDocument.DocumentType.FORM)
            .build();
        final var dialob = config.getDialob().program().form(doc).build();
        this.compiled = dialob;
        this.status = ServiceProgramStatus.UP;
      } catch(Exception e) {
        log.error(e.getMessage(), e);
        this.status = ServiceProgramStatus.ERROR;
      }
    }
    return Optional.ofNullable(this.compiled);
  }
}
