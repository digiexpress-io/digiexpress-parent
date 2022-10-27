package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.client.api.ServiceDocument.ProcessDocument;
import io.digiexpress.client.api.ServiceDocument.ProcessRevisionDocument;
import io.digiexpress.client.api.ServiceDocument.RefIdValue;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigValue;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.smallrye.mutiny.Uni;

public interface ServiceComposer {

  CreateBuilder create();
  QueryBuilder query();
  

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

  
  interface QueryBuilder {
    Uni<ComposerState> head();
    Uni<ComposerState> release(String releaseId);
  }
  
  interface CreateBuilder {
    Uni<ComposerState> repo();
    Uni<ProcessRevisionDocument> revision(CreateRevision init);
    Uni<ProcessDocument> process(CreateNextProcess init);
    Uni<ServiceConfigDocument> config(CreateNewConfg init);
  }

  interface Command extends Serializable {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateRevision.class) @JsonDeserialize(as = ImmutableCreateRevision.class)
  interface CreateRevision extends Command {
    String getName();
    Optional<Boolean> getDevMode();
    List<RefIdValue> getValues();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateNextProcess.class) @JsonDeserialize(as = ImmutableCreateNextProcess.class)
  interface CreateNextProcess extends Command {
    String getRevisionId();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableCreateNextProcess.class) @JsonDeserialize(as = ImmutableCreateNextProcess.class)
  interface CreateNewConfg extends Command {
    ServiceConfigValue getStencil();
    ServiceConfigValue getDialob();
    ServiceConfigValue getWrench();
    ServiceConfigValue getService();
  }
}