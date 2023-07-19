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
package io.dialob.client.tests.steps.support;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormItem;
import io.dialob.api.form.ImmutableForm;
import io.dialob.api.form.ImmutableFormItem;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.ImmutableQuestionnaire;
import io.dialob.api.questionnaire.ImmutableQuestionnaireMetadata;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobClient;
import io.dialob.client.spi.event.DistributedEvent.FormUpdatedEvent;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.client.spi.function.FunctionRegistryImpl;
import io.dialob.client.tests.client.DialobClientTestImpl;
import io.dialob.client.tests.steps.support.StepsBuilder.WhenMessage;



public class AbstractWebSocketTests {

  private FunctionRegistryImpl registry;
  private QuestionnaireEventPublisher publisher;
  private Context ctx;
  
  @BeforeEach
  public void createFreshClient() {
    this.ctx = new Context();
    this.publisher = new QuestionnaireEventPublisher(this.ctx);
    this.registry = new FunctionRegistryImpl();
    final var client = DialobClientTestImpl.builder()
        .functionRegistry(registry)
        .eventPublisher(publisher)
        .asyncFunctionInvoker(new AsyncFunctionInvoker(registry))
        .build();
    this.ctx.setClient(client);
  }
  
  public StepsBuilder steps() { return new StepsBuilder(ctx); }
  public DialobClient getClient() { return ctx.getClient(); }
  public FunctionRegistryImpl getRegistry() { return registry; }

  public Form shouldFindForm(ImmutableForm form) {
    return save(form);
  }
  
  public Form save(ImmutableForm form) {
    final var envir = ctx.getClient().envir()
      .addCommand().id(form.getId()).form(form).build()
      .build();
    ctx.setEnvir(envir);
    return form;
  }
  
  public void publishEvent(FormUpdatedEvent event) {
    ctx.publish(event);
  }
  
  protected Questionnaire createQuestionnaireDocument(String questionnaireId, String questionnaireRev, Form formDocument) {
    return createQuestionnaireDocument(questionnaireId, questionnaireRev, formDocument.getId(), formDocument.getRev());
  }

  protected Questionnaire createQuestionnaireDocument(String questionnaireId, String questionnaireRev, Form formDocument, List<ContextValue> context) {
    return createQuestionnaireDocument(questionnaireId, questionnaireRev, formDocument.getId(), formDocument.getRev(), context);
  }

  protected Questionnaire createQuestionnaireDocument(String questionnaireId, String questionnaireRev, String formId, String formRev) {
    Questionnaire questionnaire = ImmutableQuestionnaire.builder()
      .metadata(ImmutableQuestionnaireMetadata.builder()
        .formId(formId)
        .formRev(formRev)
        .build())
      .id(questionnaireId)
      .rev(questionnaireRev)
      .build();
    return questionnaire;
  }

  protected Questionnaire createQuestionnaireDocument(String questionnaireId, String questionnaireRev, String formId, String formRev, List<ContextValue> context) {
    Questionnaire questionnaire = ImmutableQuestionnaire.builder()
      .metadata(ImmutableQuestionnaireMetadata.builder()
        .formId(formId)
        .formRev(formRev)
        .build())
      .id(questionnaireId)
      .rev(questionnaireRev)
      .context(context)
      .build();
    return questionnaire;
  }

  protected FormItem addQuestionnaire(ImmutableForm.Builder formBuilder, Consumer<ImmutableFormItem.Builder> builderConsumer) {
    FormItem formItemBean = addItem(formBuilder, "questionnaire", builder -> {
      builder.type("questionnaire");
      builderConsumer.accept(builder);
    });
    return formItemBean;
  }

  protected FormItem addItem(ImmutableForm.Builder formBuilder, String itemId, Consumer<ImmutableFormItem.Builder> builderConsumer) {
    ImmutableFormItem.Builder builder = ImmutableFormItem.builder().id(itemId);
    builderConsumer.accept(builder);
    FormItem formItemBean = builder.build();
    formBuilder.putData(formItemBean.getId(), formItemBean);
    return formItemBean;
  }


  protected ExpectionBuilder createAndOpenSession(String formId, String rev) {
    ImmutableQuestionnaire questionnaire = ImmutableQuestionnaire.builder()
      .metadata(ImmutableQuestionnaireMetadata.builder()
        .formId(formId)
        .formRev(rev)
        .created(new Date())
        .language("en")
        .status(Questionnaire.Metadata.Status.OPEN)
        .build()).build();
    questionnaire = ctx.save(questionnaire);
    return openSession(questionnaire.getId());
  }

  protected ExpectionBuilder createAndOpenSession(Questionnaire questionnaire) {
    questionnaire = ctx.save((ImmutableQuestionnaire) questionnaire);
    return openSession(questionnaire.getId());
  }


  protected ExpectionBuilder createAndOpenSession(String formId) {
    return createAndOpenSession(formId, "LATEST");
  }

  protected ExpectionBuilder createAndOpenSession(Form formDocument) {
    return createAndOpenSession(formDocument.getId(), StringUtils.defaultString(formDocument.getRev(), "LATEST"));
  }
  
  protected ExpectionBuilder openSession(Questionnaire questionnaire) {
    return openSession(ctx.save((ImmutableQuestionnaire) questionnaire).getId());
  }

  protected ExpectionBuilder openSession(String questionnaireId) {
    return steps().when(new WhenMessage("openSession(" + questionnaireId + ")") {
        @Override
        public void accept() throws Exception {
          this.getExpectations().accept(ImmutableActions.builder().from(ctx.openSession(questionnaireId)).build());
        }
      });
  }
}
