package io.dialob.client.spi.executor;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.dialob.api.form.Form;
import io.dialob.api.proto.ValueSet;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.ImmutableQuestionnaire;
import io.dialob.api.questionnaire.ImmutableQuestionnaireMetadata;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.VariableValue;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobClient.ProgramEnvir;
import io.dialob.client.api.DialobClient.ProgramWrapper;
import io.dialob.client.api.DialobClient.QuestionnaireExecutor;
import io.dialob.client.api.DialobClient.QuestionnaireInit;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.compiler.Utils;
import io.dialob.executor.CreateDialobSessionProgramVisitor;
import io.dialob.executor.model.DialobSession;
import io.dialob.executor.model.IdUtils;
import io.dialob.executor.model.ValueSetState;
import io.dialob.program.model.VariableItem;
import io.dialob.spi.Constants;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
public class QuestionnaireExecutorBuilderImpl implements DialobClient.QuestionnaireExecutorBuilder {

  private final ProgramEnvir envir;
  private final DialobClientConfig config;

  
  @Override
  public QuestionnaireExecutor create(String id, Consumer<QuestionnaireInit> initWith) {
    return this.create(id, null, initWith);
  }
  @Override
  public QuestionnaireExecutor create(String id, String rev, Consumer<QuestionnaireInit> initWith) {
    DialobAssert.notNull(id, () -> "id can't be null!");
    DialobAssert.notNull(initWith, () -> "initWith can't be null!");
    
    final var formAndProgram = rev == null ? envir.findByFormId(id) : envir.findByFormIdAndRev(id, rev);
    final var formDocument = formAndProgram.getDocument();
    final var init = new QuestionnaireInitImpl();
    initWith.accept(init);
    
    DialobAssert.notEmpty(init.id(), () -> "questionnaire.id must be defined!");
    DialobAssert.notEmpty(init.rev(), () -> "questionnaire.rev must be defined!");
    
    
    final var questionnaire = this.createNewQuestionnaire(formDocument, init);
    final var dialobSession = this.createSession(questionnaire, formAndProgram);    
    return new QuestionnaireExecutorImpl(questionnaire, formAndProgram, dialobSession, config, true);
  }

  @Override
  public QuestionnaireExecutor restore(Questionnaire questionnaire) {
    DialobAssert.notNull(questionnaire, () -> "questionnaire can't be null!");
    DialobAssert.notEmpty(questionnaire.getId(), () -> "questionnaire.id must be defined!");
    DialobAssert.notEmpty(questionnaire.getRev(), () -> "questionnaire.rev must be defined!");
    
    final var formAndProgram = envir.findByFormIdAndRev(questionnaire.getMetadata().getFormId(), questionnaire.getMetadata().getFormRev());
    final var dialobSession = this.createSession(questionnaire, formAndProgram);
    return new QuestionnaireExecutorImpl(questionnaire, formAndProgram, dialobSession, config, false);
  }
  
  protected DialobSession createSession(Questionnaire questionnaire, ProgramWrapper formAndProgram) {
    final var dialobProgram = formAndProgram.getProgram().get();
    return dialobProgram.createSession(
        config.getFactory(),
        formAndProgram.getDocument().getMetadata().getTenantId(),
        questionnaire.getId(),
        questionnaire.getMetadata().getLanguage(),
        questionnaire.getActiveItem(), 
        initialValues(questionnaire), 
        findProvidedValueSetEntries(questionnaire),
        questionnaire.getMetadata().getCompleted(),
        questionnaire.getMetadata().getOpened(),
        questionnaire.getMetadata().getLastAnswer());
      
  }
  
  
  protected Questionnaire createNewQuestionnaire(Form formDocument, QuestionnaireInitImpl init) {
    final var formId = formDocument.getId();
    final var questionnaire = ImmutableQuestionnaire.builder();
    final var metadata = ImmutableQuestionnaireMetadata.builder();

    if (init.additionalProperties() != null) {
      metadata.putAllAdditionalProperties(init.additionalProperties());
    }
    metadata.formId(formId);
    metadata.formName(formDocument.getName());
    metadata.label(formDocument.getMetadata().getLabel());
    metadata.creator(init.creator());
    metadata.owner(Objects.toString(init.owner(), init.creator()));
    metadata.status(init.status() != null ? init.status() : Questionnaire.Metadata.Status.NEW );
    
    
    boolean useLatest = Constants.LATEST_REV.equals(formDocument.getRev());
    if (useLatest) {
      metadata.formRev(Constants.LATEST_REV);
    } else {
      metadata.formRev(formDocument.getRev());
    }
    metadata.created(new Date());
    if (init.submitUrl() != null) {
      metadata.submitUrl(init.submitUrl());
    }
    metadata.language(init.language());

    questionnaire.metadata(metadata.build());

    if (init.contextValues() != null) {
      questionnaire.context(init.contextValues());
    }
    if (init.answers() != null) {
      questionnaire.answers(init.answers());
    }
    if (init.valueSets() != null) {
      questionnaire.valueSets(init.valueSets());
    }
    
    return questionnaire
        .activeItem(init.activeItem)
        .id(init.id())
        .rev(init.rev())
        .build();
  }
  
  

  protected CreateDialobSessionProgramVisitor.InitialValueResolver initialValues(Questionnaire questionnaire) {
    return  (itemId, item) -> {
      final String id = IdUtils.toString(itemId);
      if (item instanceof VariableItem) {
        for (ContextValue contextValue : questionnaire.getContext()) {
          if (id.equals(contextValue.getId())) {
            return Optional.ofNullable(Utils.parse(item.getValueType(), contextValue.getValue()));
          }
        }
        for (VariableValue variableValue : questionnaire.getVariableValues()) {
          if (id.equals(variableValue.getId())) {
            return Optional.ofNullable(Utils.parse(item.getValueType(), variableValue.getValue()));
          }
        }
      } else {
        for (Answer answer : questionnaire.getAnswers()) {
          if (id.equals(answer.getId())) {
            return Optional.ofNullable(answer.getValue());
          }
        }
      }
      return Optional.empty();
    };
  }
  
  protected CreateDialobSessionProgramVisitor.ProvidedValueSetEntriesResolver findProvidedValueSetEntries(Questionnaire questionnaire) {
    return valueSetId -> questionnaire.getValueSets().stream()
    .filter(valueSet -> valueSet.getId().equals(valueSetId.getValueSetId()))
    .findFirst()
    .map(valueSet -> valueSet.getEntries().stream().map(entry -> ValueSetState.Entry.of(entry.getKey(), entry.getValue(), true)).collect(Collectors.toList()))
    .orElse(Collections.emptyList()); 
  }

  @Accessors(fluent = true, chain = true)
  @RequiredArgsConstructor
  @Data
  public static class QuestionnaireInitImpl implements QuestionnaireInit {
    private Questionnaire questionnaire;
    private String activeItem;
    private String id;
    private String rev;
    private String creator;
    private String owner;
    private String submitUrl;
    private List<ContextValue> contextValues;
    private String language = "en";
    private List<Answer> answers;
    private List<ValueSet> valueSets;
    private Questionnaire.Metadata.Status status;
    private Map<String,Object> additionalProperties;

    @Override
    public QuestionnaireInit language(String language) {
      this.language = language;
      if (StringUtils.isBlank(language)) {
        this.language = "en";
      }
      return this;
    }
    public String language() {
      language(this.language);
      return this.language;
    }
  }
}
