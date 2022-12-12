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
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.StencilComposer.SiteState;

public interface ComposerEntity extends Serializable {

  @Value.Immutable @JsonSerialize(as = ImmutableCreateProjectRevision.class) @JsonDeserialize(as = ImmutableCreateProjectRevision.class)
  interface CreateProjectRevision extends ComposerEntity {
    String getName();
    String getDescription();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableCreateRelease.class) @JsonDeserialize(as = ImmutableCreateRelease.class)
  interface CreateRelease extends ComposerEntity {
    String getServiceDefinitionId();
    String getName();
    String getDesc();
    LocalDateTime getActiveFrom();
    LocalDateTime getTargetDate();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateServiceDescriptor.class) @JsonDeserialize(as = ImmutableCreateServiceDescriptor.class)
  interface CreateServiceDescriptor extends ComposerEntity {
    String getDefId();
    String getDefVersionId();
    String getName();
    String getDesc();
    String getFormId();
    String getFlowId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateMigration.class) @JsonDeserialize(as = ImmutableCreateMigration.class)
  interface CreateMigration extends ComposerEntity {
    List<FormRevisionDocument> getFormRevs();
    List<FormDocument> getForms();
    ServiceDefinition getServices();
    StoreState getHdes();
    SiteState getStencil();
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableServiceComposerState.class)
  @JsonDeserialize(as = ImmutableServiceComposerState.class)
  public interface ServiceComposerState extends ComposerEntity {

    String getName();
    @Nullable String getCommit();
    @Nullable String getCommitMsg();
    SiteContentType getContentType();
    Map<String, Project> getRevisions();
    Map<String, ServiceDefinition> getDefinitions();
    Map<String, ServiceRelease> getReleases();
    List<ComposerMessage> getMessages();
    
    enum SiteContentType { OK, ERRORS, NOT_CREATED  }
  }
  


  @Value.Immutable
  @JsonSerialize(as = ImmutableServiceComposerDefinitionState.class)
  @JsonDeserialize(as = ImmutableServiceComposerDefinitionState.class)
  public interface ServiceComposerDefinitionState extends ComposerEntity {
    ServiceDefinition getDefinition();
    ComposerDialob getDialob();
    ComposerStencil getStencil();
    ComposerHdes getHdes();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableMigrationState.class) @JsonDeserialize(as = ImmutableMigrationState.class)
  interface MigrationState extends ComposerEntity {

  }

  @Value.Immutable @JsonSerialize(as = ImmutableComposerMessage.class) @JsonDeserialize(as = ImmutableComposerMessage.class)
  interface ComposerMessage extends ComposerEntity {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  

  @Value.Immutable @JsonSerialize(as = ImmutableComposerHdes.class) @JsonDeserialize(as = ImmutableComposerHdes.class)
  interface ComposerHdes extends ComposerEntity {
    Map<String, io.resys.hdes.client.api.HdesComposer.ComposerEntity<AstFlow>> getFlows();
    Map<String, io.resys.hdes.client.api.HdesComposer.ComposerEntity<AstService>> getServices();
    Map<String, io.resys.hdes.client.api.HdesComposer.ComposerEntity<AstDecision>> getDecisions();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerStencil.class) @JsonDeserialize(as = ImmutableComposerStencil.class)
  interface ComposerStencil extends ComposerEntity {
    Map<String, LocalizedSite> getSites();
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDialob.class) @JsonDeserialize(as = ImmutableComposerDialob.class)
  interface ComposerDialob extends ComposerEntity {
    Map<String, FormDocument> getForms();
    Map<String, FormRevisionDocument> getRevs();
  }
}
