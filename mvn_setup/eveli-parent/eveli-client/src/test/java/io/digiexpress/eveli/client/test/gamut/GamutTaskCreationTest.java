package io.digiexpress.eveli.client.test.gamut;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Map;

import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.spi.gamut.GamutClientImpl;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.program.ImmutableParticipant;
import io.resys.limaone.program.ImmutableParticipantForm;
import io.resys.limaone.program.ImmutableParticipantId;
import io.resys.limaone.program.WorkflowProgram.WorkflowExecutionStatus;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@DialobTest(enabled = true)
public class GamutTaskCreationTest extends GamutTestConfig {

  @Test @DialobResetDB
  public void testGamutTaskCreation(FormUrl formUrl) {

    // set up Dialob form + limaone authoring (article, workflow, flow, flow task)
    final var formDb = createFormDb(formUrl);
    final var authoringConfig = createAuthoringConfig(formDb);
    final var authoring = new AuthoringImpl(authoringConfig);
    setupAuthoringData(formDb, authoring);

    // compile all resources into a runtime bundle
    final var compiler = new CompilerImpl(authoringConfig.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();

    // create a user action (process + form session) via UserActionsBuilderImpl (same path as the controller)
    // UserAction is the portal's representation of an in-progress form — no task exists yet
    final var user = ImmutableParticipant.builder()
        .identity("010199-123A")
        .username("portal-user-id")
        .firstName("John")
        .lastName("Doe")
        .language("fi")
        .email("john.doe@example.com")
        .anon(false)
        .protectionOrder(false)
        .partId(ImmutableParticipantId.builder().hashId("hash1").realId("real1").build())
        .build();

    final var gamutClient = new GamutClientImpl(taskClient, null, null, runtime);

    final var userAction = gamutClient.userActionBuilder()
        .actionId("gamut-task-1")
        .clientLocale("fi")
        .inputContextId("task_article")
        .participant(user)
        .createOne()
        .await().atMost(AT_MOST);

    Assertions.assertNotNull(userAction.getId(), "Process ID should be assigned");
    Assertions.assertNotNull(userAction.getFormId(), "Form session (questionnaire) should be created");
    Assertions.assertEquals("gamut-task-1", userAction.getName());
    Assertions.assertEquals(true, userAction.getFormInProgress());
    Assertions.assertEquals("CREATED", userAction.getStatus());

    log.debug("User action created: processId={}, formId={}, name={}, status={}",
        userAction.getId(), userAction.getFormId(), userAction.getName(), userAction.getStatus());

    // fill and complete the form
    final var session = formDb.withTenant()
        .formInstanceQuery()
        .includeForm(true)
        .getOne(userAction.getFormId())
        .await().atMost(AT_MOST);
    completeForm(formDb, session);

    // run the flow — flow extracts form data and creates the actual task via TaskBuilder
    final var workflow = runtime.getBundle().queryWorkflows().name("gamut-task-1").getOne();
    final var flowResult = workflow.runFlow(
        ImmutableParticipantForm.builder().questionnaireId(userAction.getFormId()).build(),
        DefaultProgramInput.of(Map.of())
    );

    Assertions.assertEquals(WorkflowExecutionStatus.COMPLETED, flowResult.getStatus());
    Assertions.assertTrue(flowResult.getFlow().isPresent(), "Flow result should be present");

    final var taskData = flowResult.getFlow().get().getReturns();
    log.debug("Flow output: {}", JsonObject.mapFrom(taskData).encodePrettily());

    // verify the task data was extracted correctly
    Assertions.assertEquals("John", taskData.get("firstName"));
    Assertions.assertEquals("Doe", taskData.get("lastName"));
    Assertions.assertEquals("010199-123A", taskData.get("ssn"));
    Assertions.assertEquals("john.doe@example.com", taskData.get("email"));
    Assertions.assertEquals(null, taskData.get("address"));
    Assertions.assertEquals("fi", taskData.get("localization"));
    Assertions.assertEquals("palaute", taskData.get("label"));
    Assertions.assertNotNull(taskData.get("formId"));
    Assertions.assertEquals(session.getQuestionnaire().getId(), taskData.get("questionnaireId"));

    final var taskId = (String) taskData.get("taskId");
    Assertions.assertNotNull(taskId, "TaskBuilder should have created a task and returned its ID");

    // verify the task was actually created in the store
    final var createdTask = taskClient.queryTasks().getOneById(taskId).await().atMost(AT_MOST);
    Assertions.assertNotNull(createdTask, "Task should exist in the store");
    Assertions.assertEquals("palaute", createdTask.getSubject());
    Assertions.assertTrue(createdTask.getDescription().contains("John"));
    Assertions.assertTrue(createdTask.getDescription().contains("Doe"));
    Assertions.assertEquals(TaskStatus.NEW, createdTask.getStatus());
    Assertions.assertEquals(TaskPriority.NORMAL, createdTask.getPriority());
    Assertions.assertTrue(createdTask.getKeyWords().contains("Questionnaire"));

    log.debug("Gamut task verified: id={}, subject={}, status={}, ref={}",
        createdTask.getId(), createdTask.getSubject(), createdTask.getStatus(), createdTask.getTaskRef());

    final var originalProcess = taskClient.queryTaskProcesess()
      .findOneById(userAction.getId())
      .await().atMost(AT_MOST);

    // process should still be in CREATED status and not have a taskId yet
    Assertions.assertEquals(
      GrimProcessStatus.CREATED,
      originalProcess.get().getStatus());
    Assertions.assertNull(originalProcess.get().getTaskId());

    // update the process to link it with the created task
    taskClient.modifyProcess()
        .commitAuthor("system")
        .commitMessage("flow completed, linking task")
        .id(userAction.getId())
        .merge((current, merger) -> merger
            .status(GrimProcessStatus.ANSWERED)
            .taskId(taskId)
            .flowBody(JsonObject.mapFrom(taskData).encode())
            .build())
        .build()
        .await().atMost(AT_MOST);

    final var updatedProcess = taskClient.queryTaskProcesess()
        .findOneById(userAction.getId())
        .await().atMost(AT_MOST);

    // process should be updated to ANSWERED status and linked to the created task
    Assertions.assertTrue(updatedProcess.isPresent(), "Process should still exist after update");
    Assertions.assertEquals(
        GrimProcessStatus.ANSWERED,
        updatedProcess.get().getStatus());
    Assertions.assertEquals(taskId, updatedProcess.get().getTaskId(), "Process should be linked to the created task");

    log.debug("Gamut task fully processed: processId={}, taskId={}, status={}",
        updatedProcess.get().getId(), updatedProcess.get().getTaskId(), updatedProcess.get().getStatus());
  }


  @SuppressWarnings("unused")
  private void completeForm(FormDb formDb, FormInstance instance) {
    final var formFilled = formDb.withTenant().createFormFill()
        .formInstanceId(instance.getQuestionnaire().getId())
        .actions(JsonObject.of(
            "rev", instance.getQuestionnaire().getRev(),
            "actions", new JsonArray(
"""
[
  {'type':'ANSWER','answer':'no','id':'authentication'},
  {'type':'ANSWER','answer':'cityService','id':'mainList'},
  {'type':'ANSWER','answer':'info','id':'cityServiceMainList'},
  {'type':'ANSWER','answer':'thanks','id':'typeOfFeedback'},
  {'type':'ANSWER','answer':'feedback title','id':'feedBackTitle'},
  {'type':'ANSWER','answer':'feedback text','id':'feedBackTxt'},
  {'type':'ANSWER','answer':false,'id':'boolean11'},
  {'type':'COMPLETE'}
]""".replace("'", "\""))).encode())
        .build()
        .await().atMost(AT_MOST);

    final var completedSession = formDb.withTenant()
        .formInstanceQuery()
        .getOne(instance.getQuestionnaire().getId())
        .await().atMost(AT_MOST);

    Assertions.assertEquals(
        Questionnaire.Metadata.Status.COMPLETED,
        completedSession.getQuestionnaire().getMetadata().getStatus(),
        "Expected completed filled session");
  }


  private void setupAuthoringData(FormDb formDb, AuthoringImpl authoring) {
    final var form = new JsonObject(loadResource("gamut/palaute.json")).mapTo(Form.class);
    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(AT_MOST);

    final var tag = formDb.withTenant().createFormTag()
        .formName(created.getName()).formVersion("task-form-tag")
        .build()
        .await().atMost(AT_MOST);

    final var article = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("task_article").order(100))
        .buildSync();

    final var locale = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("fi"))
        .buildSync();

