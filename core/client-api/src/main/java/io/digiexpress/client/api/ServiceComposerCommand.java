package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.StencilComposer.SiteState;

public interface ServiceComposerCommand extends Serializable {

  @Value.Immutable @JsonSerialize(as = ImmutableCreateProjectRevision.class) @JsonDeserialize(as = ImmutableCreateProjectRevision.class)
  interface CreateProjectRevision extends ServiceComposerCommand {
    String getName();
    String getDescription();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableCreateRelease.class) @JsonDeserialize(as = ImmutableCreateRelease.class)
  interface CreateRelease extends ServiceComposerCommand {
    String getServiceDefinitionId();
    String getName();
    String getDesc();
    LocalDateTime getActiveFrom();
    LocalDateTime getTargetDate();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateServiceDescriptor.class) @JsonDeserialize(as = ImmutableCreateServiceDescriptor.class)
  interface CreateServiceDescriptor extends ServiceComposerCommand {
    String getDefId();
    String getDefVersionId();
    String getName();
    String getDesc();
    String getFormId();
    String getFlowId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateMigration.class) @JsonDeserialize(as = ImmutableCreateMigration.class)
  interface CreateMigration extends ServiceComposerCommand {
    List<FormRevisionDocument> getFormRevs();
    List<FormDocument> getForms();
    ServiceDefinition getServices();
    StoreState getHdes();
    SiteState getStencil();
  }
}
