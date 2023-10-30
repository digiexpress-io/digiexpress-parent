package io.digiexpress.client.tests.support;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;

import io.vertx.mutiny.sqlclient.Pool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import io.dialob.api.proto.Action;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ImmutableAction;
import io.dialob.api.proto.ImmutableActions;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobComposer;
import io.dialob.client.spi.DialobComposerImpl;
import io.digiexpress.client.api.AssetEnvir;
import io.digiexpress.client.api.AssetExecutor.Execution;
import io.digiexpress.client.api.AssetExecutor.ExecutionDialobBody;
import io.digiexpress.client.api.AssetExecutor.QuestionnaireStore;
import io.digiexpress.client.api.AssetExecutorEntity.ProcessState;
import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.Composer;
import io.digiexpress.client.api.ComposerEntity.CreateMigration;
import io.digiexpress.client.spi.ComposerEhCache;
import io.digiexpress.client.spi.ComposerImpl;
import io.digiexpress.client.tests.migration.MigrationsDefaults;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestCase {
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  private TestCaseBuilder builder;
  private QuestionnaireStore questionnaireStore;
  private Map<String, Questionnaire> in_memory_questionnaire = new HashMap<>();
  
  @BeforeEach
  public void setUp() {
    waitUntilPostgresqlAcceptsConnections(pgPool);
    builder = new TestCaseBuilder(pgPool);
    questionnaireStore = new QuestionnaireStore() {
      @Override
      public Questionnaire get(String questionnaireId) {
        return in_memory_questionnaire.get(questionnaireId);
      }
    };
  }

  @AfterEach
  public void tearDown() {
    builder = null;
  }

  private void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    var connection = pool.getConnection()
      .onFailure()
      .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
      .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }

  public AnswersBuilder answers(Execution<ExecutionDialobBody> exec) {
    return new AnswersBuilder(exec.getBody().getActions().getRev());
  }
  
  public Execution<ExecutionDialobBody> save(Execution<ExecutionDialobBody> value) {
    in_memory_questionnaire.put(value.getBody().getQuestionnaire().getId(), value.getBody().getQuestionnaire());
    return value;
  }
  
  public TestCaseBuilder builder(String testcases) {
    return builder.testcases(testcases);
  }
  
  public Client client() {
    return builder.getClient();
  }
  
  public DialobComposer dialob(Client client) {
    return new DialobComposerImpl(client.getConfig().getDialob());
  }
  
  public StencilComposer stencil(Client client) {
    return new StencilComposerImpl(client.getConfig().getStencil());
  }
  
  public HdesComposer hdes(Client client) {
    return new HdesComposerImpl(client.getConfig().getHdes());
  }
  
  public Composer service(Client client) {
    return new ComposerImpl(client, ComposerEhCache.builder().build("test"));
  }
    
  public String toJson(Object v) {
    try {
      final var prettyPrinter = new DefaultPrettyPrinter();
      prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
      return builder.objectMapper
          .writer(prettyPrinter)
          .writeValueAsString(v);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  public Consumer<Throwable> log() {
    return (ex) -> {
      log.error(ex.getMessage(), ex);
    }; 
  }

  public QuestionnaireStore getQuestionnaireStore() {
    return questionnaireStore;
  }
  
  @RequiredArgsConstructor
  public static class AnswersBuilder {
    private final String rev;
    private final List<Action> answered = new ArrayList<>();
    
    public AnswersBuilder answerQuestion(String questionId, String answer) {
      final var action = ImmutableAction.builder()
        .type(Action.Type.ANSWER)
        .answer(answer)
        .id(questionId)
        .build();
      answered.add(action);
      return this;
    }
    
    public Actions build() {
      return ImmutableActions.builder()
          .rev(rev)
          .actions(answered)
          .build();
    }
  }
  
  public FillTestCase fill(AssetEnvir envir, Client client, ProcessState state) {
    return new FillTestCase(envir, client, questionnaireStore, in_memory_questionnaire, state, null, null);
  }
  
  @AllArgsConstructor
  public static class FillTestCase {
    private final AssetEnvir envir;
    private final Client client;
    private final QuestionnaireStore store;
    private final Map<String, Questionnaire> mem;
    private ProcessState state;
    private String id;
    private Actions output;

    public FillTestCase start() {
      final var start = client.executor(envir).dialob(state).store(store).build();
      final var questionnaire = start.getBody().getQuestionnaire();
      this.id = questionnaire.getId();
      this.mem.put(this.id, questionnaire);
      this.state = start.getBody().getState();
      return this;
    }

    public FillTestCase complete() {
      final var input = ImmutableActions.builder()
          .rev(mem.get(id).getRev())
          .addActions(ImmutableAction.builder()
              .type(Action.Type.COMPLETE)
              .build())
          .build();
      
      final var result = client.executor(envir).dialob(state).store(store).actions(input).build();
      final var questionnaire = result.getBody().getQuestionnaire();
      this.id = questionnaire.getId();
      this.mem.put(this.id, questionnaire);
      this.state = result.getBody().getState();
      return this;
    }
    
    public AnswersBuilder answers() {
      return new AnswersBuilder(mem.get(id).getRev()) {
        @Override
        public Actions build() {
          final var input = super.build();
          final var result = client.executor(envir).dialob(state).store(store).actions(input).build();
          state = result.getBody().getState();
          mem.put(id, result.getBody().getQuestionnaire());
          output = result.getBody().getActions();
          return input;
        }
      };
    }

    public Actions getActions() {
      return output;
    }

    public ProcessState getState() {
      return state;
    }
  }
  
  public void writeOutput(CreateMigration migration) {
    try {
      final var file = new File(MigrationsDefaults.folder + "output/ImmutableCreateMigration.json");
      file.createNewFile();
      
      MigrationsDefaults.om.writer().withDefaultPrettyPrinter().writeValue(file, migration);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
//@Test
//public void tojson() throws JsonProcessingException {
//  ImmutableBatchSite site = ImmutableBatchSite.builder()
//  .addLocales(ImmutableCreateLocale.builder().locale("en").build())
//  .addArticles(ImmutableCreateArticle.builder().name("index").build())
//  .addPages(ImmutableCreatePage.builder().content("# This is opening page").articleId("index").locale("en").build())
//  .addWorkflows(ImmutableCreateWorkflow.builder().addArticles("index").value("general-message").addLabels(ImmutableLocaleLabel.builder().locale("en").labelValue("send us a message using a form").build()).build())
//  .build();
//  
//  final var res = new ObjectMapper().writeValueAsString(site);
//  
//  System.out.println(res);
//}
}
