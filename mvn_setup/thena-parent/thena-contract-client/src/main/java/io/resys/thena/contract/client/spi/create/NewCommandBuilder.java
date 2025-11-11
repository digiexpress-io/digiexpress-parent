package io.resys.thena.contract.client.spi.create;

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

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewCommand;
import io.resys.thena.contract.client.entities.Command;
import io.resys.thena.contract.client.entities.ImmutableCommand;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewCommandBuilder implements NewCommand {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final Map<String, Command> allCommands;
  private final ImmutableCommand.Builder next;
  private boolean built;
  
  public NewCommandBuilder(
      ContractCommitBuilder logger, 
      String contractId, 
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.next = ImmutableCommand.builder()
        .id(OidUtils.genUUID())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId)
        .externalId(Optional.empty())
        .commandTargetDate(Optional.empty())
        .commandDescription(Optional.empty())
        .commandError(Optional.empty());
    
  
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
      )
      .flatMap(e -> e)
      .filter(saved -> !deletes.contains(saved.getId()))
      .filter(saved -> !updates.contains(saved.getId()))
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public NewCommand externalId(@Nullable String externalId) {
    this.next.externalId(Optional.ofNullable(externalId));
    return this;
  }

  @Override
  public NewCommand commandBody(JsonObject commandBody) {
    this.next.commandBody(commandBody);
    return this;
  }

  @Override
  public NewCommand commandStatus(String commandStatus) {
    this.next.commandStatus(commandStatus);
    return this;
  }

  @Override
  public NewCommand commandType(String commandType) {
    this.next.commandType(commandType);
    return this;
  }

  @Override
  public NewCommand commandTargetDate(@Nullable LocalDate commandTargetDate) {
    this.next.commandTargetDate(Optional.ofNullable(commandTargetDate));
    return this;
  }

  @Override
  public NewCommand commandDescription(@Nullable String commandDescription) {
    this.next.commandDescription(Optional.ofNullable(commandDescription));
    return this;
  }

  @Override
  public NewCommand commandError(@Nullable JsonObject commandError) {
    this.next.commandError(Optional.ofNullable(commandError));
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableCommand close() {
    RepoAssert.isTrue(built, () -> "you must call NewCommand.build() to finalize command CREATE!");
    
    final var built = next.build();
    
    this.logger.add(built);
    return built;
  }
}