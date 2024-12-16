package io.resys.thena.structures.grim.create;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.ImmutableGrimAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import jakarta.annotation.Nullable;

public class NewAssignmentBuilder implements ThenaGrimNewObject.NewAssignment {
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
  public NewAssignment assignee(String assignee) {
    this.next.assignee(assignee);
    return this;
  }

  @Override
  public NewAssignment assignmentType(String assignmentType) {
    this.next.assignmentType(assignmentType);
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
