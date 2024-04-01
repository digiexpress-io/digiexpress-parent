package io.resys.thena.structures.grim.create;

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.RemarkChanges;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger.GrimOpType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewRemarkBuilder implements ThenaGrimChanges.RemarkChanges {
  private final GrimCommitLogger logger;
  private final @Nullable GrimOneOfRelations relation;
  private final Map<String, GrimRemark> all_remarks;
  private ImmutableGrimRemark.Builder next; 
  private boolean built;
  
  public NewRemarkBuilder(
      GrimCommitLogger logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimRemark> all_remarks) {
    
    super();
    this.logger = logger;
    this.relation = relation;
    this.all_remarks = all_remarks;
    this.next = ImmutableGrimRemark.builder()
        .id(OidUtils.gen())
        .missionId(missionId)
        .commitId(logger.getCommitId());
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public RemarkChanges remarkText(String remarkText) {
    RepoAssert.notEmpty(remarkText, () -> "remarkText can't be empty!");
    this.next.remarkText(remarkText);
    return this;
  }
  @Override
  public RemarkChanges remarkStatus(String remarkStatus) {
    this.next.remarkStatus(remarkStatus);
    return this;
  }
  @Override
  public RemarkChanges reporterId(String reporterId) {
    this.next.reporterId(reporterId);
    return this;
  }
  @Override
  public RemarkChanges oneOfRelations(GrimOneOfRelations rels) {
    if(rels != null && rels.getRemarkId() != null) {
      RepoAssert.isTrue(all_remarks.containsKey(rels.getObjectiveId()), () -> "can't find parent remark: '" + rels.getObjectiveId() + "'!");
    }
    
    return this;
  }
  public ImmutableGrimRemark close() {
    RepoAssert.isTrue(built, () -> "you must call RemarkChanges.build() to finalize mission CREATE or UPDATE!");
    final var built = next.relation(relation).build();
    
    this.logger.visit(GrimOpType.ADD, built);
    return built;
  }


}
