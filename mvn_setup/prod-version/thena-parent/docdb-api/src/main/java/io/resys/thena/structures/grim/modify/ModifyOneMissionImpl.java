package io.resys.thena.structures.grim.modify;

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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;

import io.resys.thena.api.actions.GrimCommitActions.ModifyOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.actions.ImmutableOneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.GrimInserts.GrimBatchMissions;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyOneMissionImpl implements ModifyOneMission {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private String missionId;
  private Consumer<MergeMission> mission;
  
  @Override
  public ModifyOneMission commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public ModifyOneMission commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public ModifyOneMission missionId(String missionId) {
    this.missionId = RepoAssert.notEmpty(missionId, () -> "missionId can't be empty!");
    return this;
  }
  @Override
  public ModifyOneMission modifyMission(Consumer<MergeMission> modifyMission) {
    RepoAssert.notNull(modifyMission, () -> "modifyMission can't be empty!");
    mission = modifyMission;
    return this;
  }
  @Override
  public Uni<OneMissionEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(mission, () -> "modifyMission can't be empty!");
    RepoAssert.notEmpty(missionId, () -> "missions can't be empty!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }

  private Uni<OneMissionEnvelope> doInTx(GrimState tx) {
    return createRequest(tx)
        .collect().asList()
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyOneMissionException.class).recoverWithItem(ex -> {
          final ModifyOneMissionException error = (ModifyOneMissionException) ex;          
          return ImmutableOneMissionEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'").append(" is rejected.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(error.getMessage())
                  .toString())
                .exception(error)
                .build())
            .status(CommitResultStatus.ERROR)
          .build();
        });
  }

  private OneMissionEnvelope validateRequest(GrimState tx, List<GrimBatchMissions> request) {
    if(request.size() != 1) {
      return ImmutableOneMissionEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'")
                  .append(" is rejected.")
                  .append(" Could not find mission, expected: '1' but found: '").append(request.size()).append("'!\r\n")
                  .append("  - not found: ").append(String.join(",", missionId))
                  .toString())
                .build())
            .status(CommitResultStatus.ERROR)
            .build();
    }
    return null;
  }
  
  private Uni<OneMissionEnvelope> createResponse(GrimState tx, List<GrimBatchMissions> request) {
    final var isErrors = validateRequest(tx, request);
    if(isErrors != null) {
      return Uni.createFrom().item(isErrors);
    }
    
    // Merge requests
    final var start = ImmutableGrimBatchMissions.builder()
        .tenantId(tenantId)
        .log("")
        .status(BatchStatus.OK);
    
    
    request.forEach(r -> start.from(r));
    
    // Patch all in current TX
    return tx.insert().batchMany(start.build()).onItem().transformToUni(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyOneMissionException("Failed to modify missions!", rsp);
      }

      return tx.query().missions()
          .missionId(this.missionId)
          .excludeDocs(GrimDocType.GRIM_COMMANDS, GrimDocType.GRIM_COMMIT_VIEWER)
          .findAll().collect().asList()
          .onItem().transform(container -> {
            
            return ImmutableOneMissionEnvelope.builder()
              .repoId(tenantId)
              .mission(container.iterator().next().getMission())
              .addAllMessages(rsp.getMessages())
              .status(BatchStatus.mapStatus(rsp.getStatus()))
              .build();
          });
            
    });
  }
  
  private Multi<GrimBatchMissions> createRequest(GrimState tx) {
    return tx.query().missions()
    .missionId(this.missionId)
    .excludeDocs(GrimDocType.GRIM_COMMANDS, GrimDocType.GRIM_COMMIT_VIEWER)
    .findAll().onItem().transform(labels -> createRequest(tx, labels));
  }
  
  
  private GrimBatchMissions createRequest(GrimState tx, GrimMissionContainer container) {
    RepoAssert.isTrue(container.getMissions().size() == 1, () -> "Mission container must be grouped by missions, one mission per container!");
    
    final var missionId =  container.getMissions().keySet().iterator().next();
    
    final var start = ImmutableGrimBatchMissions.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    
    ImmutableGrimBatchMissions next = start;    
    final var logger = new GrimCommitBuilder(tenantId, 
        ImmutableGrimCommit.builder()
          .commitId(OidUtils.gen())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .build()
    );
    
    final var mergeMission = new MergeMissionBuilder(container, logger);
    this.mission.accept(mergeMission);
    final var created = mergeMission.close();
    
    next = ImmutableGrimBatchMissions.builder()
        .from(start)
        .from(created)
        .from(logger.withMissionId(missionId).close())
        .build();
    return next;
  }
  
  
  public static class ModifyOneMissionException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final GrimBatchMissions batch;
    public ModifyOneMissionException(String message, GrimBatchMissions batch) {
      super(message);
      this.batch = batch;
    }
    public GrimBatchMissions getBatch() {
      return batch;
    }
  }
}
