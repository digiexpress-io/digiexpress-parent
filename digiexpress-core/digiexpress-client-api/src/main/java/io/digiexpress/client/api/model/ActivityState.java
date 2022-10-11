package io.digiexpress.client.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.dialob.api.questionnaire.Questionnaire;

public interface ActivityState extends Serializable {
  String getId();
  String getVersion();
  String getProjectId();
  
  Steps getSteps();
  StepType getStepType();
  LocalDateTime getStart();
  LocalDateTime getEnd();
  
  
  interface Steps extends Serializable {
    String getId();
    String getSummary();
    List<Step<?>> getValues();
    
    // last instance of the step
    Step<ActivityStarted> getActivityStarted();
    Step<FillCreated> getFillCreated();
    Step<FillInProgress> getFillInProgress();
    Step<FillCompleted> getFillCompleted();
    Step<FlowError> getFlowError();
    Step<FlowError> getFlowCompleted();
  }
  
  interface Step<T extends StepBody> extends Serializable {
    String getId();
    Integer getCount();
    LocalDateTime getDateTime();
    List<Error> getErrors();
    StepType getType();
    T getBody();
  }
  
  interface StepBody {}
  
  interface ActivityStarted extends StepBody {
    RevisionId getServiceId();
    RevisionId getActivityId();
  }
  
  interface FillCreated extends StepBody {
    RevisionId getFormId();
    Map<String, Serializable> getAccepts();
    QuestionnaireSessionId getReturns();
  }
  
  interface FillInProgress extends StepBody {
    Questionnaire getQuestionnaire();
  }

  interface FillCompleted extends StepBody {
    Questionnaire getQuestionnaire();
  }

  interface FlowError extends StepBody {
    Map<String, Serializable> getAccepts();
  }
  
  interface FlowCompleted extends StepBody {
    Map<String, Serializable> getReturns();
  }
  
  interface Error extends Serializable {
    String getId();
    String getMsg();
  }
  

  interface QuestionnaireSessionId {
    
  }
  
  interface RevisionId {
    String getId();
    String getRevision();
  }
  
  
  enum StepType {
    ACTIVITY_STARTED,
    FILL_CREATED,
    FILL_IN_PROGRESS,
    FILL_COMPLETED,
    FLOW_ERROR,
    FLOW_COMPLETED
  }
}