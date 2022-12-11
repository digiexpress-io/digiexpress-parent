package io.digiexpress.client.api;

import java.io.Serializable;
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
import io.resys.hdes.client.api.HdesComposer.ComposerEntity;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;


@Value.Immutable
@JsonSerialize(as = ImmutableServiceComposerState.class)
@JsonDeserialize(as = ImmutableServiceComposerState.class)
public interface ServiceComposerState extends Serializable {

  String getName();
  @Nullable String getCommit();
  @Nullable String getCommitMsg();
  SiteContentType getContentType();
  Map<String, Project> getRevisions();
  Map<String, ServiceDefinition> getDefinitions();
  Map<String, ServiceRelease> getReleases();
  List<ComposerMessage> getMessages();
  
  enum SiteContentType { OK, ERRORS, NOT_CREATED  }


  @Value.Immutable
  @JsonSerialize(as = ImmutableServiceComposerDefinitionState.class)
  @JsonDeserialize(as = ImmutableServiceComposerDefinitionState.class)
  public interface ServiceComposerDefinitionState extends Serializable {
    ServiceDefinition getDefinition();
    ComposerDialob getDialob();
    ComposerStencil getStencil();
    ComposerHdes getHdes();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableMigrationState.class) @JsonDeserialize(as = ImmutableMigrationState.class)
  interface MigrationState extends Serializable {

  }

  @Value.Immutable @JsonSerialize(as = ImmutableComposerMessage.class) @JsonDeserialize(as = ImmutableComposerMessage.class)
  interface ComposerMessage extends Serializable {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  

  @Value.Immutable @JsonSerialize(as = ImmutableComposerHdes.class) @JsonDeserialize(as = ImmutableComposerHdes.class)
  interface ComposerHdes extends Serializable {
    Map<String, ComposerEntity<AstFlow>> getFlows();
    Map<String, ComposerEntity<AstService>> getServices();
    Map<String, ComposerEntity<AstDecision>> getDecisions();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerStencil.class) @JsonDeserialize(as = ImmutableComposerStencil.class)
  interface ComposerStencil extends Serializable {
    Map<String, LocalizedSite> getSites();
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDialob.class) @JsonDeserialize(as = ImmutableComposerDialob.class)
  interface ComposerDialob extends Serializable {
    Map<String, FormDocument> getForms();
    Map<String, FormRevisionDocument> getRevs();
  }
}
