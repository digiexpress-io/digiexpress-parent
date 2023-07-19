package io.dialob.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormPutResponse;
import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.smallrye.mutiny.Uni;

public interface DialobComposer {

  Uni<ComposerState> get();
  Uni<ComposerDocumentState> get(@Nonnull String idOrName);
  Uni<ComposerDocumentState> get(@Nonnull String id, String rev);
  Uni<ComposerState> importRelease(FormReleaseDocument asset);
  Uni<ComposerReleaseState> create(@Nonnull CreateComposerRelease asset);
  Uni<ComposerDocumentState> create(@Nonnull FormDocument asset);
  Uni<ComposerDocumentState> update(@Nonnull FormDocument asset);
  Uni<ComposerDocumentState> update(@Nonnull UpdateFormRevisionEntry asset);
  Uni<ComposerDocumentState> copyAs(@Nonnull String id, String copyToName);

  // delete anything that has id and version match
  Uni<ComposerState> delete(@Nonnull String id, @Nonnull String version);

  Uni<FormPutResponse> validate(@Nonnull FormDocument asset);
  Uni<FormDocument> apply(@Nonnull FormCommands asset);

  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateComposerRelease.class) @JsonDeserialize(as = ImmutableCreateComposerRelease.class)
  interface CreateComposerRelease extends Serializable {
    String getName();
    String getDescription();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDocumentState.class) @JsonDeserialize(as = ImmutableComposerDocumentState.class)
  interface ComposerReleaseState extends Serializable {
    String getId();
    String getName();
    String getDescription();
    String getHash();
    String getContent(); //BASE 64 GZIP JSON
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDocumentState.class) @JsonDeserialize(as = ImmutableComposerDocumentState.class)
  interface UpdateFormRevisionEntry extends Serializable {
    String getId();
    String getVersion();
    String getRevisionName();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerDocumentState.class) @JsonDeserialize(as = ImmutableComposerDocumentState.class)
  interface ComposerDocumentState extends Serializable {
    FormDocument getForm();
    FormRevisionDocument getRevision();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableComposerState.class) @JsonDeserialize(as = ImmutableComposerState.class)
  interface ComposerState extends Serializable {
    Map<String, FormDocument> getForms();
    Map<String, FormRevisionDocument> getRevs();
    Map<String, FormReleaseDocument> getReleases();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreate.class) @JsonDeserialize(as = ImmutableCreate.class)
  interface Create extends Serializable {
    Form getBody();
  }
   
  
  @Value.Immutable @JsonSerialize(as = ImmutableFormCommands.class) @JsonDeserialize(as = ImmutableFormCommands.class)
  interface FormCommands {
    Form getForm();
    List<FormCommand> getValues();
  }
  
  interface FormCommand extends Serializable { }

  interface ItemCopyCommand extends FormCommand {
    String getItemId();
  }
  
  interface ItemRenameCommand extends FormCommand {
    String getOldId();
    String getNewId();
  }  
  

}
