package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import org.immutables.value.Value;

import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.client.api.AssetExecutorEntity.ProcessState;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;

public interface AssetExecutor {


  // returns new process instance and new fill session
  interface ProcessExecutor extends AssetExecutor {
    ProcessExecutor targetDate(LocalDateTime now);
    ProcessExecutor actions(Map<String, Serializable> initVariables);
    ProcessExecutor action(String variableName, Serializable variableValue);
    Execution<ProcessState> build();
  }

  // continues fill
  interface DialobExecutor extends AssetExecutor {
    DialobExecutor store(QuestionnaireStore store);
    DialobExecutor actions(io.dialob.api.proto.Actions userActions);
    Execution<ExecutionDialobBody> build();
  }
  
  interface HdesExecutor extends AssetExecutor {
    HdesExecutor store(QuestionnaireStore store);
    HdesExecutor targetDate(LocalDateTime targetDate);
    Execution<ExecutionHdesBody> build();
  }

  // returns stencil content
  interface StencilExecutor extends AssetExecutor {
    StencilExecutor targetDate(LocalDateTime targetDate);
    StencilExecutor locale(String locale);
    Execution<LocalizedSite> build();
  }
  

  interface QuestionnaireStore {
    Questionnaire get(String questionnaireId);
  }

  @Value.Immutable
  interface Execution<T> {
    T getBody();
  }

  @Value.Immutable
  interface ExecutionDialobBody {
    ProcessState getState();
    io.dialob.api.questionnaire.Questionnaire getQuestionnaire();
    io.dialob.api.proto.Actions getActions();
  }
  
  @Value.Immutable
  interface ExecutionHdesBody {
    ProcessState getState();
    FlowResult getFlow();
  }
}
