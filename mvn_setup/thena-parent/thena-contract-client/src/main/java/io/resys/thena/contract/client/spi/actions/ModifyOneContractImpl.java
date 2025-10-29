package io.resys.thena.contract.client.spi.actions;

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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.contract.client.api.ContractCommitActions.ModifyOneContract;
import io.resys.thena.contract.client.api.ContractCommitActions.OneContractEnvelope;
import io.resys.thena.contract.client.api.ImmutableOneContractEnvelope;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeContract;
import io.resys.thena.contract.client.entities.ContractDocType;
import io.resys.thena.contract.client.entities.ImmutableCommit;
import io.resys.thena.contract.client.spi.ContractDataSource;
import io.resys.thena.contract.client.spi.ContractDataSource.ContractState;
import io.resys.thena.contract.client.spi.commitlog.ContractBatchOperations;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.modify.MergeContractBuilder;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneContractImpl implements ModifyOneContract {

  private final ContractDataSource state;
  private final String tenantId;
  
  private String author;
  private String message;
  private String contractId;
  private Consumer<MergeContract> contract;
  
  @Override
  public ModifyOneContract commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  
  @Override
  public ModifyOneContract commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  
  @Override
  public ModifyOneContract contractId(String contractId) {
    this.contractId = RepoAssert.notEmpty(contractId, () -> "contractId can't be empty!");
    return this;
  }
  
  @Override
  public ModifyOneContract modifyContract(Consumer<MergeContract> modifyContract) {
    RepoAssert.notNull(modifyContract, () -> "modifyContract can't be empty!");
    contract = modifyContract;
    return this;
  }
  
  @Override
  public Uni<OneContractEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(contract, () -> "modifyContract can't be empty!");
    RepoAssert.notEmpty(contractId, () -> "contractId can't be empty!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withContractTransaction(scope, this::doInTx);
  }

  private Uni<OneContractEnvelope> doInTx(ContractState tx) {
    return createRequest(tx)
        .collect().asList()
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyOneContractException.class).recoverWithItem(ex -> {
          final ModifyOneContractException error = (ModifyOneContractException) ex;          
          return ImmutableOneContractEnvelope.builder()
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

  private OneContractEnvelope validateRequest(ContractState tx, List<ContractBatchOperations> request) {
    if(request.size() != 1) {
      return ImmutableOneContractEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'")
                  .append(" is rejected.")
                  .append(" Could not find contract, expected: '1' but found: '").append(request.size()).append("'!\\r\\n")
                  .append("  - not found: ").append(String.join(",", contractId))
                  .toString())
                .build())
            .status(CommitResultStatus.ERROR)
            .build();
    }
    return null;
  }
  
  private Uni<OneContractEnvelope> createResponse(ContractState tx, List<ContractBatchOperations> request) {
    final var isErrors = validateRequest(tx, request);
    if(isErrors != null) {
      return Uni.createFrom().item(isErrors);
    }
    
    // Merge requests
    final var start = ContractBatchOperations.builder()
        .tenantId(tenantId)
        .log("")
        .status(BatchStatus.OK);
    
    request.forEach(r -> start.from(r));
    
    // Patch all in current TX
    return tx.batchMany(start.build()).onItem().transformToUni(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyOneContractException("Failed to modify contract!", rsp);
      }

      return tx.contracts()
          .contractId(this.contractId)
          .excludeDocs(ContractDocType.COMMIT)
          .findAll().collect().asList()
          .onItem().transform(container -> {
            final var item = container.iterator().next();
            return ImmutableOneContractEnvelope.builder()
              .repoId(tenantId)
              .contract(item.getContract())
              .parties(item.getParties())
              .coverages(item.getCoverages())
              .addAllReferences(item.getReferences())
              .addAllNotes(item.getNotes())
              .addAllCapabilities(item.getCapabilities())
              .addAllInvPlans(item.getInvPlans())
              .addAllPaymentPlans(item.getPaymentPlans())
              .addAllMessages(rsp.getMessages())
              .status(BatchStatus.mapStatus(rsp.getStatus()))
              .build();
          });
            
    });
  }
  
  private Multi<ContractBatchOperations> createRequest(ContractState tx) {
    return tx.contracts()
    .contractId(this.contractId)
    .lockForUpdate()
    .excludeDocs(ContractDocType.COMMIT)
    .findAll().onItem().transform(container -> createRequest(tx, container));
  }
  
  private ContractBatchOperations createRequest(ContractState tx, ContractContainer container) {
    RepoAssert.isTrue(container.getContracts().size() == 1, () -> "Contract container must be grouped by contracts, one contract per container!");
    
    final var contract = container.getContracts().values().iterator().next();
    final var contractId = contract.getId();
    
    final var start = ContractBatchOperations.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    
    ContractBatchOperations next = start;    
    final var logger = new ContractCommitBuilder(tenantId, 
        ImmutableCommit.builder()
          .commitId(OidUtils.gen())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .parentCommitId(Optional.ofNullable(contract.getUpdatedTreeCommitId()).orElse(contract.getCommitId()))
          .build()
    );
    
    final var mergeContract = new MergeContractBuilder(container, logger);
    this.contract.accept(mergeContract);
    final var created = mergeContract.close();
    
    next = ContractBatchOperations.builder()
        .from(start)
        .from(created)
        .from(logger.withContractId(contractId).close())
        .build();
    return next;
  }
  
  public static class ModifyOneContractException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final ContractBatchOperations batch;
    
    public ModifyOneContractException(String message, ContractBatchOperations batch) {
      super(message);
      this.batch = batch;
    }
    
    public ContractBatchOperations getBatch() {
      return batch;
    }
  }
}