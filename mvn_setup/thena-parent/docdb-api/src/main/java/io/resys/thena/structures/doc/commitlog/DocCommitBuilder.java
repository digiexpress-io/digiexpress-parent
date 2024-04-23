package io.resys.thena.structures.doc.commitlog;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocCommitTree.DocCommitTreeOperation;
import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.entities.doc.ImmutableDocCommit;
import io.resys.thena.api.entities.doc.ImmutableDocCommitTree;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;



public class DocCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final ImmutableDocCommit.Builder commit;
  private final List<DocCommitTree> trees = new ArrayList<>();
  private final DocCommitLogger logger;
  private final OffsetDateTime createdAt;
  public DocCommitBuilder(String tenantId, DocCommit commit) {
    super();
    this.commitId = commit.getId();
    this.tenantId = tenantId;
    this.commit = ImmutableDocCommit.builder().from(commit);
    this.logger = new DocCommitLogger(tenantId, commit);
    this.createdAt = commit.getCreatedAt();
  }
  public String getTenantId() {
    return tenantId;
  }
  public String getCommitId() {
    return commitId;
  }
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  public DocCommitBuilder add(IsDocObject entity) {
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(DocCommitTreeOperation.ADD)
        .bodyAfter(JsonObject.mapFrom(entity))
        .build());
    this.logger.add(entity);
    return this;
  }
  public DocCommitBuilder merge(IsDocObject previous, IsDocObject next) {
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(DocCommitTreeOperation.ADD)
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  public DocCommitBuilder rm(IsDocObject current) {
    this.trees.add(ImmutableDocCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(DocCommitTreeOperation.REMOVE)
        .bodyBefore(JsonObject.mapFrom(current))
        .bodyAfter(null)
        .build());
    this.logger.remove(current);
    return this;
  }
  public Tuple2<DocCommit, List<DocCommitTree>> close() {
    final var commit = this.commit.commitLog(this.logger.build()).build();
    return Tuple2.of(commit, Collections.unmodifiableList(this.trees));
  }
}
