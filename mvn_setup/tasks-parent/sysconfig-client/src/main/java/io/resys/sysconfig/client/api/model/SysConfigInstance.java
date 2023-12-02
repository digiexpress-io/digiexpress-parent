package io.resys.sysconfig.client.api.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@Value.Immutable @JsonSerialize(as = ImmutableSysConfigInstance.class) @JsonDeserialize(as = ImmutableSysConfigInstance.class)
public interface SysConfigInstance extends Document {
  String getId();
  String getOwnerId();
  List<Step<?>> getSteps();
  
  @Value.Default default DocumentType getDocumentType() { return DocumentType.SYS_CONFIG_INSTANCE; }
  
  @JsonIgnore @SuppressWarnings("unchecked")
  default Step<ProcessCreated> getStepProcessCreated() {
    return getStep(StepType.PROCESS_CREATED)
        .map(e -> (Step<ProcessCreated>) e)
        .orElseThrow(() -> new IllegalStateException("Step: '" + StepType.PROCESS_CREATED + "' not available at this state!"));
  }
  
  @JsonIgnore @SuppressWarnings("unchecked")
  default Optional<Step<FillCreated>> getStepFillCreated() {
    return getStep(StepType.FILL_CREATED).map(e -> (Step<FillCreated>) e);
  }
  @JsonIgnore @SuppressWarnings("unchecked")
  default Optional<Step<FillCompleted>> getStepFillCompleted() {
    return getStep(StepType.FILL_COMPLETED).map(e -> (Step<FillCompleted>) e);
  }
  
  @JsonIgnore
  default Optional<Step<?>> getStep(StepType type) {
    return getSteps().stream()
        .sorted((a, b) -> b.getTargetDate().compareTo(b.getTargetDate()))
        .filter(a -> a.getBody().getType() == type)
        .findFirst();
  }


  @Value.Immutable @JsonSerialize(as = ImmutableStep.class) @JsonDeserialize(as = ImmutableStep.class)
  interface Step<T extends StepBody> extends Serializable {
    String getId();
    Instant getTargetDate();
    List<Error> getErrors();
    T getBody();
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
  @JsonSubTypes({        
    @Type(value = ProcessCreated.class, name = "PROCESS_CREATED"),
    
    @Type(value = FillCreated.class, name = "FILL_CREATED"),
    @Type(value = FillCompleted.class, name = "FILL_COMPLETED"),
    
    @Type(value = FlowCompleted.class, name = "FLOW_COMPLETED"),
    
    @Type(value = ProcessCompleted.class, name = "PROCESS_COMPLETED"),
    })
  interface StepBody extends Serializable {
    StepType getType();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableProcessCreated.class) @JsonDeserialize(as = ImmutableProcessCreated.class)
  interface ProcessCreated extends StepBody {
    String getId(); 
    String getServiceName();
    String getFormId();
    String getFlowName();
    
    String getServiceId();
    String getReleaseId();
    String getReleaseName();
    
    Map<String, Serializable> getParams();
    
    @JsonIgnore @Value.Default
    default StepType getType() { return StepType.PROCESS_CREATED; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableFillCreated.class) @JsonDeserialize(as = ImmutableFillCreated.class)
  interface FillCreated extends StepBody {
    String getQuestionnaireSessionId();
    @JsonIgnore @Value.Default 
    default StepType getType() { return StepType.FILL_CREATED; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableFillCompleted.class) @JsonDeserialize(as = ImmutableFillCompleted.class)
  interface FillCompleted extends StepBody {
    String getQuestionnaireSessionId();
    default StepType getType() { return StepType.FILL_COMPLETED; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableFlowCompleted.class) @JsonDeserialize(as = ImmutableFlowCompleted.class)
  interface FlowCompleted extends StepBody {
    Map<String, Serializable> getAccepts();
    Map<String, Serializable> getReturns();
    default StepType getType() { return StepType.FLOW_COMPLETED; }
  }

  interface ProcessCompleted extends StepBody { }
  
  interface Error extends Serializable {
    String getId();
    String getMsg();
  }
  
  enum StepType {
    PROCESS_CREATED,
    
    FILL_CREATED,
    FILL_COMPLETED,

    FLOW_COMPLETED,
    PROCESS_COMPLETED
  }
}
