/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.client.spi.executor.questionnaire;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import io.dialob.api.proto.*;
import io.dialob.api.questionnaire.Error;
import io.dialob.api.questionnaire.*;
import io.dialob.client.api.ImmutableQuestionnaireSession;
import io.dialob.client.api.QuestionnaireSession;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.form.FormActions;
import io.dialob.client.spi.form.FormActionsUpdatesCallback;
import io.dialob.client.spi.form.FormActionsUpdatesItemsVisitor;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.compiler.Utils;
import io.dialob.executor.DialobSessionUpdater;
import io.dialob.executor.model.*;
import io.dialob.program.DialobProgram;
import io.dialob.program.DialobSessionEvalContextFactory;
import io.dialob.program.EvalContext;
import io.dialob.program.model.DisplayItem;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.spi.Constants;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.dialob.compiler.Utils.*;

// DialobQuestionnaireSession
@Slf4j
@EqualsAndHashCode(exclude = {"eventPublisher", "state"})
@ToString
public class QuestionnaireSessionImpl implements QuestionnaireSession, Serializable {

  private static final long serialVersionUID = -7713834248135795339L;

  enum State {
    NEW,
    ACTIVATING,
    ACTIVE,
    PASSIVATING,
    PASSIVE
  }


  private final String rev;

  private final DialobSession dialobSession;

  private final DialobProgram dialobProgram;

  private final QuestionClientVisibility questionClientVisibility;

  private final AtomicReference<State> state;

  private final Questionnaire.Metadata metadata;

  private final transient QuestionnaireEventPublisher eventPublisher;

  private final transient DialobSessionEvalContextFactory sessionContextFactory;

  private final transient AsyncFunctionInvoker asyncFunctionInvoker;

  private final transient Function<ItemState, ActionItem> toActionItemFunction;

  private QuestionnaireSessionImpl(String rev, @Nonnull DialobSession dialobSession, @Nonnull QuestionnaireSessionImpl dialobQuestionnaireSession) {
    this.rev = rev;
    this.dialobSession = dialobSession;
    this.eventPublisher = dialobQuestionnaireSession.eventPublisher;
    this.sessionContextFactory = dialobQuestionnaireSession.sessionContextFactory;
    this.asyncFunctionInvoker = dialobQuestionnaireSession.asyncFunctionInvoker;
    this.dialobProgram = dialobQuestionnaireSession.dialobProgram;
    this.questionClientVisibility = dialobQuestionnaireSession.questionClientVisibility;
    this.state = new AtomicReference<>(dialobQuestionnaireSession.state.get());
    this.metadata = dialobQuestionnaireSession.metadata;
    this.toActionItemFunction = dialobQuestionnaireSession.toActionItemFunction;
  }

  private QuestionnaireSessionImpl(@Nonnull QuestionnaireEventPublisher eventPublisher,
                                     @Nonnull DialobSessionEvalContextFactory sessionContextFactory,
                                     @Nonnull AsyncFunctionInvoker asyncFunctionInvoker,
                                     @Nonnull DialobSession dialobSession,
                                     @Nonnull DialobProgram dialobProgram,
                                     String rev,
                                     @Nonnull Questionnaire.Metadata metadata,
                                     @Nonnull State state,
                                     @Nonnull QuestionClientVisibility questionClientVisibility) {
    this.rev = rev;
    this.metadata = metadata;
    this.eventPublisher = eventPublisher;
    this.sessionContextFactory = sessionContextFactory;
    this.asyncFunctionInvoker = asyncFunctionInvoker;
    this.dialobSession = dialobSession;
    this.dialobProgram = dialobProgram;
    this.questionClientVisibility = questionClientVisibility;
    this.state = new AtomicReference<>(state);
    this.toActionItemFunction = itemState -> Utils.toActionItem(itemState, builder -> {
      if (itemState.hasCustomProps()) {
        ItemId id = itemState.getPrototypeId();
        if (id == null) {
          id = itemState.getId();
        }
        dialobProgram.getItem(id)
          .filter(item -> item instanceof DisplayItem)
          .map(item -> ((DisplayItem) item).getProps())
          .ifPresent(builder::props);
      }
      return builder;
    });
  }

