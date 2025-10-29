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

import java.util.Map;

import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewReference;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableReference;
import io.resys.thena.contract.client.entities.Reference;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewReferenceBuilder implements NewReference {
  private final ContractCommitBuilder logger;
  private final String contractId;
  private final @Nullable String parentId; // for party/coverage references
  private final Map<String, Reference> allReferences;
  private final ImmutableReference.Builder next;
  private boolean built;
  
  public NewReferenceBuilder(
      ContractCommitBuilder logger, 
      String contractId,
      @Nullable ImmutableContractOneOfRelations parentId,
      ImmutablePersistenceUnit currentTx,
      @Nullable ContractContainer savedState) {
    
    super();
    this.logger = logger;
    this.contractId = contractId;
    this.parentId = parentId;
    this.allReferences = allReferences;
    this.next = ImmutableReference.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .createdCommitId(logger.getCommitId())
        .contractId(contractId);
    
    // Set parent relation if provided
    if (parentId != null) {
      // TODO: Set parent relation based on parent type (party/coverage)
    }
  }

  @Override
  public NewReference relations(@Nullable ContractOneOfRelations relations) {
    this.next.relations(relations);
    return this;
  }

  @Override
  public NewReference referenceType(String referenceType) {
    this.next.referenceType(referenceType);
    return this;
  }

  @Override
  public NewReference referenceValue(String referenceValue) {
    this.next.referenceValue(referenceValue);
    return this;
  }

  @Override
  public NewReference referenceBody(@Nullable JsonObject referenceBody) {
    this.next.referenceBody(referenceBody);
    return this;
  }

  @Override
  public void build() {
    this.built = true;
  }

  public ImmutableReference close() {
    RepoAssert.isTrue(built, () -> "you must call NewReference.build() to finalize reference CREATE!");
    
    final var built = next.build();
    
    // Validate uniqueness - no duplicate references with same type/value combination
    RepoAssert.isTrue(
        this.allReferences.values().stream()
        .filter(r -> 
          r.getReferenceType().equals(built.getReferenceType()) &&
          r.getReferenceValue().equals(built.getReferenceValue())
        )
        .count() == 0
        , () -> "can't have duplicate references with same type and value!");

    this.logger.add(built);
    return built;
  }
}