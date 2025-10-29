package io.resys.thena.contract.client.spi.modify;

import java.util.Collections;

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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableReference;
import io.resys.thena.contract.client.entities.Reference;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class MergeReferenceBuilder implements MergeReference {

  private final ContractCommitBuilder logger;
  private final ImmutablePersistenceUnit.Builder batch;
  private final Reference currentReference; 
  private final ImmutableReference.Builder nextReference;
  private final Map<String, Reference> allReferences;
  private boolean built;

  public MergeReferenceBuilder(ContractContainer container, ContractCommitBuilder logger, String contractId, String referenceId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    super();
    this.logger = logger;
    this.batch = ImmutablePersistenceUnit.builder().tenantId(logger.getTenantId()).log("").status(BatchStatus.OK);
    this.currentReference = container.getReferences().stream()
        .filter(r -> r.getId().equals(referenceId))
        .findFirst()
        .orElse(null);
    RepoAssert.notNull(currentReference, () -> "Can't find reference with id: '" + referenceId + "' for contract: '" + contractId + "'!");
    this.nextReference = ImmutableReference.builder().from(currentReference);
    
    final var updates = currentTx.getReferenceUpdates().stream().map(e -> e.getId()).toList();
    final var deletes = currentTx.getReferenceDeletes().stream().map(e -> e.getId()).toList();
    
    this.allReferences = Stream.of(
        // from current TX
        currentTx.getReferenceInserts().stream(),
        currentTx.getReferenceUpdates().stream(),
        
        // previously saved
        Optional.ofNullable(savedState)
          .map(saved -> saved.getReferences())
          .orElse(Collections.emptyList())
          .stream()
          .filter(saved -> !deletes.contains(saved.getId()))
          .filter(saved -> !updates.contains(saved.getId()))
      )
      .flatMap(e -> e)
      .collect(Collectors.toMap(e -> e.getId(), e -> e));
  }

  @Override
  public MergeReference referenceValue(String referenceValue) {
    this.nextReference.referenceValue(referenceValue);
    return this;
  }
  @Override
  public MergeReference referenceType(String referenceType) {
    this.nextReference.referenceType(referenceType);
    return this;
  }
  @Override
  public MergeReference referenceBody(JsonObject referenceBody) {
    this.nextReference.referenceBody(referenceBody);
    return this;
  }
  @Override
  public MergeReference relations(ContractOneOfRelations relations) {
    this.nextReference.relations(relations);
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }

  public ImmutablePersistenceUnit close() {
    RepoAssert.isTrue(built, () -> "you must call MergeReference.build() to finalize reference MERGE!");
    
    var nextReference = this.nextReference.build();
    final var isModified = !nextReference.equals(currentReference);
    if(isModified) {
      nextReference = ImmutableReference.builder()
          .from(nextReference)
          .commitId(this.logger.getCommitId())
          .build();
      logger.merge(currentReference, nextReference);
      batch.addReferenceUpdates(nextReference);
    }
    return batch.build();
  }
}