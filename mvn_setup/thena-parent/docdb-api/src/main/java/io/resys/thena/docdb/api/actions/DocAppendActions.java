package io.resys.thena.docdb.api.actions;

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.models.Message;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface DocAppendActions {
  AppendBuilder appendBuilder();
  
  interface AppendBuilder {
    AppendBuilder head(String projectName); // head GID to what to append
    AppendBuilder id(@Nullable String headGid); // OPTIONAL head GID to what to append
    AppendBuilder externalId(@Nullable String headGid); // OPTIONAL head GID to what to append
    AppendBuilder parent(String versionToModify); // for validations
    AppendBuilder parentIsLatest();

    AppendBuilder append(JsonObject doc);
    AppendBuilder merge(JsonObjectMerge doc);
    AppendBuilder log(JsonObject doc);
    
    AppendBuilder remove();
    AppendBuilder author(String author);
    AppendBuilder message(String message);
    Uni<AppendResultEnvelope> build();
  }
  
  
  @Value.Immutable
  interface AppendResultEnvelope extends ThenaEnvelope {
    String getGid(); // repo/head
    @Nullable
    DocCommit getCommit();
    AppendResultStatus getStatus();
    List<Message> getMessages();
  }
  
  enum AppendResultStatus { OK, ERROR, CONFLICT }
  
}
