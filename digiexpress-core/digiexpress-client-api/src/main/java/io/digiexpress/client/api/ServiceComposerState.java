package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;


@Value.Immutable
@JsonSerialize(as = ImmutableServiceComposerState.class)
@JsonDeserialize(as = ImmutableServiceComposerState.class)
public interface ServiceComposerState extends Serializable {

  String getName();
  @Nullable String getCommit();
  @Nullable String getCommitMsg();
  SiteContentType getContentType();
  Map<String, ServiceRevisionDocument> getRevisions();
  Map<String, ServiceDefinitionDocument> getDefinitions();
  Map<String, ServiceReleaseDocument> getReleases();
  Map<String, ServiceConfigDocument> getConfigs();

  List<ComposerMessage> getMessages();
  ComposerDialob getDialob();
  ComposerStencil getStencil();
  ComposerHdes getHdes();
  
  enum SiteContentType { OK, ERRORS, NOT_CREATED  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableMigrationState.class) @JsonDeserialize(as = ImmutableMigrationState.class)
  interface MigrationState {

  }
  

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerMessage.class)
  @JsonDeserialize(as = ImmutableComposerMessage.class)
  interface ComposerMessage {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerHdes.class)
  @JsonDeserialize(as = ImmutableComposerHdes.class)
  interface ComposerHdes {
    
  }
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerDialob.class)
  @JsonDeserialize(as = ImmutableComposerDialob.class)
  interface ComposerDialob {

  }
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerStencil.class)
  @JsonDeserialize(as = ImmutableComposerStencil.class)
  interface ComposerStencil {
    
  }
  
}
