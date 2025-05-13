package io.resys.thena.structures.fs.actions.create;

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

import io.resys.thena.api.entities.fs.FsDirentAssignment;
import io.resys.thena.api.entities.fs.ImmutableFsDirentAssignment;
import io.resys.thena.api.entities.fs.ThenaFsNewObject;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;



public class NewDirentAssignmentBuilder implements ThenaFsNewObject.NewDirentAssignment {
  private final FsCommitBuilder logger;
  private final String direntId;
  private final Map<String, FsDirentAssignment> allAssignments;
  private final ImmutableFsDirentAssignment.Builder next;
  private boolean built;
  
  public NewDirentAssignmentBuilder(
      FsCommitBuilder logger, 
      String direntId, 
      Map<String, FsDirentAssignment> allAssignments) {
    
    super();
    this.logger = logger;
    this.direntId = direntId;
    this.allAssignments = allAssignments;
    this.next = ImmutableFsDirentAssignment.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        ;
  }
  @Override
  public ThenaFsNewObject.NewDirentAssignment assignee(String assignee) {
    this.next.assignee(assignee);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentAssignment assigneeContact(String assignmeeContact) {
    this.next.assigneeContact(assignmeeContact);
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentAssignment assignmentType(String assignmentType) {
    this.next.assignmentType(assignmentType);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableFsDirentAssignment close() {
    RepoAssert.isTrue(built, () -> "you must call AssignmentChanges.build() to finalize dirent CREATE or UPDATE!");
    
    final var built = next.direntId(direntId).build();
    
    RepoAssert.isTrue(
        this.allAssignments.values().stream()
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
