package io.resys.thena.contract.client.spi.modify;

/*-
 * #%L
 * thena-contract-client
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeCommand;
import io.resys.thena.contract.client.entities.Command;
import io.resys.thena.contract.client.entities.ImmutableCommand;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class MergeCommandBuilder implements MergeCommand {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final Command currentCommand; 
  private final ImmutableCommand.Builder nextCommand;
  private final Map<String, Command> allCommands;
  private boolean built;

  public MergeCommandBuilder(ContractContainer container, ContractCommitBuilder logger, String contractId, String commandId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentCommand = container.getCommands().stream()
        .filter(c -> c.getId().equals(commandId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentCommand, () -> "Can't find command with id: '" + commandId + "' for contract: '" + contractId + "'!");
    this.nextCommand = ImmutableCommand.builder().from(currentCommand);
    
    final var updates = currentTx.getCommandUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getCommandDeletes().stream().map(e -> e.getId()).toList();
    
    this.allCommands = Stream.of(
        // from current TX
        currentTx.getCommandInserts().stream(),
        currentTx.getCommandUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getCommands())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public MergeCommand externalId(@Nullable String externalId) {
    this.nextCommand.externalId(Optional.ofNullable(externalId));
    return this;
  }

  @Override
  public MergeCommand commandBody(JsonObject commandBody) {
    this.nextCommand.commandBody(commandBody);
    return this;
  }

  @Override
  public MergeCommand commandStatus(String commandStatus) {
    this.nextCommand.commandStatus(commandStatus);
    return this;
  }

  @Override
  public MergeCommand commandType(String commandType) {
    this.nextCommand.commandType(commandType);
    return this;
  }

  @Override
  public MergeCommand commandTargetDate(@Nullable LocalDate commandTargetDate) {
    this.nextCommand.commandTargetDate(Optional.ofNullable(commandTargetDate));
    return this;
  }

  @Override
  public MergeCommand commandDescription(@Nullable String commandDescription) {
    this.nextCommand.commandDescription(Optional.ofNullable(commandDescription));
    return this;
  }

  @Override
  public MergeCommand commandError(@Nullable JsonObject commandError) {
    this.nextCommand.commandError(Optional.ofNullable(commandError));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeCommand.build() to finalize command MERGE!");
    
    var nextCommand = this.nextCommand.build();
    final var isModified = !nextCommand.equals(currentCommand);
    if(isModified) {
      nextCommand = ImmutableCommand.builder()
          .from(nextCommand)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentCommand, nextCommand);
      batch.addCommandUpdates(nextCommand);
    }
    return batch.build();
  }
}