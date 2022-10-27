package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.dialob.api.questionnaire.Questionnaire;

public interface ProcessState extends Serializable {
  String getId();
  String getVersion();
  List<Step<?>> getSteps();
  StepType getStepType();
  
  interface Step<T extends StepBody> extends Serializable {
    String getId();
    Integer getRunCount();
    LocalDateTime getStart();
    LocalDateTime getEnd();
    List<Error> getErrors();
    StepType getType();
    T getBody();
  }
  
  interface StepBody {}
  
  interface ProcessStarted extends StepBody {
    RevisionId getServiceId();
    RevisionId getProcessId();
  }
  
  interface FillStarted extends StepBody {
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
    PROCESS_STARTED,
    
    FILL_STARTED,
    FILL_IN_PROGRESS,
    FILL_COMPLETED,
    
    FLOW_STARTED,
    FLOW_COMPLETED,

    PROCESS_COMPLETED
  }
  
  
  interface ProcessStateWrapper extends Serializable {
    
    // last instance of the step
    Step<ProcessStarted> getProcessStarted();
    Step<FillStarted> getFillStarted();
    Step<FillInProgress> getFillInProgress();
    Step<FillCompleted> getFillCompleted();
    Step<FlowError> getFlowCompleted();
  }
  
}