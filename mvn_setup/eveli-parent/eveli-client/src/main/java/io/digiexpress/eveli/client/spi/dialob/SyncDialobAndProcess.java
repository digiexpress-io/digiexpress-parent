package io.digiexpress.eveli.client.spi.dialob;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.Questionnaire.Metadata.Status;
import io.digiexpress.eveli.client.api.ImmutableCompleteCustomerAssignmentCommand;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskAssignmentStatus;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


// background non blocking sync block
@RequiredArgsConstructor
@Slf4j
public class SyncDialobAndProcess {

  private final TaskClient taskClient;
  private final EveliEnvirClient envir;
  private final DialobClient dialobClient;
  private final ObjectMapper objectMapper;
  
  
  public Uni<Void> executeFlowForInstance(ProcessInstance init, Optional<Questionnaire> questionnaire) {
    final Uni<Questionnaire> questionnaireUni = questionnaire.map(q -> Uni.createFrom().item(q)).orElseGet(() -> getQuestionnaire(init));
    final Uni<ProcessInstance> execution; 
    
    if(init.getType() == null) {
      execution = executeUserTask(init, questionnaireUni);
      
    } else if(init.getType() == GrimProcessType.CUSTOMER_ASSIGNMENT) {
      execution = executeCustomerAssignment(init, questionnaireUni);
      
    } else {
      log.error("Skipping execution because process of type: {} is not supported!", init.getType());
      return Uni.createFrom().voidItem();
    }
    
    return execution
      .onFailure().recoverWithUni((e) -> {
        log.error("Skipping execution because questionnaire: {} processing failed because of: {}!", init.getQuestionnaireId(), e.getMessage(), e);
        return Uni.createFrom().item(init);
      })
      .onItem().transformToUni((ignore) -> Uni.createFrom().voidItem());
  }
  

  private Uni<ProcessInstance> executeCustomerAssignment(ProcessInstance init, Uni<Questionnaire> questionnaireUni) {
    return Uni.combine().all().unis(getRuntime(init), questionnaireUni, getTask(init))
      .asTuple()
      .onItem().transformToUni(tuple -> {
      
        final var task = tuple.getItem3().orElseThrow(() -> new SyncDialobAndProcessException("Skipping execution: '" + init.getId() + "' because task MUST be defined for objective!"));
        final var questionnaire = tuple.getItem2();
        
        return taskClient.modifyProcess()
          .commitAuthor(SyncDialobAndProcess.class.getSimpleName())
          .commitMessage("Sync Form and Eveli")
          .id(init.getId().toString())
          .onAnyUni(merger -> {
            
            final var currentState = merger.getCurrentState();
            
            final var assignment = task.getCustomerAssignments().stream()
                .filter(t -> t.getQuestionnaireId().equals(currentState.getQuestionnaireId()))
                .findFirst();
              
            if(assignment.isEmpty()) {
              throw new SyncDialobAndProcessException("Skipping execution: '" + currentState.getId() + "' because assignment MUST be defined for questionnaire!");
            }
            
            if(assignment.get().getStatus() == TaskAssignmentStatus.COMPLETED) {
              throw new SyncDialobAndProcessException("Skipping execution: '" + currentState.getId() + "' because assignment MUST be 'OPEN'!");
            }
            return taskClient.taskBuilder()
                .userId(SyncDialobAndProcess.class.getSimpleName(), null)
                .completeCustomerAssignment(task.getId(), ImmutableCompleteCustomerAssignmentCommand.builder()
                    .targetDate(ZonedDateTime.now())
                    .assignmentId(assignment.get().getId())
                    .taskVersion(task.getVersion())
                    .build());
          })
          .merge((currentState, merge) -> merge
              .formBody(toJsonString(questionnaire))
              .status(GrimProcessStatus.COMPLETED)
              .build())
          .build();
        }
      );
  }
  
