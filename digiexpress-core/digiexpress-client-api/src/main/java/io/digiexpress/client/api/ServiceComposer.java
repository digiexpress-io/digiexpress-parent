package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.StencilComposer.SiteState;

public interface ServiceComposer {
  CreateBuilder create();
  QueryBuilder query();
  
  interface QueryBuilder {
    Uni<ComposerState> head();
    Uni<ComposerState> release(String releaseId);
  }
  
  interface CreateBuilder {
    Uni<ServiceRevisionDocument> revision(CreateServiceRevision init);
    Uni<ServiceDefinitionDocument> process(CreateProcess process);
    Uni<ServiceReleaseDocument> release(CreateRelease rel);
    Uni<MigrationState> migrate(CreateMigration mig);
  }

  interface Command extends Serializable {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateServiceRevision.class) @JsonDeserialize(as = ImmutableCreateServiceRevision.class)
  interface CreateServiceRevision extends Command {
    String getName();
    String getDescription();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableCreateRelease.class) @JsonDeserialize(as = ImmutableCreateRelease.class)
  interface CreateRelease extends Command {
    String getServiceDefinitionId();
    String getName();
    String getDesc();
    LocalDateTime getActiveFrom();
    LocalDateTime getTargetDate();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateProcess.class) @JsonDeserialize(as = ImmutableCreateProcess.class)
  interface CreateProcess extends Command {
    String getServiceRevisionId();
    String getServiceRevisionVersionId();
    String getName();
    String getDesc();
    String getFormId();
    String getFlowId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateMigration.class) @JsonDeserialize(as = ImmutableCreateMigration.class)
  interface CreateMigration extends Command {
    List<FormRevisionDocument> getFormRevs();
    List<FormDocument> getForms();
    ServiceDefinitionDocument getServices();
    StoreState getHdes();
    SiteState getStencil();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableMigrationState.class) @JsonDeserialize(as = ImmutableMigrationState.class)
  interface MigrationState {

  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerState.class)
  @JsonDeserialize(as = ImmutableComposerState.class)
  interface ComposerState {
    String getName();
    @Nullable String getCommit();
    @Nullable String getCommitMsg();
    SiteContentType getContentType();
    Map<String, ServiceRevisionDocument> getRevisions();
    Map<String, ServiceDefinitionDocument> getDefinitions();
    Map<String, ServiceReleaseDocument> getReleases();
    Map<String, ServiceConfigDocument> getConfigs();
    Map<String, ComposerForm> getForms();
    Map<String, ComposerFlow> getFlows();
    List<ComposerMessage> getMessages(); 
    
  }
  

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerMessage.class)
  @JsonDeserialize(as = ImmutableComposerMessage.class)
  interface ComposerMessage {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  
  interface ComposerFlow {}
  interface ComposerForm {}
  
  enum SiteContentType { OK, ERRORS, NOT_CREATED  }
}