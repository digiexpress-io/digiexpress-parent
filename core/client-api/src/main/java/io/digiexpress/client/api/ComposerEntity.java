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
import io.thestencil.client.api.StencilComposer.SiteState;

public interface ComposerEntity extends Serializable {

  enum ComposerContentType { OK, ERRORS, NOT_CREATED  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateProjectRevision.class) @JsonDeserialize(as = ImmutableCreateProjectRevision.class)
  interface CreateProjectRevision extends ComposerEntity {
    @Nullable String getProjectId();
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
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateDescriptor.class) @JsonDeserialize(as = ImmutableCreateDescriptor.class)
  interface CreateDescriptor extends ComposerEntity {
    String getDefId();
    String getDefVersionId();
    String getName();
    String getDesc();
    String getFormId();
    String getFlowId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateMigration.class) @JsonDeserialize(as = ImmutableCreateMigration.class)
  interface CreateMigration extends ComposerEntity {
    @Nullable String getProjectId();
    List<FormRevisionDocument> getFormRevs();
    List<FormDocument> getForms();
    ServiceDefinition getServices();
    StoreState getHdes();
    SiteState getStencil();
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableHeadState.class)
  @JsonDeserialize(as = ImmutableHeadState.class)
  interface HeadState extends ComposerEntity {
    String getName();
    @Nullable String getCommit();
    @Nullable String getCommitMsg();
    ComposerContentType getContentType();
    Map<String, Project> getProjects();
    Map<String, ServiceDefinition> getDefinitions();
    Map<String, ServiceRelease> getReleases();
    List<ComposerMessage> getMessages();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableDefinitionState.class)
  @JsonDeserialize(as = ImmutableDefinitionState.class)
  interface DefinitionState extends ComposerEntity {
    ServiceDefinition getDefinition();
    DialobTree getDialob();
    StencilTree getStencil();
    HdesTree getHdes();
  }
  

  @Value.Immutable @JsonSerialize(as = ImmutableTagState.class) @JsonDeserialize(as = ImmutableTagState.class)
  interface TagState extends ComposerEntity {
    String getName();
    ProjectTags getValue();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableMigrationState.class) @JsonDeserialize(as = ImmutableMigrationState.class)
  interface MigrationState extends ComposerEntity {

  }

  @Value.Immutable @JsonSerialize(as = ImmutableComposerMessage.class) @JsonDeserialize(as = ImmutableComposerMessage.class)
  interface ComposerMessage extends Serializable {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableHdesTree.class) @JsonDeserialize(as = ImmutableHdesTree.class)
  interface HdesTree extends Serializable {
    Map<String, io.resys.hdes.client.api.HdesComposer.ComposerEntity<AstFlow>> getFlows();
    Map<String, io.resys.hdes.client.api.HdesComposer.ComposerEntity<AstService>> getServices();
    Map<String, io.resys.hdes.client.api.HdesComposer.ComposerEntity<AstDecision>> getDecisions();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableStencilTree.class) @JsonDeserialize(as = ImmutableStencilTree.class)
  interface StencilTree extends Serializable {
    Map<String, io.thestencil.client.api.MigrationBuilder.LocalizedSite> getSites();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableDialobTree.class) @JsonDeserialize(as = ImmutableDialobTree.class)
  interface DialobTree extends Serializable {
    Map<String, io.dialob.client.api.DialobDocument.FormDocument> getForms();
    Map<String, io.dialob.client.api.DialobDocument.FormRevisionDocument> getRevs();
  }
}
