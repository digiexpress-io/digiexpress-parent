package io.digiexpress.client.spi.executors;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.dialob.api.proto.Actions;
import io.dialob.client.api.DialobClient.ProgramEnvir;
import io.dialob.client.api.DialobClient.ProgramEnvirValue;
import io.dialob.client.api.DialobClient.ProgramStatus;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobDocument;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.api.ImmutableProgramWrapper;
import io.dialob.client.api.ImmutableStoreEntity;
import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ServiceClient.DialobExecutor;
import io.digiexpress.client.api.ServiceClient.Execution;
import io.digiexpress.client.api.ServiceClient.ExecutionDialobBody;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceEnvir;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobExecutorImpl implements DialobExecutor {
  private final ServiceClientConfig config;
  private final ProcessState state;
  private final ServiceEnvir envir;
  
  @Override
  public DialobExecutor actions(Actions userActions) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Execution<ExecutionDialobBody> build() {
    final var created = state.getStepCreated();
    final var client = config.getDialob();
    final var envir = FixedProgramEnvir.create(created.getBody().getFormId(), this.envir, this.config);
    
    final var executor = client.executor(envir).create(created.getBody().getFormId(), (init) -> {
      init
      .id(UUID.randomUUID().toString())
      .rev(UUID.randomUUID().toString());
    });
    
    return null;
  }
  
  
  @RequiredArgsConstructor
  private static class FixedProgramEnvir implements ProgramEnvir {
    private final ProgramWrapper wrapper;
    @Override
    public ProgramWrapper findByFormId(String formId) throws DocumentNotFoundException {
      // TODO Auto-generated method stub
      return wrapper;
    }
    @Override
    public ProgramWrapper findByFormIdAndRev(String formId, String formRev) throws DocumentNotFoundException {
      // TODO Auto-generated method stub
      return wrapper;
    }
    @Override
    public List<ProgramWrapper> findAll() {
      return Arrays.asList(wrapper);
    }
    @Override
    public Map<String, ProgramEnvirValue<?>> getValues() {
      return Map.of(wrapper.getId(), wrapper);
    }
    
    public static FixedProgramEnvir create(String formId, ServiceEnvir envir, ServiceClientConfig config) {
      final var value = envir.getForm(formId);
      final var form = value.getDelegate(config);
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
      final var wrapper = ImmutableProgramWrapper.builder()
          .id(form.getId())
          .document(doc)
          .program(value.getCompiled(config))
          .source(ImmutableStoreEntity.builder().id("").version("").bodyType(DialobDocument.DocumentType.FORM).body("").build())
          .status(ProgramStatus.UP)
          .build();
      return new FixedProgramEnvir(wrapper);
    }
  }
}
