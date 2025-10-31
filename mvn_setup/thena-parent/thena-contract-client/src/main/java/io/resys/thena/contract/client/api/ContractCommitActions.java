package io.resys.thena.contract.client.api;

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.contract.client.api.ThenaContractContainers.ContractContainer;
import io.resys.thena.contract.client.api.ThenaContractMergeObject.MergeContract;
import io.resys.thena.contract.client.api.ThenaContractNewObject.NewContract;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ContractCommitActions {

  interface CreateOneContract {
    CreateOneContract commitAuthor(String author);
    CreateOneContract commitMessage(String message);
    CreateOneContract contract(Consumer<NewContract> addContract);
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
