package io.resys.thena.grim.spi.actions;

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

import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.grim.api.GrimQueryActions.MissionProcDeleteQuery;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchMissions;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.grim.spi.builders.InternalGrimInsertsImpl.GrimMissionBatchException;
import io.resys.thena.grim.spi.datasource.GrimRegistrySqlImpl;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true, chain = true)
public class MissionProcDeleteQueryImpl implements MissionProcDeleteQuery {
  private final GrimDataSource startingState;
  private final String tenantId;
  
  private String commitAuthor;
  private String commitMessage;
  private String procId;
  
  
  @Override
  public Uni<GrimProcess> deleteOne() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(commitAuthor, () -> "commitAuthor can't be empty!");
    RepoAssert.notEmpty(commitMessage, () -> "commitMessage can't be empty!");
    RepoAssert.notEmpty(procId, () -> "missionId can't be empty!");

    final var scope = ImmutableTxScope.builder()
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage)
        .tenantId(tenantId)
        .build();
    
    return startingState.withGrimTransaction(scope, this::doInTx);
  }
  
  private Uni<GrimProcess> doInTx(GrimState state) {
    return state.missionProcs()
        .getOneById(procId)
        .onItem().transformToUni(batch -> deleteActiveData(state, batch));
  }
  
  public Uni<GrimProcess> deleteActiveData(GrimState state, GrimProcess audit) {
    final ThenaSqlDataSource wrapper = (ThenaSqlDataSource) state.getDataSource();
    RepoAssert.isTrue(wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    
    final var tx = wrapper.getClient();
    final var registry = new GrimRegistrySqlImpl(wrapper.getRegistry());
    final var del_mission = registry.processes().deleteOneById(audit.getId());
    
    final var init = ImmutableGrimBatchMissions.builder()
        .tenantId(wrapper.getTenant().getId())
        .status(BatchStatus.OK)
        .log("")
        .build();
    
    final Uni<GrimBatchMissions> del_mission_uni = Execute.apply(tx, del_mission).onItem()
        .transform(row -> successOutput(init, "Processes deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete processes \r\n" + audit.getId(), e));
   
    return del_mission_uni
        .onFailure(GrimMissionBatchException.class)
        .recoverWithUni((ex) -> {
          final var batchError = (GrimMissionBatchException) ex;
          return tx.rollback().onItem().transform(junk -> batchError.getBatch());
        })
        .map(ignore -> audit);
  }

  private GrimMissionBatchException failOutput(GrimBatchMissions current, String msg, Throwable t) {
    return new GrimMissionBatchException(ImmutableGrimBatchMissions.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build()); 
  }
  
  
  private GrimBatchMissions successOutput(GrimBatchMissions current, String msg) {
    return ImmutableGrimBatchMissions.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }

}
