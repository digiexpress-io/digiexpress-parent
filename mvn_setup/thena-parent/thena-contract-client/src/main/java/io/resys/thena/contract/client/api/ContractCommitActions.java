package io.resys.thena.contract.client.api;

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

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeContract;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewContract;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ContractCommitActions {
  
  CreateOneContract createOneContract();
  ModifyOneContract modifyOneContract();
  

  interface CreateOneContract {
    CreateOneContract commitAuthor(String author);
    CreateOneContract commitMessage(String message);
    CreateOneContract contract(Consumer<NewContract> addContract);
    CreateOneContract onNewContract(Consumer<ContractContainer> handleNewState);
    Uni<OneContractEnvelope> build();
  }
  
  interface ModifyOneContract {
    ModifyOneContract commitAuthor(String author);
    ModifyOneContract commitMessage(String message);
    ModifyOneContract contractId(String contractId);
    ModifyOneContract modifyContract(Consumer<MergeContract> modifyContract);
    Uni<OneContractEnvelope> build();
  }
  
  
  interface ModifyManyContracts {
    ModifyManyContracts commitAuthor(String author);
    ModifyManyContracts commitMessage(String message);
    ModifyManyContracts modifyMission(String missionId, Consumer<MergeContract> mergeMission);
    
    Uni<ManyContractsEnvelope> build();
  }
  
  interface CreateManyContract {
    CreateManyContract commitAuthor(String author);
    CreateManyContract commitMessage(String message);
    CreateManyContract addMission(Consumer<NewContract> addMission);
    Uni<ManyContractsEnvelope> build();
  }
  
  @Value.Immutable
  interface ManyContractsEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    @Nullable String getLog();
    @Nullable List<ContractContainer> getContracts();
  }
  
  
  @Value.Immutable
  interface OneContractEnvelope extends ThenaEnvelope {
    String getRepoId();
    CommitResultStatus getStatus();
    List<Message> getMessages();
    
    @Nullable ContractContainer getContract();

  }
}
