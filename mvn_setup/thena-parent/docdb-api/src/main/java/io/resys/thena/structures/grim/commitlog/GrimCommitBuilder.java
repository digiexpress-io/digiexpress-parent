package io.resys.thena.structures.grim.commitlog;

import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimCommitTree.GrimCommitTreeOperation;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitTree;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonObject;



public class GrimCommitBuilder {
  private final String tenantId;
  private final String commitId;
  private final ImmutableGrimCommit.Builder commit;
  private final ImmutableGrimBatchForOne.Builder next;
  private final GrimCommitLogger logger;
  
  public GrimCommitBuilder(String tenantId, GrimCommit commit) {
    super();
    this.commitId = commit.getCommitId();
    this.tenantId = tenantId;
    this.commit = ImmutableGrimCommit.builder().from(commit);
    this.next = ImmutableGrimBatchForOne.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("");
    this.logger = new GrimCommitLogger(tenantId, commit.getCommitId());
  }
  public String getTenantId() {
    return tenantId;
  }
  public String getCommitId() {
    return commitId;
  }
  public GrimCommitBuilder add(IsGrimObject entity) {
    this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(GrimCommitTreeOperation.ADD)
        .bodyAfter(JsonObject.mapFrom(entity))
        .build());
    this.logger.add(entity);
    return this;
  }
  public GrimCommitBuilder merge(IsGrimObject previous, IsGrimObject next) {
    this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(GrimCommitTreeOperation.ADD)
        .bodyBefore(JsonObject.mapFrom(previous))
        .bodyAfter(JsonObject.mapFrom(next))
        .build());
    this.logger.merge(previous, next);
    return this;
  }
  public GrimCommitBuilder rm(IsGrimObject current) {
    this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .operationType(GrimCommitTreeOperation.REMOVE)
        .bodyBefore(JsonObject.mapFrom(current))
        .bodyAfter(null)
        .build());
    this.logger.remove(current);
    return this;
  }
  public ImmutableGrimBatchForOne close() { 
    return this.next
        .addCommits(this.commit.commitLog(this.logger.build()).build())
        .log("").build();
  }
  
  public GrimCommitBuilder withMissionId(String missionId) {
    this.commit.missionId(missionId);
    return this;
  }
}
