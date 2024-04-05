package io.resys.thena.structures.grim.modify;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeRemark;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.RepoAssert;

public class MergeRemarkBuilder implements MergeRemark {

  private final GrimCommitBuilder logger;
  private final ImmutableGrimBatchMissions.Builder batch;
  private final GrimRemark currentRemark; 
  private final ImmutableGrimRemark.Builder nextRemark;
  private boolean built;

  public MergeRemarkBuilder(GrimMissionContainer container, GrimCommitBuilder logger, String missionId, String remarkId) {
    super();
    this.logger = logger;
    this.batch = ImmutableGrimBatchMissions.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentRemark = container.getRemarks().get(remarkId);
    RepoAssert.notNull(currentRemark, () -> "Can't find remark with id: '" + remarkId + "' for mission: '" + missionId + "'!");
    this.nextRemark = ImmutableGrimRemark.builder().from(currentRemark);
  }
  @Override
  public MergeRemark remarkText(String remarkText) {
    this.nextRemark.remarkText(remarkText);
    return this;
  }
  @Override
  public MergeRemark remarkStatus(String remarkStatus) {
    this.nextRemark.remarkStatus(remarkStatus);
    return this;
  }
  @Override
  public MergeRemark reporterId(String reporterId) {
    this.nextRemark.reporterId(reporterId);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call MergeRemark.build() to finalize mission MERGE!");
    
    var nextRemark = this.nextRemark.build();
    final var isModified = !nextRemark.equals(currentRemark);
    if(isModified) {
      nextRemark = ImmutableGrimRemark.builder()
          .from(nextRemark)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentRemark, nextRemark);
      batch.addUpdateRemarks(nextRemark);
    }
    return batch.build();
  }
}
