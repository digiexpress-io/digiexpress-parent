package io.dialob.client.spi.executor;

import io.dialob.api.form.Form;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobClient.ExecutorBody;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobClient.QuestionnaireExecutor;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.api.ImmutableExecutorBody;
import io.dialob.client.api.QuestionnaireSession;
import io.dialob.client.spi.executor.questionnaire.QuestionnaireSessionImpl;
import io.dialob.client.spi.form.FormActions;
import io.dialob.client.spi.form.FormActionsUpdatesCallback;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.executor.model.DialobSession;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * DialobQuestionnaireSessionBuilder
 *
 *
 *  QuestionnaireSessionProcessingService#answerQuestion

        final QuestionnaireSession session = AbstractQuestionnaireSessionService.findOne(questionnaireId);
        {
        return restore(questionnaireDatabase.findOne(currentTenant.getId(), questionnaireDocumentId));

          protected QuestionnaireSession restore(Questionnaire questionnaire) {
  return DialobQuestionnaireSessionBuilder()
    .setQuestionnaire(questionnaire)
    .build();
}
        }

        session.dispatchActions(revision, actions);
        JdbcQuestionnaireDatabase.save(currentTenant.getId(), questionnaireSession.getQuestionnaire());

        session.getSessionId().ifPresent(sessionId -> eventPublisher.completed(session.getTenantId(), sessionId));
        this.storeSessionIntoCache(questionnaireId, session);

*/


//DialobSessionEngineAutoConfiguration.QuestionnaireSessionBuilder
//DialobQuestionnaireSessionBuilder

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class QuestionnaireExecutorImpl implements DialobClient.QuestionnaireExecutor {

  private final Questionnaire questionnaire;
  private final ProgramWrapper formAndProgram;
  private final DialobSession dialobSession;
  private final DialobClientConfig config;
  private final boolean newSession;
  private boolean createOnly;

  private QuestionnaireSessionImpl questionnaireSessionImpl;
  private Actions actions;

  @Override
  public QuestionnaireExecutor actions(Actions actions) {
    this.actions = actions;
    return this;
  }
  @Override
  public QuestionnaireExecutor createOnly(boolean createOnly) {
    this.createOnly = createOnly;
    return this;
  }
  @Override
  public Actions execute() {
    return this.executeAndGetBody().getActions();
  }

  protected QuestionnaireSessionImpl createQuestionnaireSession() {
    if(this.questionnaireSessionImpl != null) {
      return this.questionnaireSessionImpl;
    }

    QuestionnaireSessionImpl state = null;
    try {
      state = QuestionnaireSessionImpl.builder()
        .eventPublisher(config.getEventPublisher())
        .sessionContextFactory(config.getFactory())
        .asyncFunctionInvoker(config.getAsyncFunctionInvoker())
        .dialobSession(dialobSession)
        .dialobProgram(formAndProgram.getProgram().get())
        .rev(questionnaire.getRev())
        .metadata(questionnaire.getMetadata())
        .questionClientVisibility(getQuestionClientVisibility(formAndProgram.getDocument().getData()))
        .build();

      if(newSession && !createOnly) {
        state.initialize();
      }

      state.activate();
      this.questionnaireSessionImpl = state;

      return state;
    } catch (Exception e) {
      if (state != null) {
        state.close();
      }
      throw e;
    }
  }



  protected QuestionnaireSession.QuestionClientVisibility getQuestionClientVisibility(Form formDocument) {
    QuestionnaireSession.QuestionClientVisibility questionClientVisibility = QuestionnaireSession.QuestionClientVisibility.ONLY_ENABLED;
    Object o = formDocument.getMetadata().getAdditionalProperties().get("questionClientVisibility");
    if (o instanceof String) {
      try {
        return QuestionnaireSession.QuestionClientVisibility.valueOf((String) o);
      } catch (IllegalArgumentException e) {
        log.error("Unknown question client visibility {}", o);
      }
    }
    o = formDocument.getMetadata().getAdditionalProperties().get("showDisabled");
    if (o != null) {
      boolean showDisabled = false;
      if (o instanceof String) {
        showDisabled = Boolean.parseBoolean((String) o);
      } else if (o instanceof Boolean) {
        showDisabled = (Boolean) o;
      }
      if (showDisabled) {
        questionClientVisibility = QuestionnaireSession.QuestionClientVisibility.SHOW_DISABLED;
      }
    }
    return questionClientVisibility;
  }

  @Override
  public QuestionnaireSession toSession() {
    DialobAssert.notEmpty(questionnaire.getId(), () -> "questionnaire.id must be defined!");
    DialobAssert.notEmpty(questionnaire.getRev(), () -> "questionnaire.rev must be defined!");

    if (questionnaire.getMetadata().getStatus() == Questionnaire.Metadata.Status.COMPLETED) {
      dialobSession.complete();
    }
    // create execution state for questionnaire
    return createQuestionnaireSession();
  }
  @Override
  public ExecutorBody executeAndGetBody() {
    DialobAssert.notEmpty(questionnaire.getId(), () -> "questionnaire.id must be defined!");
    DialobAssert.notEmpty(questionnaire.getRev(), () -> "questionnaire.rev must be defined!");

    if (questionnaire.getMetadata().getStatus() == Questionnaire.Metadata.Status.COMPLETED) {
      dialobSession.complete();
    }

    // create execution state for questionnaire
    final var state = createQuestionnaireSession();

    if (state.isCompleted()) {
      return ImmutableExecutorBody.builder()
          .session(state)
          .actions(ImmutableActions.builder().rev(state.getRev()).build())
          .build();
    }

    // build all actions from scratch no answers update
    if(actions == null || actions.getActions().isEmpty()) {
      final var formActions = new FormActions();
      state.buildFullForm(new FormActionsUpdatesCallback(formActions));
      return ImmutableExecutorBody.builder()
          .session(state)
          .actions(ImmutableActions.builder()
              .actions(formActions.getActions())
              .rev(state.getRevision())
              .build())
          .build();
    }

    // update answers
    final var rev = actions.getRev() == null ? state.getRev() : actions.getRev();
    final QuestionnaireSession.DispatchActionsResult response = state.dispatchActions(rev, actions.getActions());
    if (response.isDidComplete()) {
      state.getSessionId().ifPresent(sessionId -> config.getEventPublisher().completed(sessionId));
    }


    return ImmutableExecutorBody.builder()
        .session(state)
        .actions(response.getActions())
        .build();
  }
}
