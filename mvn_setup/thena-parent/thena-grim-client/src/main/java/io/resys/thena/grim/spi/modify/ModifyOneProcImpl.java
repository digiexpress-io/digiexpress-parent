package io.resys.thena.grim.spi.modify;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.function.Consumer;

import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeProc;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.grim.api.GrimCommitActions.ModifyOneProc;
import io.resys.thena.grim.api.GrimCommitActions.OneProcEnvelope;
import io.resys.thena.grim.api.ImmutableOneProcEnvelope;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchMissions;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneProcImpl implements ModifyOneProc {
  private final GrimDataSource state;
  private final String tenantId;
  
  private String author;
  private String message;
  private String missionId;
  private Consumer<MergeProc> mission;
  
  @Override
  public ModifyOneProcImpl commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public ModifyOneProcImpl commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public ModifyOneProcImpl procId(String procId) {
    this.missionId = RepoAssert.notEmpty(procId, () -> "procId can't be empty!");
    return this;
  }
  @Override
  public ModifyOneProcImpl modifyProc(Consumer<MergeProc> modifyProc) {
    RepoAssert.notNull(modifyProc, () -> "modifyProc can't be empty!");
    mission = modifyProc;
    return this;
  }

  @Override
  public Uni<OneProcEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(mission, () -> "modifyMission can't be empty!");
    RepoAssert.notEmpty(missionId, () -> "missions can't be empty!");
    
    final var scope = ImmutableTxScope.builder()
        .commitAuthor(author)
        .commitMessage(message)
        .tenantId(tenantId)
        .build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }

  private Uni<OneProcEnvelope> doInTx(GrimState tx) {
    return tx.missionProcs().getOneById(missionId)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyOneProcException.class).recoverWithItem(ex -> {
          final ModifyOneProcException error = (ModifyOneProcException) ex;          
          return ImmutableOneProcEnvelope.builder()
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
  

  private Uni<OneProcEnvelope> createResponse(GrimState tx, GrimProcess request) {
    final var isErrors = validateRequest(tx, request);
    if(isErrors != null) {
      return Uni.createFrom().item(isErrors);
    }
    
    final var merger = new MergeProcImpl(request);
    mission.accept(merger);
    final var merged = merger.close();
    
    
    // Merge requests
    final var start = ImmutableGrimBatchMissions.builder()
      .tenantId(tenantId)
      .log("")
      .addUpdateProcs(merged)
      .status(BatchStatus.OK);
    
    // Patch all in current TX
    return tx.batchMany(start.build()).onItem().transform(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyOneProcException("Failed to modify proc!", rsp);
      }

      return ImmutableOneProcEnvelope.builder()
          .repoId(tenantId)
          .proc(merged)
          .status(CommitResultStatus.OK)
          .build();
            
    });
  }
  
  private OneProcEnvelope validateRequest(GrimState tx, GrimProcess request) {
    if(request == null) {
      return ImmutableOneProcEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'")
                  .append(" is rejected.")
                  .append(" Could not find proc, expected: '1' but found: non!\r\n")
                  .append("  - not found: ").append(String.join(",", missionId))
                  .toString())
                .build())
            .status(CommitResultStatus.ERROR)
            .build();
    }
    return null;
  }
  
  public static class ModifyOneProcException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final GrimBatchMissions batch;
    public ModifyOneProcException(String message, GrimBatchMissions batch) {
      super(message);
      this.batch = batch;
    }
    public GrimBatchMissions getBatch() {
      return batch;
    }
  }
}
