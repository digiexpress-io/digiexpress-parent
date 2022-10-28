package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.client.api.ServiceDocument.ProcessDocument;
import io.digiexpress.client.api.ServiceDocument.ProcessRevisionDocument;
import io.digiexpress.client.api.ServiceDocument.RefIdValue;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.smallrye.mutiny.Uni;

public interface ServiceComposer {
  CreateBuilder create();
  QueryBuilder query();
  
  interface QueryBuilder {
    Uni<ComposerState> head();
    Uni<ComposerState> release(String releaseId);
  }
  
  interface CreateBuilder {
    Uni<ProcessRevisionDocument> revision(CreateRevision init);
    Uni<ProcessDocument> process(CreateRevisionValue init);
  }

  interface Command extends Serializable {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateRevision.class) @JsonDeserialize(as = ImmutableCreateRevision.class)
  interface CreateRevision extends Command {
    String getName();
    String getDescription();
    List<RefIdValue> getValues();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateRevisionValue.class) @JsonDeserialize(as = ImmutableCreateRevisionValue.class)
  interface CreateRevisionValue extends Command {
    String getRevisionId();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerState.class)
  @JsonDeserialize(as = ImmutableComposerState.class)
  interface ComposerState {
    String getName();
    @Nullable
    String getCommit();
    SiteContentType getContentType();
    Map<String, ProcessRevisionDocument> getRevisions();
    Map<String, ProcessDocument> getProcesses();
    Map<String, ServiceReleaseDocument> getReleases();
    Map<String, ServiceConfigDocument> getConfigs();
  }
  
  enum SiteContentType { OK, ERRORS, NOT_CREATED, EMPTY, RELEASE }
}