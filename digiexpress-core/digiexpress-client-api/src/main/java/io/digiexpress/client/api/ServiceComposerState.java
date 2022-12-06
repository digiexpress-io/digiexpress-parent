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
    ComposerHdesTag getHead();
    Map<String, ComposerHdesTag> getTags();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableComposerHdesTag.class) @JsonDeserialize(as = ImmutableComposerHdesTag.class)
  interface ComposerHdesTag extends Serializable {
    String getId();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerStencil.class) @JsonDeserialize(as = ImmutableComposerStencil.class)
  interface ComposerStencil extends Serializable {
    ComposerStencilTag getHead();
    Map<String, ComposerStencilTag> getTags();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableComposerStencilTag.class) @JsonDeserialize(as = ImmutableComposerStencilTag.class)
  interface ComposerStencilTag extends Serializable {
    String getId();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDialob.class) @JsonDeserialize(as = ImmutableComposerDialob.class)
  interface ComposerDialob extends Serializable {
    Map<String, ComposerDialobForm> getForms();
    Map<String, ComposerDialobTag> getTags();
    Map<String, ComposerDialobMeta> getMeta();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDialobForm.class) @JsonDeserialize(as = ImmutableComposerDialobForm.class)
  interface ComposerDialobForm extends Serializable {
    String getId();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDialobTag.class) @JsonDeserialize(as = ImmutableComposerDialobTag.class)
  interface ComposerDialobTag extends Serializable {
    String getId(); 
  }
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDialobMeta.class) @JsonDeserialize(as = ImmutableComposerDialobMeta.class)
  interface ComposerDialobMeta extends Serializable {
    String getId();
  }
}
