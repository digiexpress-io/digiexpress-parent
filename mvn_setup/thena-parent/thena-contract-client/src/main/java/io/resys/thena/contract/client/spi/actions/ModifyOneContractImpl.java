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
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.envelope.QueryEnvelopeList;
import io.resys.thena.contract.client.api.ContractCommitActions.ModifyOneContract;
import io.resys.thena.contract.client.api.ContractCommitActions.OneContractEnvelope;
import io.resys.thena.contract.client.api.ImmutableOneContractEnvelope;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeContract;
import io.resys.thena.contract.client.entities.ContractDocType;
import io.resys.thena.contract.client.entities.ImmutableCommit;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.modify.MergeContractBuilder;
import io.resys.thena.contract.client.spi.queries.ContractQueryImpl;
import io.resys.thena.contract.client.tables.ContractDb;
import io.resys.thena.contract.client.tables.ContractDbBuilder.PersistenceUnit;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneContractImpl implements ModifyOneContract {

  private final ContractDb state;
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
    return this.state.withTransaction(scope, this::doInTx);
  }

  private Uni<OneContractEnvelope> doInTx(ContractDb tx) {
    return createRequest(tx)
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


  
  private Uni<OneContractEnvelope> createResponse(ContractDb tx, PersistenceUnit request) {

    // Merge requests
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .log("")
        .status(BatchStatus.OK)
        .from(request);
    
    // Patch all in current TX
    return tx.builder().from(start.build()).persist().onItem().transformToUni(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyOneContractException("Failed to modify contract!", rsp);
      }

      return ContractQueryImpl.of(tx)
          .addContractId(this.contractId)
          .excludeDocs(ContractDocType.COMMIT)
          .findAll()
          .onItem().transform(container -> {
            final var item = container.getObjects().iterator().next();
            final OneContractEnvelope env = ImmutableOneContractEnvelope.builder()
              .repoId(tenantId)
              .contract(item)
              .addAllMessages(container.getMessages())
              .status(BatchStatus.mapStatus(rsp.getStatus()))
              .build();
            return env;
          });
            
    });
  }
  
  private Uni<PersistenceUnit> createRequest(ContractDb tx) {
    return ContractQueryImpl.of(tx)
      .addContractId(this.contractId)
      .lockForUpdate()
      .excludeDocs(ContractDocType.COMMIT)
      .findAll().onItem()
      .transform(container -> createRequest(tx, container));
  }
  
  private ImmutablePersistenceUnit createRequest(ContractDb tx, QueryEnvelopeList<ContractContainer> env) {
    RepoAssert.isTrue(env.getObjects().size() == 1, () -> "Contract container must be grouped by contracts, one contract per container!");
    
    final var contract = env.getObjects().get(0).getContract();
    final var contractId = contract.getId();
    
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    
    ImmutablePersistenceUnit next = start;    
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
    
    final var mergeContract = new MergeContractBuilder(env.getObjects().get(0), logger);
    this.contract.accept(mergeContract);
    final var created = mergeContract.close();
    
    next = ImmutablePersistenceUnit.builder()
        .from(start)
        .from(created)
        .from(logger.withContractId(contractId).close())
        .build();
    return next;
  }
  
  public static class ModifyOneContractException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final PersistenceUnit batch;
    
    public ModifyOneContractException(String message, PersistenceUnit batch) {
      super(message);
      this.batch = batch;
    }
    
    public PersistenceUnit getBatch() {
      return batch;
    }
  }
}