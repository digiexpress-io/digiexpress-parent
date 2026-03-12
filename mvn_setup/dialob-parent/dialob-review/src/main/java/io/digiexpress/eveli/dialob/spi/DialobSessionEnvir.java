package io.digiexpress.eveli.dialob.spi;

/*-
 * #%L
 * dialob-review
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.api.questionnaire.VariableValue;
import io.dialob.questionnaire.service.api.session.FormFinder;
import io.dialob.questionnaire.service.api.session.QuestionnaireSession;
import io.dialob.session.engine.DialobProgramFromFormCompiler;
import io.dialob.session.engine.Utils;
import io.dialob.session.engine.program.DialobProgram;
import io.dialob.session.engine.program.DialobSessionEvalContextFactory;
import io.dialob.session.engine.program.model.VariableItem;
import io.dialob.session.engine.session.model.DialobSession;
import io.dialob.session.engine.session.model.IdUtils;
import io.dialob.session.engine.session.model.ValueSetState;
import io.dialob.session.engine.sp.DialobQuestionnaireSession;
import io.dialob.session.engine.sp.DialobQuestionnaireSessionServiceFacade;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class DialobSessionEnvir implements FormFinder {

  private final Form form; 
  private final Questionnaire formData;
  private final DialobProgram program;
  private final DialobSessionEvalContextFactory context;
  private final NoEventPublisher eventPublisher;
  private final NoFunctionInvoker asyncFunctionInvoker;
  
  public DialobSessionEnvir(Form form, Questionnaire formData) {
    super();
    this.form = form;
    this.formData = formData;
    
    
    final var reg = new FunctionRegistryForReview();
    final var compiler = new DialobProgramFromFormCompiler(reg);
    
    this.context = new DialobSessionEvalContextFactory(reg, null);
    this.program = compiler.compileForm(form);
    this.eventPublisher = new NoEventPublisher();
    this.asyncFunctionInvoker = new NoFunctionInvoker();
  }


  public QuestionnaireSession accept() {
    DialobSession dialobSession = this.program.createSession(
      context,
      form.getMetadata().getTenantId(),
      formData.getId(),
      formData.getMetadata().getLanguage(),
      formData.getActiveItem(), 
      (itemId, item) -> {
        final String id = IdUtils.toString(itemId);
        if (item instanceof VariableItem) {
          for (ContextValue contextValue : formData.getContext()) {
            if (id.equals(contextValue.getId())) {
              return Optional.ofNullable(Utils.parse(item.valueType(), contextValue.getValue()));
            }
          }
          for (VariableValue variableValue : formData.getVariableValues()) {
            if (id.equals(variableValue.getId())) {
              return Optional.ofNullable(Utils.parse(item.valueType(), variableValue.getValue()));
            }
          }
        } else {
          for (Answer answer : formData.getAnswers()) {
            if (id.equals(answer.getId())) {
              return Optional.ofNullable(answer.getValue());
            }
          }
        }
        return Optional.empty();
      }, 
      
      valueSetId -> formData.getValueSets().stream()
        .filter(valueSet -> valueSet.getId().equals(valueSetId.getValueSetId()))
        .findFirst()
        .map(valueSet -> valueSet.getEntries().stream().map(entry -> ValueSetState.Entry.of(entry.getKey(), entry.getValue(), true)).toList())
        .orElse(Collections.emptyList()),
        
      formData.getMetadata().getCompleted(),
      formData.getMetadata().getOpened(),
      formData.getMetadata().getLastAnswer()
    );
    
    if (formData.getMetadata().getStatus() == Questionnaire.Metadata.Status.COMPLETED) {
      dialobSession.complete();
    }
    
    
    DialobQuestionnaireSession dialobQuestionnaireSession = null;
    try {
      dialobQuestionnaireSession = applyFormSettings(
        DialobQuestionnaireSession.builder()
          .serviceFacade(new DialobQuestionnaireSessionServiceFacade(eventPublisher, context, asyncFunctionInvoker))
          .dialobSession(dialobSession)
          .dialobProgram(program)
          .rev(formData.getRev())
          .metadata(formData.getMetadata()), formData.getMetadata().getAdditionalProperties())
        .build();

      dialobQuestionnaireSession.activate();
      return dialobQuestionnaireSession;
    } catch (Exception e) {
      if (dialobQuestionnaireSession != null) {
        dialobQuestionnaireSession.close();
      }
      throw e;
    }
  }

  

  private DialobQuestionnaireSession.Builder applyFormSettings(DialobQuestionnaireSession.Builder builder, Map<String, Object> additionalProperties) {
    builder.questionClientVisibility(getQuestionClientVisibility(additionalProperties));
    return builder;
  }
  
  private QuestionnaireSession.QuestionClientVisibility getQuestionClientVisibility(Map<String, Object> additionalProperties) {
    return
      parseEnum(QuestionnaireSession.QuestionClientVisibility.class, additionalProperties.get("questionClientVisibility"))
        .or(() -> {
          if (parseBoolean(additionalProperties.get("showDisabled"))) {
            return Optional.of(QuestionnaireSession.QuestionClientVisibility.SHOW_DISABLED);
          }
          return Optional.empty();
        })
        .orElse(QuestionnaireSession.QuestionClientVisibility.ONLY_ENABLED);
  }
  
  private static <T extends Enum<T>> Optional<T> parseEnum(Class<T> enumClass, Object o) {
    if (o instanceof String) {
      try {
        return Optional.of(Enum.valueOf(enumClass, (String) o));
      } catch (IllegalArgumentException e) {
        log.error("Unknown question client visibility {}", o);
      }
    }
    return Optional.empty();
  }

  private static boolean parseBoolean(Object o) {
    boolean showDisabled = false;
    if (o instanceof String) {
      showDisabled = Boolean.parseBoolean((String) o);
    } else if (o instanceof Boolean) {
      showDisabled = (Boolean) o;
    }
    return showDisabled;
  }

  @Override
  public Form findForm(String formId, String formRev) {
    return this.form;
  }
}
