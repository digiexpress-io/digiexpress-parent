package io.digiexpress.eveli.client.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.TaskCommands.TaskStatus;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import jakarta.annotation.Nullable;


public interface ProcessCommands {
  ProcessQuery query();
  ProcessStatusBuilder status();
  Process create(InitProcess request);
  void delete(String processId);
  
  interface ProcessQuery {
    Optional<Process> get(String id);
    Optional<Process> getByQuestionnaireId(String id);
    Optional<Process> getByTaskId(String id);
    Page<ProcessEntity> find(String name, List<String> status, String userId, Pageable page);
    List<Process> findAll();
  }

  interface ProcessStatusBuilder {
    void answered(String id);
    void answeredByQuestionnaire(String questionnaireId, String taskId);
    void taskStatusChange(String taskId, TaskStatus taskStatus);
    void inProgress(String id);
    void completed(String id);
    void rejected(String id);
  }
  
  //@Relation(collectionRelation = "processDataList", itemRelation = "processDataList", value = "processDataList" )
  @Value.Immutable
  @JsonSerialize(as = ImmutableProcess.class)
  @JsonDeserialize(as = ImmutableProcess.class)
  interface Process {
    Long getId();
    String getWorkflowName();
    ProcessStatus getStatus();
    String getQuestionnaire();
    @Nullable
    String getTask();
    @Nullable
    String getUserId();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    @Nullable
    String getInputContextId();
    @Nullable
    String getInputParentContextId();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableInitProcess.class)
  @JsonDeserialize(as = ImmutableInitProcess.class)
  interface InitProcess {
    String getIdentity();
    String getWorkflowName();
    Boolean getProtectionOrder();    

    @Nullable
    String getCompanyName();
    @Nullable
    String getFirstName();
    @Nullable
    String getLastName();
    @Nullable
    String getLanguage();
    @Nullable
    String getEmail();
    @Nullable
    String getAddress();

    @Nullable
    String getRepresentativeFirstName();
    @Nullable
    String getRepresentativeLastName();
    @Nullable
    String getRepresentativeIdentity();
    @Nullable
    String getInputContextId();
    @Nullable
    String getInputParentContextId();
  }
  
  enum ProcessStatus {
    CREATED,
    ANSWERING,
    ANSWERED,
    IN_PROGRESS,
    WAITING,
    COMPLETED,
    REJECTED
  }
}
