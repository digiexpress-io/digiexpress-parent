package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.smallrye.mutiny.Uni;

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
  }

  interface Command extends Serializable {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateServiceRevision.class) @JsonDeserialize(as = ImmutableCreateServiceRevision.class)
  interface CreateServiceRevision extends Command {
    String getName();
    String getDescription();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreateServiceRevision.class) @JsonDeserialize(as = ImmutableCreateServiceRevision.class)
  interface CreateProcess extends Command {
    String getServiceRevisionId();
    String getServiceRevisionVersionId();
    String getName();
    String getDesc();
    String getFormId();
    String getFlowId();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerState.class)
  @JsonDeserialize(as = ImmutableComposerState.class)
  interface ComposerState {
    String getName();
    @Nullable
    String getCommit();
    SiteContentType getContentType();
    Map<String, ServiceRevisionDocument> getRevisions();
    Map<String, ServiceDocument> getProcesses();
    Map<String, ServiceReleaseDocument> getReleases();
    Map<String, ServiceConfigDocument> getConfigs();
  }
  
  enum SiteContentType { OK, ERRORS, NOT_CREATED, EMPTY, RELEASE }
}