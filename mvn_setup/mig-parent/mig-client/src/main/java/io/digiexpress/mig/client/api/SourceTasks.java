package io.digiexpress.mig.client.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import io.digiexpress.eveli.client.spi.task.TaskMapper;

@Value.Immutable
public interface SourceTasks {

  Map<Long, SourceTask> getTasks();
  Map<Long, List<SourceRole>> getRoles();
  Map<Long, List<SourceKeywords>> getKeywords();
  Map<Long, List<SourceComment>> getComments();
  Map<Long, List<SourceLink>> getLinks();
  Map<Long, List<SourceAccess>> getAccess();
  Map<Long, SourceProcess> getProcesses();
  Map<Long, SourceWorkflow> getWorkflows();
  
  
  default ZoneId getZoneId() {
    return ZoneId.of("Europe/Helsinki");
  }
  
  
  default List<SourceComment> getComments(long taskId) {
    return Optional.ofNullable(this.getComments().get(taskId)).orElse(Collections.emptyList())
      .stream().sorted((a, b) -> a.getCreated().compareTo(b.getCreated()))
      .toList();
  }
  
  default String getQuestionnaireId(long taskId) {
    return Optional.ofNullable(this.getLinks().get(taskId))
        .map(links -> links.stream()
            .filter(e -> "questionnaireId".equals(e.getLink_key()))
            .map(e -> e.getLink_address())
            .findFirst().orElse(null))
        .orElse(null);
  }


  @Value.Immutable
  interface SourceWorkflow {
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
  interface SourceProcess {
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
  interface SourceTask {
    
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
    
    
    default String getAssigneeGid() {
      return String.valueOf(getId()) + "_assignee";
    }
  }
  
  @Value.Immutable
  interface SourceKeywords {
    long getTask_id();
    String getKey_words();
    
    
    default String getGid() {
      return getTask_id() + "_kw";
    }
  }
  
  @Value.Immutable
  interface SourceRole {
    long getTask_id();
    String getAssigned_roles();
    
    default String getGid() {
      return String.valueOf(getTask_id()) + "_role";
    }
  }
  
  @Value.Immutable
  interface SourceComment {
    long getTask_id();
    long getId();
    String getComment_text();
    LocalDateTime getCreated();
    String getUser_name();
    Optional<Long> getReply_to_id();
    Optional<Boolean> getExternal();
    Optional<String> getSource();
    
    
    default String getRemarkType() {
      return Boolean.TRUE.equals(getExternal().orElse(false)) ? TaskMapper.COMMENT_EXTERNAL : TaskMapper.COMMENT_INTERNAL;
    }
    
    default String getGid() {
      return getId() + "_comment";
    }
    
    default Optional<String> getReployToGid() {
      return getReply_to_id().map(r -> r + "_comment");
    }
  }

  @Value.Immutable
  interface SourceLink {
    long getId();
    String getLink_address(); 
    String getLink_key(); // = questionnaireId
    long getTask_id();
  }
  
  @Value.Immutable
  interface SourceAccess {
    long getTask_id();
    LocalDateTime getUpdated();
    String getUser_id();
    
    
    default String getGid() {
      return getTask_id() + "_cw";
    }
  }
  
}
