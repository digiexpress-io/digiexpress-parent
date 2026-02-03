package io.resys.thena.grim.spi.modify;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeProcess;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.grim.api.GrimCommitActions.ModifyOneProc;
import io.resys.thena.grim.api.GrimCommitActions.OneProcEnvelope;
import io.resys.thena.grim.api.ImmutableOneProcEnvelope;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchMissions;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.grim.spi.builders.InternalMissionContainerQuerySqlImpl;
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
  private String procId;
  private BiConsumer<GrimProcess, MergeProcess> modifyProc;
  private Function<MergeProcess, Uni<?>> onAnyUni;
  private BiConsumer<Optional<GrimMissionContainer>, MergeProcess> onMission;
  
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
    this.procId = RepoAssert.notEmpty(procId, () -> "procId can't be empty!");
    return this;
  }
  @Override
  public ModifyOneProcImpl modifyProc(BiConsumer<GrimProcess, MergeProcess> modifyProc) {
    RepoAssert.notNull(modifyProc, () -> "modifyProc can't be empty!");
    this.modifyProc = modifyProc;
    return this;
  }

  @Override
  public Uni<OneProcEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(modifyProc, () -> "modifyProc can't be empty!");
    RepoAssert.notEmpty(procId, () -> "procId can't be empty!");
    
    final var scope = ImmutableTxScope.builder()
        .commitAuthor(author)
        .commitMessage(message)
        .tenantId(tenantId)
        .build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }
  @Override
  public ModifyOneProc onAnyUni(Function<MergeProcess, Uni<?>> onAnyUni) {
    this.onAnyUni = onAnyUni;
    return this;
  }
  @Override
  public ModifyOneProc onMission(BiConsumer<Optional<GrimMissionContainer>, MergeProcess> onMission) {
    this.onMission = onMission;
    return this;
  }

  private Uni<OneProcEnvelope> doInTx(GrimState tx) {
    return tx.missionProcs().getOneByIdWithLock(procId)
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
    
    return getAnyUni(tx, merger)
      .onItem().transformToUni(junk -> getMissionContainer(tx, merger))
      .onItem().transformToUni(container -> {

        if(onMission != null) {
          onMission.accept(container, merger);
        }

        modifyProc.accept(request, merger);
        if(merger.isSkipped()) {
          return Uni.createFrom().item(
            ImmutableOneProcEnvelope.builder()
              .repoId(tenantId)
              .proc(request)
              .status(CommitResultStatus.OK)
              .build()
          );
        }
    
        final var merged = merger.close();
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
      });
  }

  private Uni<?> getAnyUni(GrimState tx, MergeProcImpl merger) {
    if(this.onAnyUni == null) {
      return Uni.createFrom().voidItem();
    }
    return this.onAnyUni.apply(merger);
  }
  
  private Uni<Optional<GrimMissionContainer>> getMissionContainer(GrimState tx, MergeProcImpl merger) {
    if(this.onMission == null) {
      return Uni.createFrom().item(Optional.empty());
    }
    return new InternalMissionContainerQuerySqlImpl((ThenaSqlDataSource) tx.getDataSource())
        .findOneByQuestionnaireId(merger.getCurrentState().getQuestionnaireId());
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
                  .append("  - not found: ").append(String.join(",", procId))
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