    authoring.newModel()
        .newArticlePage()
        .props(props -> props.articleId(article.getId()).locale(locale.getId()).content("# Task submission"))
        .buildSync();

    authoring.newModel()
        .newArticleWorkflow().props(props -> props
          .value("gamut-task-1")
          .formName(form.getName()).formTag(tag.getName()).flowName("taskCreationFlow")
          .formId("task-form-id")
          .addLabels(ImmutableLocaleLabel.builder().locale(locale.getId()).labelValue("taskForm").build())
          .build())
        .buildSync();

    authoring.newModel().newFlowTask()
        .props(props -> props
            .name("ExtractTaskData")
            .body("""
public class ExtractTaskData {

  public Output execute(Input input, Runtime runtime) {
    Output output = new Output();

    final var dialob = runtime.getProperties().getFormDb().withTenant().formInstanceQuery()
        .getOneSync(input.questionnaireId);

    output.firstName = dialob.context "FirstNames"
    output.lastName = dialob.context "LastName"
    output.ssn = dialob.context "SocialSecurityNumber"
    output.address = dialob.context "Address"
    output.email = dialob.context "Email"

    output.localization = dialob.variable "localization"

    output.formId = dialob.metadata().getFormId()
    output.label = dialob.metadata().getLabel()
    output.questionnaireId = input.questionnaireId

    return output;
  }

  @ServiceData
  public static class Input implements Serializable {
    String workflowName;
    String questionnaireId;
  }

  @ServiceData
  public static class Output implements Serializable {
    String firstName;
    String lastName;
    String ssn;
    String email;
    String address;

    String localization;

    String label;
    String questionnaireId;
    String formId;
  }
}
            """)
            .build())
        .buildSync();

