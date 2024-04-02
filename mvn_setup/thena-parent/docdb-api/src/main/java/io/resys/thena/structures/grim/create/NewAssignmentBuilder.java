package io.resys.thena.structures.grim.create;

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.ImmutableGrimAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.AssignmentChanges;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewAssignmentBuilder implements ThenaGrimChanges.AssignmentChanges {
  private final GrimCommitBuilder logger;
  private final String missionId;
  private final @Nullable GrimOneOfRelations relation;
  private final Map<String, GrimAssignment> allAssignments;
  private final ImmutableGrimAssignment.Builder next;
  private boolean built;
  
  public NewAssignmentBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimAssignment> allAssignments) {
    
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.relation = relation;
    this.allAssignments = allAssignments;
    this.next = ImmutableGrimAssignment.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        ;
  }
  @Override
  public AssignmentChanges assignee(String assignee) {
    this.next.assignee(assignee);
    return this;
  }

  @Override
  public AssignmentChanges assignmentType(String assignmentType) {
    this.next.assignmentType(assignmentType);
    return this;
  }
  @Override
  public AssignmentChanges oneOfRelations(GrimOneOfRelations rels) {
    this.next.relation(rels);
    return this;
  }  
  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableGrimAssignment close() {
    RepoAssert.isTrue(built, () -> "you must call AssignmentChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var built = next.missionId(missionId).relation(relation).build();
    
    RepoAssert.isTrue(
        this.allAssignments.values().stream()
        .filter(a -> 
          (a.getRelation() == null && relation == null) ||
          (a.getRelation() != null && a.getRelation().equals(relation))
        )
        .filter(a -> 
          a.getAssignmentType().equals(built.getAssignmentType()) &&
          a.getAssignee().equals(built.getAssignee())
        )
        .count() == 0
        , () -> "can't have duplicate assignments!");

    this.logger.add(built);
    return built;
  }


}
