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
import java.util.function.Consumer;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.contract.client.api.ContractCommitActions.CreateOneContract;
import io.resys.thena.contract.client.api.ContractCommitActions.OneContractEnvelope;
import io.resys.thena.contract.client.api.ImmutableContractContainer;
import io.resys.thena.contract.client.api.ImmutableOneContractEnvelope;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewContract;
import io.resys.thena.contract.client.entities.ImmutableCommit;
import io.resys.thena.contract.client.spi.commitlog.ContractCommitBuilder;
import io.resys.thena.contract.client.spi.create.NewContractBuilder;
import io.resys.thena.contract.client.tables.ContractDb;
import io.resys.thena.contract.client.tables.ContractDbBuilder.PersistenceUnit;
import io.resys.thena.contract.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneContractImpl implements CreateOneContract {

  private final ContractDb state;
  private final String tenantId;
  
  private String author;
  private String message;
  private Consumer<NewContract> contract;
  private Consumer<ContractContainer> handleNewState;
  
  @Override
  public CreateOneContract commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  
  @Override
  public CreateOneContract commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  
  @Override
  public CreateOneContract contract(Consumer<NewContract> addContract) {
    RepoAssert.notNull(addContract, () -> "addContract can't be empty!");
    contract = addContract;
    return this;
  }

  @Override
  public Uni<OneContractEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(contract, () -> "contract can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withTransaction(scope, this::doInTx);
  }
  
  @Override
  public CreateOneContract onNewContract(Consumer<ContractContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }

  private Uni<OneContractEnvelope> doInTx(ContractDb tx) {
    return createRequest(tx)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(CreateOneContractException.class).recoverWithItem(ex -> {
          final CreateOneContractException error = (CreateOneContractException) ex;          
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
    return tx.builder().from(request).persist().onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneContractException("Failed to create contract!", rsp);
      }
      
      final OneContractEnvelope result = ImmutableOneContractEnvelope.builder()
          .repoId(tenantId)
          .contract(ImmutableContractContainer.builder()
            .contract(rsp.getContractInserts().iterator().next())
            .parties(rsp.getPartyInserts())
            .coverages(rsp.getCoverageInserts())
            .addAllReferences(rsp.getReferenceInserts())
            .addAllNotes(rsp.getNoteInserts())
            .addAllCapabilities(rsp.getCapabilityInserts())
            .addAllInvPlans(rsp.getInvPlanInserts())
            .addAllPaymentPlans(rsp.getPaymentPlanInserts())
            .build())
          .addAllMessages(rsp.getCommitLogs().stream().map(log -> ImmutableMessage.builder()
              .exception(log.getException())
              .text(log.getText())
              .build()).toList())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    })
    .onItem().invoke(newState -> {
      if(handleNewState != null) {
        handleNewState.accept(newState.getContract());
      }
    });
  }
  
  private Uni<ImmutablePersistenceUnit> createRequest(ContractDb tx) {
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    ImmutablePersistenceUnit next = start;

    final var logger = new ContractCommitBuilder(tenantId, 
        ImmutableCommit.builder()
          .commitId(OidUtils.genUUID())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .build()
    );
    
    // Generate contract number - could be from sequence or other logic
    final var contractNumber = generateContractNumber();
    
    final var newContract = new NewContractBuilder(logger, contractNumber);
    this.contract.accept(newContract);
    final var created = newContract.close();
    
    final var contractId = created.getContractInserts().iterator().next().getId();
    
    next = ImmutablePersistenceUnit.builder()
        .from(start)
        .from(created)
        .from(logger.withContractId(contractId).close())
        .build();
  
    return Uni.createFrom().item(next);
  }
  
  private String generateContractNumber() {
    // TODO: Implement contract number generation logic
    // Could be sequence-based, date-based, or other business rules
    return "CONTRACT-" + System.currentTimeMillis();
  }
  
  public static class CreateOneContractException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final PersistenceUnit batch;
    
    public CreateOneContractException(String message, PersistenceUnit batch) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", batch.getCommitLogs().stream().map(e -> e.getText()).toList()));
      
      batch.getCommitLogs().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
      this.batch = batch;
    }
    
    public PersistenceUnit getBatch() {
      return batch;
    }
  }
}