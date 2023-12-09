package io.resys.sysconfig.client.spi.executor.builder;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigProcesssFillBuilder;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.ImmutableSysConfigSession;
import io.resys.sysconfig.client.api.model.ImmutableFlowCompleted;
import io.resys.sysconfig.client.api.model.ImmutableStep;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigInstance.FlowCompleted;
import io.resys.sysconfig.client.spi.executor.ExecutorStore;
import io.resys.sysconfig.client.spi.executor.ExecutorStore.WrenchFlow;
import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import io.resys.thena.docdb.support.OidUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SysConfigProcesssFillBuilderImpl implements SysConfigProcesssFillBuilder {
  private final ExecutorStore store;
  private final AssetClient assetClient;
  private final Map<String, Serializable> initProps = new HashMap<>();
  private SysConfigSession session;
  private Instant targetDate;
  
  @Override public SysConfigProcesssFillBuilder addAllProps(Map<String, Serializable> initVariables) { this.initProps.putAll(initVariables); return this; }
  @Override public SysConfigProcesssFillBuilder addProp(String variableName, Serializable variableValue) { this.initProps.put(variableName, variableValue); return this; }
  @Override public SysConfigProcesssFillBuilder targetDate(Instant targetDate) { this.targetDate = targetDate; return this; }
  @Override public SysConfigProcesssFillBuilder session(SysConfigSession session) { this.session = session; return this; }

  @Override
  public Uni<SysConfigSession> build() {
    SysConfigAssert.notNull(session, () -> "session must be defined!");
    SysConfigAssert.notNull(targetDate, () -> "targetDate must be defined!");
    
    final var created = session.getState().getStepProcessCreated().getBody();
    return store.queryFlows().releaseId(created.getReleaseId()).get(created.getFlowName())
        .onItem().transform(flow -> doInFlow(flow, session));
  }

  private SysConfigSession doInFlow(WrenchFlow flow, SysConfigSession session) {
    final var created = session.getState().getStepProcessCreated().getBody();
    
    final var flowBuilder = assetClient.getConfig().getHdes().executor(flow.getEnvir())
        .inputMap(initProps)
        .inputField("questionnaire", session.getQuestionnaire());
    
    session.getQuestionnaire().getContext().forEach(ctx -> {
      
      flowBuilder.inputField(ctx.getId(), (Serializable) ctx.getValue());
    });
   
    final var flowResult = flowBuilder.flow(created.getFlowName()).andGetBody();
    final var completed = ImmutableStep.<FlowCompleted>builder()
        .id(OidUtils.gen())
        .targetDate(targetDate)
        .body(ImmutableFlowCompleted.builder()
            .accepts(flowResult.getAccepts())
            .returns(flowResult.getReturns())
            .build())
        .build();
   
    return ImmutableSysConfigSession.builder().from(session)
        .state(ImmutableSysConfigInstance.builder()
            .from(session.getState())
            .addSteps(completed)
            .build())
        .build();
  }
}