  private Uni<ProcessInstance> executeUserTask(ProcessInstance init, Uni<Questionnaire> questionnaireUni) {
    return Uni.combine().all().unis(getRuntime(init), questionnaireUni)
      .asTuple()
      .onItem().transformToUni(tuple -> {
        
        final var questionnaire = tuple.getItem2();
        final var runtime = tuple.getItem1();
      
        return taskClient.modifyProcess()
          .commitAuthor(SyncDialobAndProcess.class.getSimpleName())
          .commitMessage("Sync Form and Eveli")
          .id(init.getId().toString())
          .onAnyUni((merger) -> executeWorkflow(questionnaire, merger.getCurrentState(), runtime)
              .onItem().invoke(flow -> merger
                .flowBody(toJsonString(flow))
                .formBody(toJsonString(questionnaire))
              )
          )
          .onTask((task, merger) -> task.stream().forEach(t -> merger.taskId(t.getId())))
          .merge((currentState, merge) -> {
  
            if(currentState.getTaskId() != null) {
              log.debug("Skipping execution: {} because task is already created, process status handling is probably wrong!", currentState.getId());
              merge.skip();
              return;
            }
            merge.status(GrimProcessStatus.ANSWERED).build();
          })
          .build();
      });
  }
  
  private Uni<Optional<Task>> getTask(ProcessInstance init) {
    return init.getTaskId() == null ? 
        Uni.createFrom().item(Optional.empty()) : 
        taskClient.queryTasks().getOneById(init.getTaskId()).onItem().transform(Optional::of);
  }
  
  private Uni<Questionnaire> getQuestionnaire(ProcessInstance init) {
    return Uni.createFrom().item(() -> dialobClient.getQuestionnaireAndMetaById(init.getQuestionnaireId()))
      .onItem().transform(questionnaire -> {
        
        if(questionnaire.getMetadata().getStatus() != Status.COMPLETED) {
          log.debug("Skipping execution because questionnaire: {} state is not completed!", init.getQuestionnaireId());
          throw new SyncDialobAndProcessException(
              "Can't transfer questionnaire to flow, " + 
              "expected status: 'COMPLETED' " + 
              "but was: '" + questionnaire.getMetadata().getStatus() + "'"
          );
        }
        return questionnaire;
      });
  }
  
  private Uni<EveliRuntime> getRuntime(ProcessInstance init) {
    return envir
        .withCockpitIdSupplier(() -> Uni.createFrom().item(Optional.ofNullable(init.getCockpitId())))
        .runtimeQuery().getOne();
  }
  
  private String toJsonString(Object object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch(Exception e) {
      throw new SyncDialobAndProcessException("Failed to writeValueAsString for: " + object.getClass().getSimpleName());
    }
  }
  
  private Uni<FlowResult> executeWorkflow(Questionnaire questionnaire, ProcessInstance instance, EveliRuntime runtime) {
    return Uni.createFrom().item(() -> {
      final var flowInput = new HashMap<String, Serializable>();
      flowInput.put("questionnaireId", instance.getQuestionnaireId());
      flowInput.put("workflowName", instance.getWorkflowName());
      
      
      final var flowName = Optional.ofNullable(instance.getFlowName()).orElseGet(() -> {
        return runtime.getStencil(OffsetDateTime.now())
          .getSites().values().stream().flatMap(e -> e.getLinks().values().stream())
          .filter(topic -> Boolean.TRUE.equals(topic.getWorkflow()))
          .filter(topic -> topic.getValue().equals(instance.getWorkflowName()))
          .map(e -> e.getFlowName())
          .findFirst().orElse(null);
      });
      
      RepoAssert.notEmpty(flowName, () -> "Can't identify stencil workflow for name: " + instance.getWorkflowName() + "!");
      
      
      final FlowResult run = runtime.getWrench()
          .inputMap(flowInput)
          .flow(flowName)
          .andGetBody();
      
      return run;
    })
    .runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
  }
  
  private static class SyncDialobAndProcessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SyncDialobAndProcessException(String message) {
      super(message);
    }
  }
}