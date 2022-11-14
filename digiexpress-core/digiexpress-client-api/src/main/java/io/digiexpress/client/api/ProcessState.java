package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
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


@Value.Immutable @JsonSerialize(as = ImmutableProcessState.class) @JsonDeserialize(as = ImmutableProcessState.class)
public interface ProcessState extends Serializable {
  String getId();
  Integer getVersion();
  
  ServiceRef getDef(); // service definition doc
  ServiceRel getRel(); // service release doc
  
  List<Step<?>> getSteps();
  
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
  default Optional<Step<FillInProgress>> getStepFillInProgress() {
    return getStep(StepType.FILL_IN_PROGRESS).map(e -> (Step<FillInProgress>) e);
  }
  @JsonIgnore @SuppressWarnings("unchecked")
  default Optional<Step<FillCompleted>> getStepFillCompleted() {
    return getStep(StepType.FILL_COMPLETED).map(e -> (Step<FillCompleted>) e);
  }
  
  @JsonIgnore
  default Optional<Step<?>> getStep(StepType type) {
    return getSteps().stream()
        .sorted((a, b) -> b.getStart().compareTo(b.getStart()))
        .filter(a -> a.getBody().getType() == type)
        .findFirst();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableStep.class) @JsonDeserialize(as = ImmutableStep.class)
  interface Step<T extends StepBody> extends Serializable {
    String getId();
    Integer getVersion();
    LocalDateTime getStart();
    LocalDateTime getEnd();
    List<Error> getErrors();
    T getBody();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableServiceRef.class) @JsonDeserialize(as = ImmutableServiceRef.class)
  interface ServiceRef {
    String getId();
    String getVersion();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableServiceRel.class) @JsonDeserialize(as = ImmutableServiceRel.class)
  interface ServiceRel {
    String getId();
    String getVersion();
    String getName();
  }
  
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
  @JsonSubTypes({        
    @Type(value = ProcessCreated.class, name = "PROCESS_CREATED"),
    
    @Type(value = FillCreated.class, name = "FILL_CREATED"),
    @Type(value = FillInProgress.class, name = "FILL_IN_PROGRESS"),
    @Type(value = FillCompleted.class, name = "FILL_COMPLETED"),
    
    @Type(value = FlowStarted.class, name = "FLOW_STARTED"),
    @Type(value = FillInProgress.class, name = "FLOW_COMPLETED"),
    
    @Type(value = ProcessCompleted.class, name = "PROCESS_COMPLETED"),
    })
  interface StepBody extends Serializable {
    StepType getType();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableProcessCreated.class) @JsonDeserialize(as = ImmutableProcessCreated.class)
  interface ProcessCreated extends StepBody {
    String getName();
    String getDesc();
    String getFlowId();
    String getFormId();
    @Value.Default
    default StepType getType() { return StepType.PROCESS_CREATED; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableFillCreated.class) @JsonDeserialize(as = ImmutableFillCreated.class)
  interface FillCreated extends StepBody {
    String getQuestionnaireSessionId();
    @Value.Default
    default StepType getType() { return StepType.FILL_CREATED; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableFillInProgress.class) @JsonDeserialize(as = ImmutableFillInProgress.class)
  interface FillInProgress extends StepBody {
    String getQuestionnaireSessionId();
    default StepType getType() { return StepType.FILL_IN_PROGRESS; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableFillCompleted.class) @JsonDeserialize(as = ImmutableFillCompleted.class)
  interface FillCompleted extends StepBody {
    String getQuestionnaireSessionId();
    default StepType getType() { return StepType.FILL_COMPLETED; }
  }

  interface FlowStarted extends StepBody {
    Map<String, Serializable> getAccepts();
  }

  interface FlowCompleted extends StepBody {
    Map<String, Serializable> getReturns();
  }

  interface ProcessCompleted extends StepBody {
  }
  
  
  interface Error extends Serializable {
    String getId();
    String getMsg();
  }
  
  enum StepType {
    PROCESS_CREATED,
    
    FILL_CREATED,
    FILL_IN_PROGRESS,
    FILL_COMPLETED,
    
    FLOW_STARTED,
    FLOW_COMPLETED,

    PROCESS_COMPLETED
  }
}