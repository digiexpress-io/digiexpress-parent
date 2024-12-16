package io.resys.hdes.client.test.config;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.store.ThenaStore;
import io.resys.hdes.client.spi.util.RepositoryToStaticData;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.git.GitPrinter;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

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
    final var ctx = TenantTableNames.defaults(repoName);
    return DbStateSqlImpl.create(ctx, pgPool);
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GitPrinter(createState(repo.getName())).print(repo);
    log.debug(result);
  }

  public String toRepoExport(String repoName) {
    final var repo = getThena().git(repoName).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = new RepositoryToStaticData(createState(repoName)).print(repo);
    return result;
  }

  public ThenaClient getThena() {
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
