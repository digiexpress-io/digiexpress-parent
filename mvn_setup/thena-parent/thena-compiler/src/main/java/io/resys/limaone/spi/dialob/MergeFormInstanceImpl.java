package io.resys.limaone.spi.dialob;

import java.util.Objects;

import io.dialob.api.proto.Actions;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.dialob.FormDb.MergeFormInstance;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MergeFormInstanceImpl implements MergeFormInstance {
  private final FormDbProps db;
  private String questionnaireId;
  private Actions actions;
  private Boolean forceCompletion;

  @Override
  public MergeFormInstance formInstanceId(String questionnaireId) {
    this.questionnaireId = Objects.requireNonNull(questionnaireId, () -> "questionnaireId must be defined");
    return this;
  }
  @Override
  public MergeFormInstance props(Actions actions) {
    this.actions = Objects.requireNonNull(actions, () -> "actions must be defined");
    return this;
  }
  @Override
  public MergeFormInstance forceCompletion(boolean forceCompletion) {
    this.forceCompletion = forceCompletion;
    return this;
  }

  @Override
  public Uni<FormInstance> build() {
    Objects.requireNonNull(questionnaireId, () -> "questionnaireId must be defined");
    
    if (Boolean.TRUE.equals(forceCompletion)) {
      return forceCompleteSession();
    }
    
    Objects.requireNonNull(actions, () -> "actions must be defined when not force completing");
    
    // Apply normal actions to questionnaire
    return db.getQuestionnaireHttp()
      .httpQuery()
      .uri(uri -> uri.append("questionnaires").append(questionnaireId).append("actions").build())
      .method(io.dialob.api.questionnaire.Questionnaire.class)
      .body(actions)
      .postOne()
      .onItem().transform(questionnaire -> new FormInstanceImpl(questionnaire, false, db));
  }
  
  private Uni<FormInstance> forceCompleteSession() {
    // Get current session state
    return db.getQuestionnaireHttp()
      .httpQuery()
      .uri(uri -> uri.append("questionnaires").append(questionnaireId).build())
      .method(com.fasterxml.jackson.databind.JsonNode.class)
      .getOne()
      .onItem().transformToUni(sessionData -> {
        final String currentStatus = getStatus(sessionData);
        
        if ("COMPLETED".equals(currentStatus)) {
          // Already completed, just return current state
          return getFinalQuestionnaire();
        }
        
        if ("NEW".equals(currentStatus)) {
          // NEW -> OPEN -> COMPLETED
          return transitionStatus(sessionData, "OPEN")
            .onItem().transformToUni(openSessionData -> 
              transitionStatus(openSessionData, "COMPLETED")
            )
            .onItem().transformToUni(completedData -> 
              getFinalQuestionnaire()
            );
        } else {
          // Any other status -> COMPLETED directly
          return transitionStatus(sessionData, "COMPLETED")
            .onItem().transformToUni(completedData -> 
              getFinalQuestionnaire()
            );
        }
      });
  }
  
  private Uni<com.fasterxml.jackson.databind.JsonNode> transitionStatus(
      com.fasterxml.jackson.databind.JsonNode sessionData, 
      String newStatus) {
    
    final var updatedData = setStatus(sessionData, newStatus);
    
    return db.getQuestionnaireHttp()
      .httpQuery()
      .uri(uri -> uri.append("questionnaires").append(questionnaireId).build())
      .method(com.fasterxml.jackson.databind.JsonNode.class)
      .body(updatedData)
      .putOne()
      .onItem().transformToUni(response -> 
        // Get updated session data after status change
        db.getQuestionnaireHttp()
          .httpQuery()
          .uri(uri -> uri.append("questionnaires").append(questionnaireId).build())
          .method(com.fasterxml.jackson.databind.JsonNode.class)
          .getOne()
      );
  }
  
  private Uni<FormInstance> getFinalQuestionnaire() {
    return db.getQuestionnaireHttp()
      .httpQuery()
      .uri(uri -> uri.append("questionnaires").append(questionnaireId).build())
      .method(io.dialob.api.questionnaire.Questionnaire.class)
      .getOne()
      .onItem().transform(questionnaire -> new FormInstanceImpl(questionnaire, false, db));
  }
  
  private String getStatus(com.fasterxml.jackson.databind.JsonNode sessionData) {
    final var statusNode = sessionData.get("status");
    return statusNode != null ? statusNode.asText() : "UNKNOWN";
  }
  
  private com.fasterxml.jackson.databind.JsonNode setStatus(
      com.fasterxml.jackson.databind.JsonNode sessionData, 
      String newStatus) {
    
    if (sessionData instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
      final var objectNode = (com.fasterxml.jackson.databind.node.ObjectNode) sessionData.deepCopy();
      objectNode.put("status", newStatus);
      return objectNode;
    }
    
    // Fallback: create minimal status update object
    final var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    final var statusUpdate = mapper.createObjectNode();
    statusUpdate.put("status", newStatus);
    return statusUpdate;
  }
  
}
