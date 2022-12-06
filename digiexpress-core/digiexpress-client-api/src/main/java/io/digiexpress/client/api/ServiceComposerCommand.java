package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.StencilComposer.SiteState;

public interface ServiceComposerCommand extends Serializable {

  @Value.Immutable @JsonSerialize(as = ImmutableCreateServiceRevision.class) @JsonDeserialize(as = ImmutableCreateServiceRevision.class)
  interface CreateServiceRevision extends ServiceComposerCommand {
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
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateProcess.class) @JsonDeserialize(as = ImmutableCreateProcess.class)
  interface CreateProcess extends ServiceComposerCommand {
    String getServiceRevisionId();
    String getServiceRevisionVersionId();
    String getName();
    String getDesc();
    String getFormId();
    String getFlowId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateMigration.class) @JsonDeserialize(as = ImmutableCreateMigration.class)
  interface CreateMigration extends ServiceComposerCommand {
    List<FormRevisionDocument> getFormRevs();
    List<FormDocument> getForms();
    ServiceDefinitionDocument getServices();
    StoreState getHdes();
    SiteState getStencil();
  }
}
