package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

import org.immutables.value.Value;

import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.thena.docdb.api.DocDB;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.StencilClient;

public interface Client {
  ServiceEnvirBuilder envir();
  TenantBuilder repo();
  ServiceExecutorBuilder executor(ServiceEnvir envir);
  ClientConfig getConfig();
  QueryFactory getQuery();
  
  interface ServiceClientException {}

  interface ServiceExecutorBuilder {
    ProcessExecutor process(String nameOrId);
    DialobExecutor dialob(ProcessState state);
    HdesExecutor hdes(ProcessState state);
    StencilExecutor stencil();
  }

  // returns new process instance and new fill session
  interface ProcessExecutor {
    ProcessExecutor targetDate(LocalDateTime now);
    ProcessExecutor actions(Map<String, Serializable> initVariables);
    ProcessExecutor action(String variableName, Serializable variableValue);
    Execution<ProcessState> build();
  }

  // continues fill
  interface DialobExecutor {
    DialobExecutor store(QuestionnaireStore store);
    DialobExecutor actions(io.dialob.api.proto.Actions userActions);
    Execution<ExecutionDialobBody> build();
  }
  
  interface HdesExecutor {
    HdesExecutor store(QuestionnaireStore store);
    HdesExecutor targetDate(LocalDateTime targetDate);
    Execution<ExecutionHdesBody> build();
  }

  // returns stencil content
  interface StencilExecutor {
    StencilExecutor targetDate(LocalDateTime targetDate);
    StencilExecutor locale(String locale);
    Execution<LocalizedSite> build();
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

  interface TenantBuilder {
    TenantBuilder repoStencil(String repoStencil);
    TenantBuilder repoHdes(String repoHdes);
    TenantBuilder repoDialob(String repoDialob);
    TenantBuilder repoProject(String repoService);

    Uni<Client> load();
    Uni<Client> create();
    Client build();
  }
  
  interface QuestionnaireStore {
    Questionnaire get(String questionnaireId);
  }
  
  interface ServiceEnvirBuilder {
    ServiceEnvirBuilder add(ServiceRelease release);
    ServiceEnvir build();
  }
  
  @Value.Immutable
  interface ClientConfig {
    ClientStore getStore();
    ClientCache getCache();
    Parser getParser();
    
    DialobClient getDialob();
    HdesClient getHdes();
    StencilClient getStencil();
    Archiver getArchiver();
    DocDB getDocDb();
  }
}
