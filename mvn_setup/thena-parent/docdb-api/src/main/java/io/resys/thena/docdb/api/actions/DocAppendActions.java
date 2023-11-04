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
  DocAppendBuilder appendBuilder();
  
  interface DocAppendBuilder {
    DocAppendBuilder repoId(String repoId);
    DocAppendBuilder branchName(String branchName);
    DocAppendBuilder docId(@Nullable String docId); 
    DocAppendBuilder externalId(@Nullable String externalId); // user given unique id 
    DocAppendBuilder parent(String versionToModify);
    DocAppendBuilder parentIsLatest();

    DocAppendBuilder append(JsonObject doc);
    DocAppendBuilder merge(JsonObjectMerge doc);
    DocAppendBuilder log(JsonObject doc);
    
    DocAppendBuilder remove();
    DocAppendBuilder author(String author);
    DocAppendBuilder message(String message);
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
