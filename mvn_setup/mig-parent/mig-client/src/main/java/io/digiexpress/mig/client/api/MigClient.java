package io.digiexpress.mig.client.api;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.smallrye.mutiny.Uni;

public interface MigClient {
  SourceDbTaskQuery taskQuery();
  SourceDbDialobQuery dialobQuery();
  
  interface SourceDbDialobQuery {
    SourceDbDialobQuery includeFromQuestionnaires(List<String> questionnaires);
    SourceDbDialobQuery includeFrom(List<? extends FormFilter> formMeta);
    Uni<SourceForms> findAll();
  }
  
  interface SourceDbTaskQuery {
    Uni<SourceTasks> findAll();
  }
  
  
  @Value.Immutable
  interface FormFilter {
    Optional<String> getFormId();
    String getFormTag();
    String getFormName();
  }
  

  
}
