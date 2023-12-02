package io.resys.sysconfig.client.spi.executor.builder;

import java.time.Instant;
import java.util.ArrayList;

import io.dialob.api.proto.Actions;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.spi.executor.QuestionnaireExecutorImpl;
import io.dialob.client.spi.executor.questionnaire.QuestionnaireSessionImpl;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigFillBuilder;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.ImmutableSysConfigSession;
import io.resys.sysconfig.client.api.model.ImmutableFillCompleted;
import io.resys.sysconfig.client.api.model.ImmutableStep;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigInstance;
import io.resys.sysconfig.client.api.model.SysConfigInstance.FillCompleted;
import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import io.resys.thena.docdb.support.OidUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SysConfigFillBuilderImpl implements SysConfigFillBuilder {
  private final DialobClientConfig config;
  private Actions userActions;
  private SysConfigSession session;
  
  @Override public SysConfigFillBuilder actions(Actions userActions) { this.userActions = userActions; return this; }
  @Override public SysConfigFillBuilder session(SysConfigSession session) { this.session = session; return this; }
  @Override
  public Uni<SysConfigSession> build() {
    SysConfigAssert.notNull(userActions, () -> "actions must be defined!");
    SysConfigAssert.notNull(session, () -> "session must be defined!");
    
    final var sessionImpl = (QuestionnaireSessionImpl) session.getSession();
    final var questionnaire = session.getQuestionnaire();
    final var restored = new QuestionnaireExecutorImpl(
        questionnaire, session.getForm(),
        sessionImpl.getDialobSession(), config, false, false, sessionImpl, userActions);
    final var dialobExecution = restored.actions(userActions).executeAndGetBody();
    
    
    final SysConfigInstance instance;
    if(dialobExecution.getQuestionnaire().getMetadata().getStatus() == Questionnaire.Metadata.Status.COMPLETED) {
      final var steps = new ArrayList<>(this.session.getState().getSteps());
      final var end = Instant.now();
      
      steps.add(ImmutableStep.<FillCompleted>builder()
          .id(OidUtils.gen())
          .targetDate(end)
          .body(ImmutableFillCompleted.builder().questionnaireSessionId(questionnaire.getId()).build())
          .build());
      
      instance = ImmutableSysConfigInstance.builder()
          .from(this.session.getState())
          .steps(steps)
          .build();
    } else {
      instance = this.session.getState();
    }

    return Uni.createFrom().item(ImmutableSysConfigSession.builder()
        .from(this.session)
        .state(instance)
        .actions(dialobExecution.getActions())
        .session(dialobExecution.getSession())
        .build());
  }

}
