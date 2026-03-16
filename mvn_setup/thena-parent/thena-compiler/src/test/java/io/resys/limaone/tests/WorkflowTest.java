package io.resys.limaone.tests;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.api.form.Form;
import io.dialob.api.questionnaire.Questionnaire;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.program.ImmutableWorkflowDefaultProps;
import io.resys.limaone.program.ImmutableWorkflowUser;
import io.resys.limaone.program.WorkflowProgram.WorkflowExecutionStatus;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.program.DefaultRuntime;
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
@DialobTest( enabled = true )
public class WorkflowTest extends DbSupport {

  
  @Test @DialobResetDB
  public void testWorkflow(FormUrl formUrl) {
    // set up implementations
    final var formDb = withData(TestTemplate.getFormDb(formUrl));
    final var authoring = new AuthoringImpl(createConfig(formDb));


    // compile all resources
    final var compiler = CompilerImpl.builder()
      .formDb(formDb)
      .astParser(authoring.getConfig().getAstParser())
      .build();


    // magic asset bundle ... contains all we need to run dialob/wrench/stencil
    final var world = authoring.worldQuery().findAllSync();
    final var bundle = compiler.compile(world).id(world.getName()).build();
    final var runtime = DefaultRuntime.withBundle(bundle).withFormDb(formDb);

    // get and run the workflow
    final var workflow = bundle.queryWorkflows().name("form-1").getOne();
    final var workflowProps = ImmutableWorkflowDefaultProps.builder().build();
    final var user = ImmutableWorkflowUser.builder()
        .identity("123456-789A")
        .firstName("Sam")
        .lastName("Vimes")
        .language("fi")
        .email("sam.vimes@resys.io")
        .anon(false)
        .build();


    // Assert questionnaire
    final var workflowResult = workflow.runForm(runtime, user, workflowProps);
    Assertions.assertEquals(workflowResult.getAccessAllowed(), true);
    Assertions.assertEquals(workflowResult.getForm().isPresent(), true);
    
    
    //  Assert the created data
    final var form = workflowResult.getForm().get();
    Assertions.assertEquals(false, form.getAssignment());
    Assertions.assertEquals("flow1", form.getFlowName());
    Assertions.assertEquals("testi1", form.getFormName());
    Assertions.assertEquals("my-first-tag", form.getFormVersion());
    Assertions.assertNotNull(form.getFormSessionId());
    Assertions.assertEquals("form-1", form.getWorkflowName());
    Assertions.assertEquals("main", form.getTagName());


    // retrieve created session
    // complete the session
    final var session = formDb.withTenant()
        .formInstanceQuery()
        .includeForm(true)
        .getOne(form.getFormSessionId())
        .await().atMost(Duration.ofMinutes(1));
    completeForm(formDb, session);

    // prints form in a readable manner
    // log.debug("Form data:\n{}", session.encodeFormPrettily().get());

    // run the flow
    final var flow = workflow.runFlow(runtime, form, DefaultProgramInput.of(Map.of(), runtime));
    Assertions.assertEquals(WorkflowExecutionStatus.COMPLETED, flow.getStatus());
    final var resultProps = flow.getFlow().get().getReturns();
    
    log.debug("Flow results {}", JsonObject.mapFrom(resultProps).encodePrettily());
    
    Assertions.assertEquals(null, resultProps.get("sum"));
    Assertions.assertEquals("Sam", resultProps.get("firstName"));
    Assertions.assertEquals("Vimes", resultProps.get("lastName"));
    Assertions.assertEquals("123456-789A", resultProps.get("ssn"));
    Assertions.assertEquals("sam.vimes@resys.io", resultProps.get("email"));
    Assertions.assertEquals(null, resultProps.get("address"));
    Assertions.assertEquals("", resultProps.get("title"));
    Assertions.assertEquals(null, resultProps.get("taskFeature"));
    Assertions.assertEquals("", resultProps.get("feedBackType"));
    Assertions.assertEquals("", resultProps.get("feedBackTxt"));
    Assertions.assertEquals("fi", resultProps.get("localization"));
    Assertions.assertEquals(null, resultProps.get("language"));
    Assertions.assertEquals("palaute", resultProps.get("label"));
    Assertions.assertEquals(session.getQuestionnaire().getId(), resultProps.get("questionnaireId"));
  }
  
  
  @SuppressWarnings("unused")
  public void completeForm(FormDb formDb, FormInstance instance) {
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
  
  
  
  @SuppressWarnings("unused")
  public FormDb withData(FormDb formDb) {
    
    final var form = new JsonObject(TestTemplate.toString("forms/palaute.json")).mapTo(Form.class);
    final var created = formDb.withTenant().createForm()
        .props(form).build()
        .await().atMost(Duration.ofMinutes(1));
    
    final var tag = formDb.withTenant().createFormTag()
        .formName(created.getName()).formVersion("my-first-tag")
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    
    final var authoring = new AuthoringImpl(createConfig());
    final var article = authoring.newModel()
        .newArticle()
        .props(builder -> builder.name("my_first_article").order(100))
        .buildSync();
    
    final var locale = authoring.newModel()
        .newLocale()
        .props(props -> props.locale("en"))
        .buildSync();

    final var page = authoring.newModel()
        .newArticlePage()
        .props(props -> props.articleId(article.getId()).locale(locale.getId()).content("# English content"))
        .buildSync();
    
    final var workflow = authoring.newModel()
        .newArticleWorkflow().props(props -> props
          .value("form-1")
          .formName(form.getName()).formTag(tag.getName()).flowName("flow1")
          .formId("external-form-id")
          .addLabels(ImmutableLocaleLabel.builder().locale(locale.getId()).labelValue("firstForm").build())
          .build())
        .buildSync();

    
    authoring.newModel().newFlowTask()
    .props(props -> props
        .name("ExtractDialobData")
        .body("""
public class ExtractDialobData {

  public Output execute(Input input, Runtime runtime) {
    Output output = new Output();
    
    final var dialob = runtime.getFormDb().withTenant().formInstanceQuery()
        .getOneSync(input.questionnaireId);

    output.firstName = dialob.context "FirstNames"
    output.lastName = dialob.context "LastName"
    output.ssn = dialob.context "SocialSecurityNumber"
    output.address = dialob.context "Address"
    output.email = dialob.context "Email"
    output.language = dialob.context "language"
    
    output.feedBackType = dialob.text "feedBackType"
    output.feedBackTxt = dialob.text "feedBackText"
    output.title = dialob.text "feedBackTitle"
    output.localization = dialob.variable "localization"


    if(dialob.bool "publicAnswerAllowed"){
      output.taskFeature = "feedback,feedback-ai"
    }

    if(dialob.bool "publishFeedBack"){
      output.taskFeature = "feedback,feedback-ai"
    }
    
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
    // null test
    Integer sum;
    
    String firstName;
    String lastName;
    String ssn;
    String email;
    String address;
    // feedback data
    String title;
    String taskFeature;
    String feedBackType;
    String feedBackTxt;
    String localization;
    String language;
    // dialob base date
    String label;
    String questionnaireId;
    String formId;
  }
}
            """)
        .build())
    .buildSync();
    
    authoring.newModel().newFlow()
      .props(props -> props
          .name("flow1")
          .body("""
id: flow1
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
  - Extract dialob data:
    id: "extract_dialob_data"
    then: end
    service:
      ref: ExtractDialobData
      collection: false
      inputs:
        workflowName: workflowName
        questionnaireId: questionnaireId    
""")
          .build())
      .buildSync();
    
    
    return formDb;
  }
}
