package io.resys.limaone.tests;

/*-
 * #%L
 * limaone-compiler
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

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.resys.limaone.tests.support.DbSupport;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.thena.test.DialobTest;
import io.resys.thena.test.DialobTest.DialobResetDB;
import io.resys.thena.test.DialobTest.FormUrl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@DialobTest(enabled = true)
public class FormStepTest extends DbSupport {

  @Test @DialobResetDB
  public void testFormStep(FormUrl formUrl) {
    final var formDb = TestTemplate.getFormDb(formUrl);

    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(Duration.ofMinutes(1));

    final var sessionId = formDb.withTenant().createFormInstance()
        .formId(created.getId())
        .context(Map.of(
            "FirstNames", "Sam",
            "LastName", "Vimes",
            "SocialSecurityNumber", "123456-789A",
            "Email", "sam.vimes@resys.io",
            "Address", "Treacle Mine Road"
        ))
        .build().await().atMost(Duration.ofMinutes(1));

    completeForm(formDb, formDb.withTenant()
        .formInstanceQuery()
        .getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1)));

    final var config = createConfig(formDb);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-test")
            .body("""
id: form-step-test
description:

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Collect data:
    id: "collectData"
    then: end
    form:
      ref: questionnaireId
      returns: |
        final String firstName = form.context "FirstNames"
        final String lastName = form.context "LastName"
        final String ssn = form.context "SocialSecurityNumber"
        final String email = form.context "Email"
        String protectionOrderString = form.context "ProtectionOrder"
        final Boolean protectionOrder = protectionOrderString == "true"
        final String formId = form.metadata().getFormId()
        final String label = form.metadata().getLabel()
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var bundle = runtime.getBundle();

    final var flow = bundle.queryFlows().name("form-step-test").getOne();
    final var result = flow.run(DefaultProgramInput.of(Map.of(
        "questionnaireId", sessionId.getId()
    ))).andGetBody();

    log.debug("Form step results: {}", JsonObject.mapFrom(result.getReturns()).encodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, result.getStatus());
    Assertions.assertEquals("Sam", result.getReturns().get("firstName"));
    Assertions.assertEquals("Vimes", result.getReturns().get("lastName"));
    Assertions.assertEquals("123456-789A", result.getReturns().get("ssn"));
    Assertions.assertEquals("sam.vimes@resys.io", result.getReturns().get("email"));
    Assertions.assertEquals(false, result.getReturns().get("protectionOrder"));
    Assertions.assertNotNull(result.getReturns().get("formId"));
    Assertions.assertEquals("palaute", result.getReturns().get("label"));
    Assertions.assertNull(result.getReturns().get("protectionOrderString"));
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
  {'type':'ANSWER','answer':'thank you','id':'feedBackTitle'},
  {'type':'ANSWER','answer':'very big text','id':'feedBackTxt'},
  {'type':'ANSWER','answer':false,'id':'boolean11'},
  {'type':'COMPLETE'}
]""".replace("'", "\""))).encode())
        .build()
        .await().atMost(Duration.ofMinutes(1));

    final var completedSession = formDb.withTenant()
        .formInstanceQuery()
        .getOne(instance.getQuestionnaire().getId())
        .await().atMost(Duration.ofMinutes(1));

    Assertions.assertEquals(
        Questionnaire.Metadata.Status.COMPLETED,
        completedSession.getQuestionnaire().getMetadata().getStatus(),
        "Expected completed filled session");
  }

  @Test
  public void testFormStepWithInvalidRef() {
    final var config = createConfig(null);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-invalid-ref")
            .body("""
id: form-step-invalid-ref
description: Test invalid form reference

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Collect data:
    id: "collectData"
    then: end
    form:
      ref: nonExistentInput
      returns: |
        final String firstName = form.context "FirstNames"
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var flow = runtime.getBundle().queryFlows().name("form-step-invalid-ref").getOne();

    // Check that the AST contains validation errors
    Assertions.assertFalse(flow.getAst().getErrors().isEmpty(), "Expected validation errors for invalid form ref");
    
    // Verify the error message contains expected text
    final var errorMessages = flow.getAst().getErrors().stream()
        .map(error -> error.getMsg())
        .toList();
    Assertions.assertTrue(
        errorMessages.stream().anyMatch(msg -> msg.contains("nonExistentInput") && msg.contains("unknown input")),
        "Expected error about nonExistentInput being an unknown input, but got: " + errorMessages
    );
  }

  @Test
  public void testFormStepCompilationError() {
    final var config = createConfig(null);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-compile-error")
            .body("""
id: form-step-compile-error
description:

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Collect data:
    id: "collectData"
    then: end
    form:
      ref: questionnaireId
      returns: |
        final String firstName = form.context "FirstNames"
        final String lastName = form.context "LastName"
        final String broken = this is not valid groovy
        final String email = form.context "Email"
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var flow = runtime.getBundle().queryFlows().name("form-step-compile-error").getOne();

    Assertions.assertFalse(flow.getAst().getErrors().isEmpty(),
        "Expected compilation errors for invalid Groovy code");

    final var errors = flow.getAst().getErrors();
    log.debug("Compilation errors:");
    for (final var error : errors) {
      log.debug("  line={}, msg={}", error.getLine(), error.getMsg());
    }

    final var firstError = errors.get(0);
    Assertions.assertTrue(firstError.getLine() != null && firstError.getLine() == 17,
        "Error should point to line 17 of the flow (returns line 14 + code line 3), but was: " + firstError.getLine());
  }

  @Test
  public void testFormStepWithEmptyReturns() {
    final var config = createConfig(null);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-empty-returns")
            .body("""
id: form-step-empty-returns
description: Test form step with empty returns

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Collect data:
    id: "collectData"
    then: end
    form:
      ref: questionnaireId
      returns: |
        
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var flow = runtime.getBundle().queryFlows().name("form-step-empty-returns").getOne();

    Assertions.assertFalse(flow.getAst().getErrors().isEmpty(),
        "Expected validation error for empty returns");
    
    final var errorMessages = flow.getAst().getErrors().stream()
        .map(error -> error.getMsg())
        .toList();
    Assertions.assertTrue(
        errorMessages.stream().anyMatch(msg -> msg.contains("non-empty 'returns' block")),
        "Expected error about non-empty returns block, but got: " + errorMessages
    );
  }

  @Test
  public void testFormStepInstanceNotFound() {
    final var config = createConfig(null);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-not-found")
            .body("""
id: form-step-not-found
description: Test form instance not found

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Collect data:
    id: "collectData"
    then: end
    form:
      ref: questionnaireId
      returns: |
        final String result = "processed"
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var flow = runtime.getBundle().queryFlows().name("form-step-not-found").getOne();

    final var result = flow.run(DefaultProgramInput.of(Map.of(
        "questionnaireId", "non-existent-id-12345"
    ))).andGetBody();

    Assertions.assertEquals(FlowExecutionStatus.ERROR, result.getStatus(),
        "Expected flow to fail when FormDb is not configured, but got status: " + result.getStatus());

  }

  @Test @DialobResetDB
  public void testFormStepWithNullValues(FormUrl formUrl) {
    final var formDb = TestTemplate.getFormDb(formUrl);

    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(Duration.ofMinutes(1));

    final var sessionId = formDb.withTenant().createFormInstance()
        .formId(created.getId())
        .context(Map.of(
            "FirstNames", "Sam",
            "LastName", "Vimes"
        ))
        .build().await().atMost(Duration.ofMinutes(1));

    completeForm(formDb, formDb.withTenant()
        .formInstanceQuery()
        .getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1)));

    final var config = createConfig(formDb);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-null-values")
            .body("""
id: form-step-null-values
description: Test handling of null values

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Collect data:
    id: "collectData"
    then: end
    form:
      ref: questionnaireId
      returns: |
        final String firstName = form.context "FirstNames"
        final String lastName = form.context "LastName"
        final String ssn = form.context "SocialSecurityNumber"
        final String email = form.context "Email"
        final String address = form.context "Address"
        final Boolean hasAddress = address != null
        final String defaultEmail = email != null ? email : "no-email@example.com"
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var flow = runtime.getBundle().queryFlows().name("form-step-null-values").getOne();

    final var result = flow.run(DefaultProgramInput.of(Map.of(
        "questionnaireId", sessionId.getId()
    ))).andGetBody();

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, result.getStatus());
    Assertions.assertEquals("Sam", result.getReturns().get("firstName"));
    Assertions.assertEquals("Vimes", result.getReturns().get("lastName"));
    Assertions.assertNull(result.getReturns().get("ssn"));
    Assertions.assertNull(result.getReturns().get("email"));
    Assertions.assertNull(result.getReturns().get("address"));
    Assertions.assertEquals(false, result.getReturns().get("hasAddress"));
    Assertions.assertEquals("no-email@example.com", result.getReturns().get("defaultEmail"));
  }

  @Test @DialobResetDB
  public void testUsingFormStepOutputsInOtherSteps(FormUrl formUrl) {
    final var formDb = TestTemplate.getFormDb(formUrl);

    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(Duration.ofMinutes(1));

    final var sessionId = formDb.withTenant().createFormInstance()
        .formId(created.getId())
        .context(Map.of(
            "FirstNames", "Alice",
            "LastName", "Smith",
            "Email", "alice.smith@example.com",
            "SocialSecurityNumber", "111111-1111"
        ))
        .build().await().atMost(Duration.ofMinutes(1));

    completeForm(formDb, formDb.withTenant()
        .formInstanceQuery()
        .getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1)));

    final var config = createConfig(formDb);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-with-output-usage")
            .body("""
id: form-step-with-output-usage
description: Test form step with output usage

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Extract form data:
      id: "extractData"
      then: returnData
      form:
        ref: questionnaireId
        returns: |
          final String firstName = form.context("FirstNames")
          final String lastName = form.context("LastName")
          final String email = form.context("Email")
          final String ssn = form.context("SocialSecurityNumber")
  - Return extracted data:
      id: "returnData"
      then: end
      returns:
        inputs:
          firstName: extractData.firstName
          lastName: extractData.lastName
          email: extractData.email
          ssn: extractData.ssn
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var flow = runtime.getBundle().queryFlows().name("form-step-with-output-usage").getOne();

    final var result = flow.run(DefaultProgramInput.of(Map.of(
        "questionnaireId", sessionId.getId()
    ))).andGetBody();

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, result.getStatus());

    Assertions.assertEquals("Alice", result.getReturns().get("firstName"));
    Assertions.assertEquals("Smith", result.getReturns().get("lastName"));
    Assertions.assertEquals("alice.smith@example.com", result.getReturns().get("email"));
    Assertions.assertEquals("111111-1111", result.getReturns().get("ssn"));

  }

  @Test @DialobResetDB
  @SuppressWarnings("unchecked")
  public void testFormStepWithGenericTypes(FormUrl formUrl) {
    final var formDb = TestTemplate.getFormDb(formUrl);

    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(Duration.ofMinutes(1));

    final var sessionId = formDb.withTenant().createFormInstance()
        .formId(created.getId())
        .context(Map.of("FirstNames", "Sam"))
        .build().await().atMost(Duration.ofMinutes(1));

    completeForm(formDb, formDb.withTenant()
        .formInstanceQuery()
        .getOne(sessionId.getId())
        .await().atMost(Duration.ofMinutes(1)));

    final var config = createConfig(formDb);
    final var authoring = new AuthoringImpl(config);

    authoring.newModel().newFlow()
        .props(props -> props
            .name("form-step-generics")
            .body("""
id: form-step-generics
description: Test generic type handling

inputs:
  questionnaireId:
    required: true
    type: STRING
tasks:
  - Collect data:
    id: "collectData"
    then: end
    form:
      ref: questionnaireId
      returns: |
        final List<String> names = new ArrayList<>()
        names.add(form.context("FirstNames").toString())
        final Map<String, Object> metadata = new HashMap<>()
        metadata.put("formId", form.metadata().getFormId())
""")
            .build())
        .buildSync();

    final var compiler = new CompilerImpl(config.getEnvir());
    final var world = authoring.worldQuery().findAllSync();
    final var runtime = compiler.compile(world).id(world.getName()).build();
    final var flow = runtime.getBundle().queryFlows().name("form-step-generics").getOne();

    Assertions.assertTrue(flow.getAst().getErrors().isEmpty(),
        "Expected no AST errors, got: " + flow.getAst().getErrors());

    final var result = flow.run(DefaultProgramInput.of(Map.of(
        "questionnaireId", sessionId.getId()
    ))).andGetBody();

    log.debug("Form step (generics) results: {}", JsonObject.mapFrom(result.getReturns()).encodePrettily());

    Assertions.assertEquals(FlowExecutionStatus.COMPLETED, result.getStatus());
    final var returns = result.getReturns();

    final var names = (List<Object>) returns.get("names");
    Assertions.assertNotNull(names);
    Assertions.assertEquals(1, names.size());
    Assertions.assertEquals("Sam", names.get(0));

    final var metadata = (Map<String, Object>) returns.get("metadata");
    Assertions.assertNotNull(metadata);
    Assertions.assertNotNull(metadata.get("formId"));
  }
}