  public QuestionnaireSession withIdAndRev(String id, String rev) {
    return new QuestionnaireSessionImpl(rev, dialobSession.withId(id), this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public void writeTo(@Nonnull CodedOutputStream output) throws IOException {
    writeNullableString(output, rev);
    output.writeInt32NoTag(questionClientVisibility.ordinal());
    output.writeInt32NoTag(state.get().ordinal());

    output.writeStringNoTag(metadata.getStatus().name());
    output.writeStringNoTag(metadata.getFormId());
    writeNullableString(output, metadata.getFormRev());
    writeNullableString(output, metadata.getLanguage());
    writeNullableString(output, metadata.getLabel());
    writeNullableDate(output, metadata.getCreated());
    writeNullableDate(output, metadata.getLastAnswer());
    writeNullableString(output, metadata.getCreator());
    writeNullableString(output, metadata.getOwner());
    writeNullableString(output, metadata.getTenantId());
    writeNullableString(output, metadata.getSubmitUrl());

    output.writeInt32NoTag(metadata.getAdditionalProperties().size());
    for (Map.Entry<String, Object> entry : metadata.getAdditionalProperties().entrySet()) {
      output.writeStringNoTag(entry.getKey());
      Utils.writeObjectValue(output, entry.getValue());
    }
    dialobSession.writeTo(output);
  }

  public static class Builder {

    private QuestionnaireEventPublisher eventPublisher;

    private DialobSessionEvalContextFactory sessionContextFactory;

    private AsyncFunctionInvoker asyncFunctionInvoker;

    private String rev;

    private DialobSession dialobSession;

    private DialobProgram dialobProgram;

    private State state = State.NEW;

    private QuestionClientVisibility questionClientVisibility = QuestionClientVisibility.ONLY_ENABLED;

    private Questionnaire.Metadata metadata;

    public Builder readFrom(@Nonnull CodedInputStream input) throws IOException {
      rev = readNullableString(input);
      questionClientVisibility = QuestionClientVisibility.values()[input.readInt32()];
      state = State.values()[input.readInt32()];

      ImmutableQuestionnaireMetadata.Builder metadataBuilder = ImmutableQuestionnaireMetadata.builder()
        .status(Questionnaire.Metadata.Status.valueOf(input.readString()))
        .formId(input.readString())
        .formRev(readNullableString(input))
        .language(readNullableString(input))
        .label(readNullableString(input))
        .created(readNullableDate(input))
        .lastAnswer(readNullableDate(input))
        .creator(readNullableString(input))
        .owner(readNullableString(input))
        .tenantId(readNullableString(input))
        .submitUrl(readNullableString(input));

      int additionalPropertiesCount = input.readInt32();
      for (int i = 0; i < additionalPropertiesCount; ++i) {
        String key = input.readString();
        Object value = Utils.readObjectValue(input);
        metadataBuilder.putAdditionalProperties(key, value);
      }

      metadata = metadataBuilder.build();
      dialobSession = DialobSession.readFrom(input);
      return this;
    }

    public Builder eventPublisher(QuestionnaireEventPublisher eventPublisher) {
      this.eventPublisher = eventPublisher;
      return this;
    }

    public Builder sessionContextFactory(DialobSessionEvalContextFactory sessionContextFactory) {
      this.sessionContextFactory = sessionContextFactory;
      return this;
    }

    public Builder asyncFunctionInvoker(AsyncFunctionInvoker asyncFunctionInvoker) {
      this.asyncFunctionInvoker = asyncFunctionInvoker;
      return this;
    }

    public Builder rev(String rev) {
      this.rev = rev;
      return this;
    }

    public Builder dialobSession(DialobSession dialobSession) {
      this.dialobSession = dialobSession;
      return this;
    }

    public Builder dialobProgram(DialobProgram dialobProgram) {
      this.dialobProgram = dialobProgram;
      return this;
    }

    public Builder state(State state) {
      this.state = state;
      return this;
    }

    public Builder questionClientVisibility(@Nonnull QuestionClientVisibility questionClientVisibility) {
      this.questionClientVisibility = questionClientVisibility;
      return this;
    }

    public Builder metadata(Questionnaire.Metadata metadata) {
      this.metadata = metadata;
      return this;
    }

    public Questionnaire.Metadata getMetadata() {
      return metadata;
    }

    public QuestionnaireSessionImpl build() {
      return new QuestionnaireSessionImpl(
        Objects.requireNonNull(eventPublisher, "eventPublisher is null"),
        Objects.requireNonNull(sessionContextFactory, "sessionContextFactory is null"),
        Objects.requireNonNull(asyncFunctionInvoker, "asyncFunctionInvoker is null"),
        dialobSession,
        dialobProgram,
        rev,
        metadata,
        state,
        questionClientVisibility);
    }
  }


  @Nonnull
  @Override
  public DispatchActionsResult dispatchActions(String revision, @Nonnull Collection<Action> actions) {
    final var questionnaireSession = this;

    ImmutableQuestionnaireSession.DispatchActionsResult.Builder result = ImmutableQuestionnaireSession
      .DispatchActionsResult.builder()
      .isDidComplete(false);
    if (isCompleted()) {
      return result
        .actions(ImmutableActions.builder()
          .rev(dialobSession.getRevision())
          .build())
        .build();
    }
    final FormActions formActions = new FormActions();
    try {
      MDC.put(Constants.QUESTIONNAIRE, getSessionId().orElse("no-session-id"));
      boolean revisionMatch = revision != null && revision.equals(dialobSession.getRevision());
      log.debug("revision comparison: {} vs. {} == {}", revision, dialobSession.getRevision(), revisionMatch);
      final DialobSessionUpdater sessionUpdater = sessionContextFactory.createSessionUpdater(dialobProgram, dialobSession);
      // broadcast user actions to other nodes
      final List<Action> userActions = actions.stream()
        .filter(action -> action.getType().isClientAction())
        .collect(Collectors.toList());
      final FormActionsUpdatesItemsVisitor actionsUpdatesItemsVisitor = new FormActionsUpdatesItemsVisitor(formActions, getIsVisiblePredicate(), this.toActionItemFunction);
      sessionUpdater.dispatchActions(actions, state.get() == State.ACTIVATING)
        .accept(new EvalContext.AbstractDelegateUpdatedItemsVisitor(actionsUpdatesItemsVisitor) {
          @Override
          public void visitCompleted() {
            super.visitCompleted();
            result.isDidComplete(true);
            getSessionId().ifPresent(sessionId -> eventPublisher.completed(sessionId));
          }

          @Override
          public Optional<AsyncFunctionCallVisitor> visitAsyncFunctionCalls() {
            return Optional.of(asyncFunctionInvoker.createVisitor(questionnaireSession));
          }
        });
      List<Action> updateActions = new ArrayList<>(userActions);
      List<Action> broadcastActions = updateActions;
      updateActions.addAll(formActions.getActions());

      if (!revisionMatch) {
        // reset updates and build form from ground up
        formActions.clear();
        buildFullForm(new FormActionsUpdatesCallback(formActions));
        broadcastActions = formActions.getActions();
      }
      if (!updateActions.isEmpty() && isActive()) {
        publishQuestionnaireActions(dialobSession.getRevision(), broadcastActions);
      }
    } finally {
      MDC.remove(Constants.QUESTIONNAIRE);
    }
    // But we wont return user actions back to original client
    return result
      .actions(ImmutableActions.builder()
        .actions(formActions.getActions())
        .rev(dialobSession.getRevision())
        .build())
      .build();
  }

  @Nonnull
  @Override
  public DispatchActionsResult dispatchActions(@Nonnull Collection<Action> actions) {
    return dispatchActions(dialobSession.getRevision(), actions);
  }

  @Nonnull
  @Override
  public Questionnaire getQuestionnaire() {
    Questionnaire.Metadata.Status status;
    if (dialobSession.isCompleted()) {
      status = Questionnaire.Metadata.Status.COMPLETED;
    } else {
      switch (state.get()) {
        case ACTIVATING:
        case NEW:
          status = Questionnaire.Metadata.Status.NEW;
          break;
        default:
          status = Questionnaire.Metadata.Status.OPEN;
      }
    }
    return ImmutableQuestionnaire.builder()
      .id(dialobSession.getId())
      .rev(this.rev)
      .context(getContextVariableValues())
      .answers(getAnswers())
      .errors(getErrors())
      .variableValues(getVariableValues())
      .activeItem(getActiveItem().orElse(null)) // deprecated
      .valueSets(getProvidedValueSets())
      .metadata(ImmutableQuestionnaireMetadata.builder()
        .from(metadata)
        .lastAnswer(new Date(dialobSession.getLastUpdate().toEpochMilli()))
        .completed(dialobSession.getCompleted() != null ? new Date(dialobSession.getCompleted().toEpochMilli()) : null)
        .opened(dialobSession.getOpened() != null ? new Date(dialobSession.getOpened().toEpochMilli()) : null)
        .status(status)
        .language(dialobSession.getLanguage())
        .tenantId(dialobSession.getTenantId())
        .build()
      ).build();
  }

  private Iterable<? extends ValueSet> getProvidedValueSets() {
    return () -> dialobSession.getValueSetStates().values().stream().map(state ->
      (ValueSet) ImmutableValueSet.builder()
        .id(state.getId().getValueSetId())
        .entries(() -> state.getEntries().stream()
          .filter(ValueSetState.Entry::isProvided)
          .map(entry ->
            (ValueSetEntry) ImmutableValueSetEntry.builder()
              .key(entry.getId())
              .value(entry.getLabel())
              .build()).iterator()
        ).build()).iterator();
  }

  private Iterable<? extends ContextValue> getContextVariableValues() {
    final List<ContextValue> answers = new ArrayList<>();
    dialobSession.accept(new DialobSessionVisitor() {
      @Override
      public Optional<ItemVisitor> visitItemStates() {
        return Optional.of(itemState -> {
          if (Utils.isContextVariable(itemState.getType())) {
            Object value = ConversionUtil.toJSON(itemState.getValue());
            answers.add(ImmutableContextValue.builder().id(IdUtils.toString(itemState.getId())).value(value == null ? null : value.toString()).build());
          }
        });
      }
    });
    return answers;
  }

  @Nonnull
  @Override
  public String getRevision() {
    return dialobSession.getRevision();
  }

  @Override
  public String getRev() {
    return rev;
  }

  @Override
  @Nullable
  public String getOwner() {
    return this.metadata.getOwner();
  }

  @Nonnull
  @Override
  public Instant getLastUpdate() {
    return dialobSession.getLastUpdate();
  }

  @Override
  public Optional<String> getActiveItem() {
    return dialobSession.getRootItem().getActivePage().map(IdUtils::toString);
  }

  @Nonnull
  @Override
  public List<io.dialob.api.proto.ValueSet> getValueSets() {
    List<io.dialob.api.proto.ValueSet> valueSets = new ArrayList<>();
    dialobSession.accept(new DialobSessionVisitor() {
      @Override
      public Optional<ValueSetVisitor> visitValueSetStates() {
        return Optional.of(valueSetState -> valueSets.add(ImmutableValueSet.builder().id(IdUtils.toString(valueSetState.getId())).entries(
          valueSetState.getEntries().stream().map(entry -> ImmutableValueSetEntry.builder().key(entry.getId()).value(entry.getLabel()).build()).collect(Collectors.toList())
        ).build()));
      }
    });

    return valueSets;
  }

  @Nonnull
  @Override
  public List<Error> getErrors() {
    List<Error> errors = new ArrayList<>();
    dialobSession.accept(new DialobSessionVisitor() {
      @Override
      public Optional<ErrorVisitor> visitErrorStates() {
        return Optional.of(errorState -> {
          if (errorState.isActive()) {
            errors.add(ImmutableError.builder()
              .code(errorState.getCode())
              .id(IdUtils.toString(errorState.getItemId()))
              .description(errorState.getLabel()).build()
            );
          }
        });
      }
    });
    return errors;
  }

  @Nonnull
  @Override
  public List<ActionItem> getItems() {
    final List<ActionItem> formItems = new ArrayList<>();
    dialobSession.accept(new DialobSessionVisitor() {
      @Override
      public Optional<ItemVisitor> visitItemStates() {
        return Optional.of(itemState -> formItems.add(toActionItemFunction.apply(itemState)));
      }
    });
    return formItems;
  }

  @Override
  public Optional<ActionItem> getItemById(@Nonnull String itemId) {
    return dialobSession.getItemState(IdUtils.toId(itemId)).map(toActionItemFunction);
  }

  @Nonnull
  @Override
  public List<ActionItem> getVisibleItems() {
    final List<ActionItem> formItems = new ArrayList<>();
    Predicate<SessionObject> isVisiblePredicate = getIsVisiblePredicate();
    dialobSession.accept(new DialobSessionVisitor() {
      @Override
      public Optional<ItemVisitor> visitItemStates() {
        return Optional.of(itemState -> {
          if (isVisiblePredicate.test(itemState)) {
            formItems.add(toActionItemFunction.apply(itemState));
          }
        });
      }
    });
    return formItems;
  }

  @Nonnull
  @Override
  public List<Answer> getAnswers() {
    final List<Answer> answers = new ArrayList<>();
    dialobSession.accept(new DialobSessionVisitor() {
      @Override
      public Optional<ItemVisitor> visitItemStates() {
        return Optional.of(itemState -> {
          if (itemState.isActive() && isQuestionType(itemState)) {
            final ImmutableAnswer.Builder answerBuilder = ImmutableAnswer.builder()
              .id(IdUtils.toString(itemState.getId()))
              .value(itemState.getAnswer());
            ItemId itemId = itemState.getPrototypeId();
            if (itemId == null) {
              itemId = itemState.getId();
            }

            dialobProgram.getItem(itemId).ifPresent(item -> {
              ValueType valueType = item.getValueType();
              if (valueType != null) {
                answerBuilder.type(valueType.getName());
              }
            });
            answers.add(answerBuilder.build());
          }
        });
      }
    });
    return answers;
  }

  @Nonnull
  @Override
  public List<VariableValue> getVariableValues() {
    final List<VariableValue> answers = new ArrayList<>();
    dialobSession.accept(new DialobSessionVisitor() {
      @Override
      public Optional<ItemVisitor> visitItemStates() {
        return Optional.of(itemState -> {
          if (Utils.isProgramVariable(itemState.getType())) {
            Object value = ConversionUtil.toJSON(itemState.getValue());
            answers.add(ImmutableVariableValue.builder().id(IdUtils.toString(itemState.getId())).value(value == null ? null : value.toString()).build());
          }
        });
      }
    });
    return answers;
  }

  @Override
  public void buildFullForm(@Nonnull UpdatesCallback updatesCallback) {
    updatesCallback.removeAll();
    getLocale().ifPresent(updatesCallback::locale);
    getVisibleItems().forEach(updatesCallback::questionAdded);
    getValueSets().forEach(updatesCallback::valueSetAdded);
    getErrors().forEach(updatesCallback::errorAdded);
    if (dialobSession.isCompleted()) {
      updatesCallback.completed();
    }
  }

  @Override
  public Optional<String> getSessionId() {
    return Optional.ofNullable(dialobSession.getId());
  }

  @Override
  public String getTenantId() {
    return dialobSession.getTenantId();
  }

  public void initialize() {
    // run initialization rules
    getSessionId().ifPresent(eventPublisher::created);
  }

  public void activate() {
    if (!state.compareAndSet(State.PASSIVE, State.ACTIVATING) && !state.compareAndSet(State.NEW, State.ACTIVATING)) {
      return;
    }

    List<Action> restoreActions = new ArrayList<>();
    // row group answers needs ro be restored.. but how??
    if (metadata.getStatus() == Questionnaire.Metadata.Status.COMPLETED) {
      restoreActions.add(ActionsFactory.complete(dialobSession.getId()));
    }
    // run activation rules
    dispatchActions(restoreActions);
    state.set(State.ACTIVE);
    getSessionId().ifPresent(eventPublisher::opened);
  }

  @Nonnull
  static String[] convertRows(List<String> rows) {
    Pattern pattern = Pattern.compile("^([^\\[]+)\\[(\\d+)]$");
    return rows.stream()
      .map(pattern::matcher)
      .filter(Matcher::matches)
      .map(matcher -> matcher.group(1) + "." + matcher.group(2)).toArray(String[]::new);
  }


  @Override
  public void passivate() {
    if (!state.compareAndSet(State.ACTIVE, State.PASSIVATING)) {
      return;
    }
    state.compareAndSet(State.PASSIVATING, State.PASSIVE);
  }

  @Override
  public boolean isActive() {
    return state.get() == State.ACTIVE;
  }

  @Override
  public boolean isCompleted() {
    return getStatus() == Questionnaire.Metadata.Status.COMPLETED;
  }

  @Override
  public boolean usesLastestFormRevision() {
    return true;
  }

  @Nonnull
  @Override
  public String getFormId() {
    return dialobProgram.getProgram().getId();
  }

  @Override
  public Optional<Locale> getLocale() {
    final String language = dialobSession.getLanguage();
    if (StringUtils.isNotBlank(language)) {
      return Optional.of(new Locale(language));
    }
    return Optional.empty();
  }

  @Override
  public QuestionClientVisibility getQuestionClientVisibility() {
    return questionClientVisibility;
  }

  @Override
  public void close() {
    // nothing to close here
  }

  Predicate<SessionObject> getIsVisiblePredicate() {
    switch(questionClientVisibility) {
      case ALL:
        return itemState -> itemState != null && itemState.isDisplayItem();
      case SHOW_DISABLED:
        return itemState -> itemState != null && itemState.isDisplayItem() && itemState.isActive();
      case ONLY_ENABLED:
      default:
        return itemState -> itemState != null && itemState.isDisplayItem() && itemState.isActive() && !itemState.isDisabled();
    }
  }

  private void publishQuestionnaireActions(String nextRevision, List<Action> actionQueue) {
    if (!actionQueue.isEmpty() && eventPublisher != null) {
      final ImmutableActions.Builder builder = ImmutableActions.builder().rev(nextRevision);
      actionQueue.stream().map(action -> ImmutableAction.builder().from(action).serverEvent(true).build()).forEach(builder::addActions);
      getSessionId().ifPresent(sessionId -> eventPublisher.actions(sessionId, builder.build()));
    }
  }

  @Nonnull
  @Override
  public Questionnaire.Metadata.Status getStatus() {
    return dialobSession.isCompleted() ? Questionnaire.Metadata.Status.COMPLETED : Questionnaire.Metadata.Status.OPEN;
  }

  @Nonnull
  public DialobProgram getDialobProgram() {
    return dialobProgram;
  }

  @Nonnull
  public DialobSession getDialobSession() {
    return dialobSession;
  }

}
