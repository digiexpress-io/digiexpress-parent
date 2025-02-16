package io.digiexpress.mig.client.api;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.smallrye.mutiny.Uni;

public interface MigClient {
  SourceTaskQuery taskQuery();
  SourceDialobQuery dialobQuery();
  
  TargetDialobBuilder dialobBuilder();
  TargetTaskBuilder taskBuilder();
  
  interface TargetTaskBuilder {
    Uni<SourceTasks> build(SourceTasks tasks);
  }
  
  interface TargetDialobBuilder {
    Uni<SourceForms> build(SourceForms source);
  }
  
  interface SourceDialobQuery {
    SourceDialobQuery includeFromQuestionnaires(List<String> questionnaires);
    SourceDialobQuery includeFrom(List<? extends FormFilter> formMeta);
    Uni<SourceForms> findAll();
  }
  
  interface SourceTaskQuery {
    Uni<SourceTasks> findAll();
  }
  
  
  @Value.Immutable
  interface FormFilter {
    Optional<String> getFormId();
    String getFormTag();
    String getFormName();
  }
  

  
}
