package io.resys.thena.docdb.spi.doc.commits;

import java.util.Optional;

import io.resys.thena.docdb.api.actions.CommitActions.JsonObjectMerge;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.spi.DocDbInserts.DocBatch;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.experimental.Accessors;

public interface DocCommitBatchBuilder {
  DocCommitBatchBuilder commitAuthor(String commitAuthor);
  DocCommitBatchBuilder commitMessage(String commitMessage);
  DocCommitBatchBuilder toBeMerged(JsonObjectMerge toBeMerged);
  DocCommitBatchBuilder toBeInserted(JsonObject toBeInserted);
  DocCommitBatchBuilder toBeRemoved(boolean toBeRemoved);
  DocBatch build();

  
  @lombok.Data @lombok.Builder(toBuilder = true) @Accessors(fluent = false)
  public static class DocCommitState {
    private final Repo repo;
    private final String branchId;
    private final String branchName;
    private final String docId;
    private final String externalId;
    @Builder.Default private final Optional<Doc> doc = Optional.empty();
    @Builder.Default private final Optional<DocBranch> branch = Optional.empty();
    @Builder.Default private final Optional<DocCommit> commit = Optional.empty();
  }
}
