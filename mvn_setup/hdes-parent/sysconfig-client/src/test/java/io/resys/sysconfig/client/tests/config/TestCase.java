package io.resys.sysconfig.client.tests.config;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient;
import io.resys.sysconfig.client.api.ExecutorClient.SysConfigSession;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.spi.StencilComposerImpl;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestCase {
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  public TestCaseBuilder builder;
  
  private Map<String, Questionnaire> in_memory_questionnaire = new HashMap<>();
  private static final Instant targetDate = LocalDateTime.of(2023, 1, 1, 1, 1).toInstant(ZoneOffset.UTC);
  public static final Duration atMost = Duration.ofMillis(100000);
  
  
  public static Instant getTargetDate() {
    return targetDate;
  }
  public static String getUserId() {
    return "test-user";
  }

  @BeforeEach
  public void setUp() {
    waitUntilPostgresqlAcceptsConnections(pgPool);
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

  /*
  public Execution<ExecutionDialobBody> save(Execution<ExecutionDialobBody> value) {
    in_memory_questionnaire.put(value.getBody().getQuestionnaire().getId(), value.getBody().getQuestionnaire());
    return value;
  }*/
  
  public Uni<AssetClient> createRepo(String repoId, String testcases) {
    this.builder = new TestCaseBuilder(pgPool, repoId).testcases(testcases);
    return this.builder.getStore().query().deleteAll().onItem()
        .transformToUni(junk -> this.builder.getStore().query().createIfNot())
        .onItem().transform(newStore -> this.builder.withTenant(newStore.getConfig().getRepoId()).getClient()); 
        
  }
  
  
  
  public AssetClient assets() {
    return builder.getClient();
  }
  
  public DialobComposer dialob() {
    return new DialobComposerImpl(builder.getClient().getConfig().getDialob());
  }
  
  public StencilComposer stencil() {
    return new StencilComposerImpl(builder.getClient().getConfig().getStencil());
  }
  
  public HdesComposer hdes() {
    return new HdesComposerImpl(builder.getClient().getConfig().getHdes());
  }
  
  public ExecutorClient executor() {
    return builder.getExecutor();
  }
  
  public SysConfigClient sysConfig() {
    return builder.getSysConfig();
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
  
  public FillTestCase fill(SysConfigSession state) {
    return new FillTestCase(executor(), in_memory_questionnaire, state, null, null);
  }
  
  @AllArgsConstructor
  public static class FillTestCase {
    private final ExecutorClient envir;
    private final Map<String, Questionnaire> mem;
    private SysConfigSession state;
    private String id;
    private Actions output;

    public FillTestCase start() {
      this.mem.put(this.id, state.getQuestionnaire());
      return this;
    }

    public FillTestCase complete() {
      final var input = ImmutableActions.builder()
          .rev(mem.get(id).getRev())
          .addActions(ImmutableAction.builder()
              .type(Action.Type.COMPLETE)
              .build())
          .build();
      
      final var result = envir.fillInstance().session(state).actions(input).build().await().atMost(atMost);
      final var questionnaire = result.getQuestionnaire();
      this.id = questionnaire.getId();
      this.mem.put(this.id, questionnaire);
      this.state = result;
      return this;
    }
    
    public AnswersBuilder answers() {
      return new AnswersBuilder(mem.get(id).getRev()) {
        @Override
        public Actions build() {
          final var input = super.build();
          final var result = envir.fillInstance().session(state).actions(input).build().await().atMost(atMost);
          state = result;
          mem.put(id, result.getQuestionnaire());
          output = result.getActions();
          return input;
        }
      };
    }

    public Actions getActions() {
      return output;
    }

    public SysConfigSession getState() {
      return state;
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
