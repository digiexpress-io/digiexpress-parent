package io.resys.sysconfig.client.api;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import org.immutables.value.Value;

import io.dialob.client.api.DialobClient;
import io.resys.sysconfig.client.api.SysConfigClient.SysConfigReleaseQuery;
import io.resys.sysconfig.client.api.model.SysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.smallrye.mutiny.Uni;

public interface ExecutorClient {
  SysConfigSessionBuilder createSession();
  SysConfigSessionQuery querySession();
  SysConfigReleaseQuery queryReleases();
  SysConfigFillBuilder fillInstance();
  SysConfigProcesssFillBuilder processFillInstance();
  
  Uni<SysConfigSession> save(SysConfigSession session);
  Uni<SysConfigRelease> save(SysConfigRelease release);
  
  ExecutorClient withTenantId(String tenantConfigId);
  
  
  interface SysConfigSessionQuery {
    Uni<SysConfigSession> get(String sessionIdOrInstanceId);
  }
  
  interface SysConfigSessionBuilder {
    SysConfigSessionBuilder releaseId(String releaseId);
    SysConfigSessionBuilder ownerId(String ownerId);
    SysConfigSessionBuilder workflowName(String workflowName);
    SysConfigSessionBuilder locale(String locale);
    SysConfigSessionBuilder targetDate(Instant now);
    SysConfigSessionBuilder addAllProps(Map<String, Serializable> initVariables);
    SysConfigSessionBuilder addProp(String variableName, Serializable variableValue);
    Uni<SysConfigSession> build();

  }
  
  interface SysConfigFillBuilder {
    SysConfigFillBuilder session(SysConfigSession session);
    SysConfigFillBuilder actions(io.dialob.api.proto.Actions userActions);
    Uni<SysConfigSession> build();
  }
  interface SysConfigProcesssFillBuilder {
    SysConfigProcesssFillBuilder addAllProps(Map<String, Serializable> initVariables);
    SysConfigProcesssFillBuilder addProp(String variableName, Serializable variableValue);
    SysConfigProcesssFillBuilder session(SysConfigSession session);
    SysConfigProcesssFillBuilder targetDate(Instant targetDate);
    Uni<SysConfigSession> build();
  }    

  @Value.Immutable
  interface SysConfigSession {
    SysConfigInstance getState();
    DialobClient.ProgramWrapper getForm();
    io.dialob.client.api.QuestionnaireSession getSession();
    io.dialob.api.proto.Actions getActions();
    
    default io.dialob.api.questionnaire.Questionnaire getQuestionnaire() { return getSession().getQuestionnaire(); }
  }
 
}
