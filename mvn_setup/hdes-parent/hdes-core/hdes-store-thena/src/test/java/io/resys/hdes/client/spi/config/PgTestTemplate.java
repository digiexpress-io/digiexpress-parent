package io.resys.hdes.client.spi.config;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.ThenaStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.util.RepositoryToStaticData;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.models.git.GitPrinter;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.store.sql.DbStateSqlImpl;
import io.resys.thena.docdb.store.sql.PgErrors;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PgTestTemplate {
  private ThenaStore store;
  
  @Inject
  io.vertx.mutiny.pgclient.PgPool pgPool;

  @BeforeEach
  public void setUp(TestInfo testInfo) {
    final AtomicInteger gid = new AtomicInteger(0);
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.store = ThenaStore.builder()
        .repoName("")
        .pgPool(pgPool)
        .objectMapper(TestUtils.objectMapper)
        .gidProvider((type) -> type + "-" + gid.incrementAndGet())
        .build();
  }
  
  @AfterEach
  public void tearDown() {
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

  private DbState createState(String repoName) {
    final var ctx = DbCollections.defaults(repoName);
    return DbStateSqlImpl.state(ctx, pgPool, new PgErrors());
  }
  
  public void printRepo(Repo repo) {
    final String result = new GitPrinter(createState(repo.getName())).print(repo);
    log.debug(result);
  }
  
  public void prettyPrint(String repoId) {
    Repo repo = getThena().git().project().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo(); 
    printRepo(repo);
  }

  public String toRepoExport(String repoName) {
    final var repo = getThena().git().project().projectName(repoName).get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = new RepositoryToStaticData(createState(repoName)).print(repo);
    return result;
  }

  public DocDB getThena() {
    return store.getConfig().getClient();
  }
  
  public HdesClient getClient() {
    return HdesClientImpl.builder().objectMapper(TestUtils.objectMapper).store(store)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return null;
          }
        })
        .serviceInit(new ServiceInit() {
            @Override
            public <T> T get(Class<T> type) {
              try {
                return type.getDeclaredConstructor().newInstance();
              } catch(Exception e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            }
          })
        .build();
  }
  
  public HdesComposer getComposer() {
    return new HdesComposerImpl(getClient());
  }
  
}
