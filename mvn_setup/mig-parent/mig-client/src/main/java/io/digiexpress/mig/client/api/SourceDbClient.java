package io.digiexpress.mig.client.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

public interface SourceDbClient {
  SourceDbTaskQuery taskQuery();
  SourceDbDialobQuery dialobQuery();
  
  interface SourceDbDialobQuery {
    SourceDbDialobQuery includeFromQuestionnaires(List<String> questionnaires);
    SourceDbDialobQuery includeFrom(List<? extends FormFilter> formMeta);
    Uni<SourceDbDialob> findAll();
  }
  
  interface SourceDbTaskQuery {
    Uni<SourceDbTasks> findAll();
  }
  
  
  @Value.Immutable
  interface FormFilter {
    Optional<String> getFormId();
    String getFormTag();
    String getFormName();
  }
  
  @Value.Immutable
  interface SourceDbDialob {
    List<SourceDbForm> getForms();
    List<SourceDbFormDocument> getFormDocument();
    List<SourceDbFormRev> getFormRev();
    List<SourceDbQuestionnaire> getQuestionnaires();
  }
  
  @Value.Immutable
  interface SourceDbForm {
    String getName();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    String getLatest_form_id();
    String getTenant_id();
    
    Optional<String> getLabel();
  }
  @Value.Immutable
  interface SourceDbFormDocument {
    String getId();
    int getRev();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    Optional<JsonObject> getData();
    String getTenant_id();
  }
  @Value.Immutable
  interface SourceDbFormRev {
    String getForm_name();
    String getName();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    String getForm_document_id();
    String getTenant_id();
    
    Optional<String> getDescription();
    Optional<String> getType();
    Optional<String> getRef_name();
  }
  
  @Value.Immutable
  interface SourceDbQuestionnaire {
    String getId();
    int getRev();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();
    String getForm_document_id();
    String getTenant_id();
    
    Optional<JsonObject> getData();
    Optional<String> getStatus();
    Optional<String> getOwner();
  }
  
  @Value.Immutable
  interface SourceDbTasks {
    Map<Long, SourceDbTask> getTasks();
    Map<Long, List<SourceDbTaskRole>> getRoles();
    Map<Long, List<SourceDbTaskKeywords>> getKeywords();
    Map<Long, List<SourceDbTaskComment>> getComments();
    Map<Long, List<SourceDbTaskLink>> getLinks();
    Map<Long, List<SourceDbTaskAccess>> getAccess();
    Map<Long, SourceDbTaskProcess> getProcesses();
    Map<Long, SourceDbTaskWorkflow> getWorkflows();
  }

  @Value.Immutable
  interface SourceDbTaskWorkflow {
    long getId();
    String getName();
    LocalDateTime getUpdated();
    String getForm_name();
    String getForm_tag();
    
    Optional<LocalDate> getEnd_date();
    Optional<String> getFlow_name();
    Optional<String> getForm_id();
    Optional<LocalDate> getStart_date();
  }
  
  @Value.Immutable
  interface SourceDbTaskProcess {
    long getId();
    LocalDateTime getCreated();
    LocalDateTime getUpdated();    
    String getWorkflow_name();
    
    Optional<String> getStatus();
    Optional<String> getQuestionnaire_id();
    Optional<Long> getTask_id();
    Optional<String> getUser_id();
    Optional<String> getInput_context_id();
    Optional<String> getInput_parent_context_id();
  }
  
  @Value.Immutable
  interface SourceDbTask {
    long getId();
    LocalDateTime getCreated();
    int getPriority();
    int getStatus();
    LocalDateTime getUpdated();
    int getVersion();
    
    Optional<LocalDateTime> getCompleted();
    Optional<String> getDescription();
    Optional<LocalDate> getDue_date();
    Optional<String> getSubject();
    Optional<String> getUpdater_id();
    Optional<String> getAssigned_user();
    Optional<String> getClient_identificator();
    Optional<String> getAssigned_user_email();
    Optional<String> getTask_ref();
  }
  
  @Value.Immutable
  interface SourceDbTaskKeywords {
    long getTask_id();
    String getKey_words();
  }
  
  @Value.Immutable
  interface SourceDbTaskRole {
    long getTask_id();
    String getAssigned_roles();
  }
  
  @Value.Immutable
  interface SourceDbTaskComment {
    long getTask_id();
    long getId();
    String getComment_text();
    LocalDateTime getCreated();
    String getUser_name();
    Optional<Long> getReply_to_id();
    Optional<Boolean> getExternal();
    Optional<String> getSource();
  }

  @Value.Immutable
  interface SourceDbTaskLink {
    long getId();
    String getLink_address(); 
    String getLink_key(); // = questionnaireId
    long getTask_id();
  }
  
  @Value.Immutable
  interface SourceDbTaskAccess {
    long getTask_id();
    LocalDateTime getUpdated();
    String getUser_id();
  }
}