    authoring.newModel().newFlowTask()
        .props(props -> props
            .name("TaskBuilder")
            .body("""
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommand;

public class TaskBuilder {

  public Output execute(Input input, Runtime runtime) {
    final var taskClient = runtime.getProperties().getBean(TaskClient.class)

    final var command = ImmutableCreateTaskCommand.builder()
      .subject(input.label)
      .description("Customer " + input.firstName + " " + input.lastName + ", SSN " + input.ssn + ", " + input.email)
      .clientIdentificator(input.firstName + " " + input.lastName)
      .priority(TaskPriority.NORMAL)
      .status(TaskStatus.NEW)
      .addKeyWords("Questionnaire")
      .questionnaireId(input.questionnaireId)
      .build()

    Task createdTask = taskClient.taskBuilder()
      .userId(input.ssn, input.email)
      .createTask(command)
      .await().atMost(Duration.ofMinutes(1))

    Output output = new Output()
    output.taskId = createdTask.id
    return output
  }

  @ServiceData
  public static class Input implements Serializable {
    String firstName;
    String lastName;
    String ssn;
    String email;
    String address;
    String label;
    String questionnaireId;
  }

  @ServiceData
  public static class Output implements Serializable {
    String taskId;
  }
}
            """)
            .build())
        .buildSync();

    authoring.newModel().newFlow()
        .props(props -> props
            .name("taskCreationFlow")
            .body("""
id: taskCreationFlow
description:

inputs:
  workflowName:
    required: true
    type: STRING
    debugValue: "1"
  questionnaireId:
    required: true
    type: STRING
    debugValue: "1"
tasks:
  - Extract task data:
      id: "extract_task_data"
      then: "create_task"
      service:
        ref: ExtractTaskData
        collection: false
        inputs:
          workflowName: workflowName
          questionnaireId: questionnaireId

  - Create task:
      id: "create_task"
      then: "return_results"
      service:
        ref: TaskBuilder
        collection: false
        inputs:
          firstName: extract_task_data.firstName
          lastName: extract_task_data.lastName
          ssn: extract_task_data.ssn
          email: extract_task_data.email
          address: extract_task_data.address
          label: extract_task_data.label
          questionnaireId: extract_task_data.questionnaireId

  - Return results:
      id: "return_results"
      then: end
      returns:
        collection: false
        inputs:
          firstName: extract_task_data.firstName
          lastName: extract_task_data.lastName
          ssn: extract_task_data.ssn
          email: extract_task_data.email
          address: extract_task_data.address
          localization: extract_task_data.localization
          label: extract_task_data.label
          questionnaireId: extract_task_data.questionnaireId
          formId: extract_task_data.formId
          taskId: create_task.taskId
""")
            .build())
        .buildSync();
  }
}
