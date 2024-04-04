package io.resys.thena.structures.grim.create;

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRemark;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewRemarkBuilder implements ThenaGrimNewObject.NewRemark {
  private final GrimCommitBuilder logger;
  private final @Nullable GrimOneOfRelations relation;
  private final Map<String, GrimRemark> all_remarks;
  private ImmutableGrimRemark.Builder next; 
  private boolean built;
  
  public NewRemarkBuilder(
      GrimCommitBuilder logger, 
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
        .createdWithCommitId(logger.getCommitId())
        .commitId(logger.getCommitId());
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
  public ImmutableGrimRemark close() {
    RepoAssert.isTrue(built, () -> "you must call RemarkChanges.build() to finalize mission CREATE or UPDATE!");
    final var built = next.relation(relation).build();
    
    this.logger.add(built);
    return built;
  }


}
