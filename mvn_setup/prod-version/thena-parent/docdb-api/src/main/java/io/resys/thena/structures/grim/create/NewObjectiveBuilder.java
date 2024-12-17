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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.ImmutableGrimMissionData;
import io.resys.thena.api.entities.grim.ImmutableGrimObjective;
import io.resys.thena.api.entities.grim.ImmutableGrimObjectiveTransitives;
import io.resys.thena.api.entities.grim.ImmutableGrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewAssignment;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewGoal;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewObjective;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimRelationType;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class NewObjectiveBuilder implements ThenaGrimNewObject.NewObjective {
  private final GrimCommitBuilder logger;
  private final String missionId;
  private final OffsetDateTime createdAt = OffsetDateTime.now();
  private final ImmutableGrimBatchMissions.Builder batch;
  private final GrimOneOfRelations childRel;
  private final String objectiveId;
  private final ImmutableGrimObjective.Builder objective;
  private final ImmutableGrimMissionData.Builder objectiveMeta;
  private boolean built;
  
  public NewObjectiveBuilder(GrimCommitBuilder logger, String missionId) {
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.objectiveId = OidUtils.gen();
    this.batch = ImmutableGrimBatchMissions.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.objective = ImmutableGrimObjective.builder()
        .createdWithCommitId(logger.getCommitId())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .id(objectiveId)
        .title("")
        .description("")
        ;
    this.childRel = ImmutableGrimOneOfRelations.builder()
        .objectiveId(objectiveId)
        .relationType(GrimRelationType.OBJECTIVE)
        .build();
    this.objectiveMeta = ImmutableGrimMissionData.builder()
        .id(OidUtils.gen())
        .createdWithCommitId(logger.getCommitId())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .relation(childRel);
  }
  @Override
  public void build() {
    this.built = true;
  }

  @Override
  public NewObjective title(String title) {
    this.objective.title(title);
    return this;
  }
  @Override
  public NewObjective description(String description) {
    this.objective.description(description);
    return this;
  }
  @Override
  public NewObjective status(String status) {
    this.objective.objectiveStatus(status);
    return this;
  }
  @Override
  public NewObjective startDate(LocalDate startDate) {
    this.objective.startDate(startDate);
    return this;
  }
  @Override
  public NewObjective dueDate(LocalDate dueDate) {
    this.objective.dueDate(dueDate);
    return this;
  }
  @Override
  public NewObjective addGoal(Consumer<NewGoal> newGoal) {
    final var builder = new NewGoalBuilder(logger, missionId, objectiveId);
    newGoal.accept(builder);
    this.batch.from(builder.close());
    return this;
  }
  @Override
  public NewObjective addAssignees(Consumer<NewAssignment> assignment) {
    final var all_assignments = this.batch.build().getAssignments().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var builder = new NewAssignmentBuilder(logger, missionId, childRel, all_assignments);
    assignment.accept(builder);
    final var built = builder.close();
    this.batch.addAssignments(built);
    return this;
  }
  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call ObjectiveChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var data = this.objectiveMeta.build();
    final var objective = this.objective
        .transitives(ImmutableGrimObjectiveTransitives.builder()
            .createdAt(createdAt)
            .updatedAt(createdAt)
            .build())
        .build();
    
    logger.add(objective);

    
    this.batch.addObjectives(objective);
    
    if(data.getDataExtension() != null) {
      this.batch.addData(data);
      logger.add(data);
    }
    
    return this.batch.build();
  }
}
