package io.digiexpress.client.spi.executors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import io.dialob.api.proto.Actions;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient.ProgramEnvir;
import io.dialob.client.api.DialobClient.ProgramEnvirValue;
import io.dialob.client.api.DialobClient.ProgramStatus;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobClient.QuestionnaireExecutor;
import io.dialob.client.api.DialobDocument;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.client.api.ImmutableFormDocument;
import io.dialob.client.api.ImmutableProgramWrapper;
import io.dialob.client.api.ImmutableStoreEntity;
import io.dialob.client.spi.support.OidUtils;
import io.digiexpress.client.api.ImmutableExecution;
import io.digiexpress.client.api.ImmutableExecutionDialobBody;
import io.digiexpress.client.api.ImmutableFillCompleted;
import io.digiexpress.client.api.ImmutableFillCreated;
import io.digiexpress.client.api.ImmutableFillInProgress;
import io.digiexpress.client.api.ImmutableProcessState;
import io.digiexpress.client.api.ImmutableStep;
import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ProcessState.FillCompleted;
import io.digiexpress.client.api.ProcessState.FillCreated;
import io.digiexpress.client.api.ProcessState.FillInProgress;
import io.digiexpress.client.api.Client.DialobExecutor;
import io.digiexpress.client.api.Client.Execution;
import io.digiexpress.client.api.Client.ExecutionDialobBody;
import io.digiexpress.client.api.Client.QuestionnaireStore;
import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.spi.support.ServiceAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobExecutorImpl implements DialobExecutor {
  private final ClientConfig config;
  private final ProcessState state;
  private final ServiceEnvir envir;
  private QuestionnaireStore questionnaireStore; 
  private Actions userActions;
  
  @Override
  public DialobExecutor store(QuestionnaireStore questionnaireStore) {
    ServiceAssert.notNull(questionnaireStore, () -> "questionnaireStore must be defined!!");
    this.questionnaireStore = questionnaireStore; 
    return this; 
  }
  @Override
  public DialobExecutor actions(Actions userActions) {
    this.userActions = userActions;
    return this;
  }
  @Override
  public Execution<ExecutionDialobBody> build() {
    ServiceAssert.notNull(questionnaireStore, () -> "questionnaireStore must be defined!!");
    
    final var created = state.getStepProcessCreated();
    final var dialob = config.getDialob();
    final var envir = create(created.getBody().getFormId());
    
    final var fillCreated = state.getStepFillCreated();
    if(fillCreated.isPresent()) {
      ServiceAssert.notNull(userActions, () -> "actions must be defined!!");
      
      final var inProgress = state.getStepFillInProgress()
          .map(previous -> ImmutableStep.<FillInProgress>builder().version(previous.getVersion()+1).from(previous).build())
          .orElseGet(() -> {
            return ImmutableStep.<FillInProgress>builder()
                .id(genGid())
                .version(1)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now())
                .body(ImmutableFillInProgress.builder()
                    .questionnaireSessionId(fillCreated.get().getBody().getQuestionnaireSessionId())
                    .build())
                .build();
          });
  
      final var id = inProgress.getBody().getQuestionnaireSessionId();
      final var questionnaire = this.questionnaireStore.get(id);
      final var restored = dialob.executor(envir).restore(questionnaire);
      final var dialobExecution = restored.actions(userActions).executeAndGetBody();
      final var steps = new ArrayList<>(state.getSteps().stream().filter(step -> !step.getId().equals(inProgress.getId())).collect(Collectors.toList()));
      final var end = LocalDateTime.now();
      
      if(dialobExecution.getQuestionnaire().getMetadata().getStatus() == Questionnaire.Metadata.Status.COMPLETED) {
        steps.add(ImmutableStep.<FillCompleted>builder()
            .id(genGid())
            .version(1)
            .start(fillCreated.get().getStart())
            .end(end)
            .body(ImmutableFillCompleted.builder().questionnaireSessionId(id).build())
            .build()); 
      }
      
      final var nextState = ImmutableProcessState.builder()
          .from(state)
          .steps(steps)
          .addSteps(ImmutableStep.<FillInProgress>builder().from(inProgress).end(end).build())
          .build();
      
      
      return ImmutableExecution.<ExecutionDialobBody>builder()
          .body(ImmutableExecutionDialobBody
              .builder()
              .actions(dialobExecution.getActions())
              .state(nextState)
              .questionnaire(dialobExecution.getQuestionnaire())
              .build())
          .build();
    }

    final var questionnaireSessionId = genGid();
    QuestionnaireExecutor newExecutor = dialob.executor(envir).create(created.getBody().getFormId(), (init) -> {
      init.id(questionnaireSessionId).rev(genGid());
    });
  
    if(this.userActions != null) {
      newExecutor = newExecutor.actions(userActions);
    }
    
    final var dialobExecution = newExecutor.executeAndGetBody();
    final var nextState = ImmutableProcessState.builder()
        .from(state)
        .steps(state.getSteps())
        .addSteps(ImmutableStep.<FillCreated>builder()
            .id(UUID.randomUUID().toString())
            .version(1)
            .start(LocalDateTime.now())
            .end(LocalDateTime.now())
            .body(ImmutableFillCreated.builder().questionnaireSessionId(questionnaireSessionId).build())
            .build())
        .build();
    return ImmutableExecution.<ExecutionDialobBody>builder()
        .body(ImmutableExecutionDialobBody.builder()
            .state(nextState)
            .actions(dialobExecution.getActions())            
            .questionnaire(dialobExecution.getQuestionnaire())
            .build())
        .build();
  }
  private FixedProgramEnvir create(String formId) {
    final var value = envir.getForm(formId);
    final var form = value.getDelegate(config);
    final var doc = ImmutableFormDocument.builder().data(form)
        .id(form.getId())
        .created(Instant.ofEpochMilli(form.getMetadata().getCreated().getTime())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime())
        .updated(Instant.ofEpochMilli(form.getMetadata().getLastSaved().getTime())
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime())
        .name(form.getName())
        .description("release")
        .version(form.getRev())
        .type(DialobDocument.DocumentType.FORM)
        .build();
    final var wrapper = ImmutableProgramWrapper.builder()
        .id(form.getId())
        .document(doc)
        .program(value.getCompiled(config))
        .source(ImmutableStoreEntity.builder().id("").version("").bodyType(DialobDocument.DocumentType.FORM).body("").build())
        .status(ProgramStatus.UP)
        .build();
    return new FixedProgramEnvir(wrapper);
  }
  
  final String genGid() {
    return OidUtils.gen();
  }

  @RequiredArgsConstructor
  private static class FixedProgramEnvir implements ProgramEnvir {
    private final ProgramWrapper wrapper;
    @Override
    public ProgramWrapper findByFormId(String formId) throws DocumentNotFoundException {
      final var form = wrapper.getDocument().getData();
      ServiceAssert.isTrue(form.getId().equals(formId), 
          () -> "Fixed envir not set up correctly, expecting formId = '" + form.getId() + "' but was '" + formId + "'!");
      return wrapper;
    }
    @Override
    public ProgramWrapper findByFormIdAndRev(String formId, String formRev) throws DocumentNotFoundException {
      final var form = wrapper.getDocument().getData();
      ServiceAssert.isTrue(form.getId().equals(formId), () -> "Fixed envir not set up correctly, expecting formId = '" + form.getId() + "' but was '" + formId + "'!");
      ServiceAssert.isTrue(form.getRev().equals(formRev), () -> "Fixed envir not set up correctly, expecting formRev = '" + form.getRev() + "' but was '" + formRev + "'!");
      return wrapper;
    }
    @Override
    public List<ProgramWrapper> findAll() {
      return Arrays.asList(wrapper);
    }
    @Override
    public Map<String, ProgramEnvirValue<?>> getValues() {
      return Map.of(wrapper.getId(), wrapper);
    }
  }
}
