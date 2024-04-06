package io.resys.thena.structures.grim.create;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewRemarkBuilder implements ThenaGrimNewObject.NewRemark {
  private final GrimCommitBuilder logger;
  private final @Nullable GrimOneOfRelations relation;
  private final Map<String, GrimRemark> all_remarks;
  private final ImmutableGrimBatchMissions.Builder batch;
  private final String remarkId;
  private final String missionId;
  private ImmutableGrimRemark.Builder next; 
  
  private boolean built;
  
  public NewRemarkBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimRemark> all_remarks) {
    
    super();
    this.missionId = missionId;
    this.logger = logger;
    this.relation = relation;
    this.all_remarks = all_remarks;
    this.remarkId = OidUtils.gen();
    this.next = ImmutableGrimRemark.builder()
        .id(remarkId)
        .missionId(missionId)
        .createdWithCommitId(logger.getCommitId())
        .commitId(logger.getCommitId());
    
    this.batch = ImmutableGrimBatchMissions.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public NewRemark remarkText(String remarkText) {
    RepoAssert.notEmpty(remarkText, () -> "remarkText can't be empty!");
    this.next.remarkText(remarkText);
    return this;
  }
  @Override
  public NewRemark remarkStatus(String remarkStatus) {
    this.next.remarkStatus(remarkStatus);
    return this;
  }
  @Override
  public NewRemark reporterId(String reporterId) {
    this.next.reporterId(reporterId);
    return this;
  }

  @Override
  public NewRemark parentId(String parentId) {
    RepoAssert.isTrue(parentId == null || all_remarks.containsKey(parentId), () -> "Can't find parent remark by id: '" +  parentId + "'!");
    this.next.parentId(parentId);
    return this;
  }


  @Override
  public NewRemark addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, 
        ImmutableGrimOneOfRelations.builder()
        .remarkId(remarkId)
        .relationType(GrimRelationType.REMARK)
        .build(), all_assignments);
    
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }

  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call RemarkChanges.build() to finalize mission CREATE or UPDATE!");
    final var built = next.relation(relation).build();
    
    this.logger.add(built);
    return this.batch.addRemarks(built).build();
  }

}
