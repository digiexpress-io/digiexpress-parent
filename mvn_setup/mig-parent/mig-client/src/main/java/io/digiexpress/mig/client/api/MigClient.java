package io.digiexpress.mig.client.api;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface MigClient {
  SourceTaskQuery taskQuery();
  
  SourceDialobQuery dialobQuery();
  SourceThenaQuery thenaQuary();
  
  TargetDialobBuilder dialobBuilder();
  TargetTaskBuilder taskBuilder();
  TargetWrenchBuilder wrenchBuilder();
  TargetStencilBuilder stencilBuilder();
  TargetTaskRolesBuilder taskRolesBuilder();
  
  
  interface TargetStencilBuilder {
    Uni<SourceThena> build(SourceThena thena, SourceTasks tasks, String tenantName);
  }
  
  
  interface TargetWrenchBuilder {
    Uni<SourceThena> build(SourceThena tasks, String tenantName);
  }
  
  
  interface TargetTaskBuilder {
    Uni<SourceTasks> build(SourceTasks tasks, String tenantName);
  }
  
  interface TargetTaskRolesBuilder {
    Uni<SourceTasks> build(SourceTasks tasks, String tenantName);
  }
  
  interface TargetDialobBuilder {
    Uni<SourceForms> build(SourceForms source);
  }
  
  interface SourceThenaQuery {
    Uni<SourceThena> findAll(String tenanPrefix);
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
  
  interface StencilEntityConverter {
    JsonObject convertValue(JsonObject blob);
  }
  
}
