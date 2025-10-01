package io.digiexpress.tagomi.tests.config;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.digiexpress.tagomi.api.TagomiStore;
import io.digiexpress.tagomi.spi.ImmutableTagomiStoreConfig;
import io.digiexpress.tagomi.spi.TagomiStoreImpl;
import io.digiexpress.tagomi.spi.json.FromJsonObject;
import io.digiexpress.tagomi.spi.json.ToJsonObject;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.git.api.GitClient;
import io.resys.thena.git.api.GitDataSource;
import io.resys.thena.git.spi.GitDataSourceImpl;
import io.resys.thena.git.spi.GitPrinter;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PgTestTemplate {
  private GitClient client;
  @Inject
  io.vertx.mutiny.pgclient.PgPool pgPool;

  @BeforeEach
  public void setUp() {
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.client = GitDataSourceImpl.create()
        .db("junit")
        .client(pgPool)
        .build();
    this.client.tenants().commit().name("junit", StructureType.git).build();
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

  public GitClient getClient() {
    return client;
  }
  
  public GitDataSource createState() {
    final var ctx = TenantContext.defaults("junit");
    return GitDataSourceImpl.create(ctx, pgPool, new TenantCacheImpl());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GitPrinter(createState()).print(repo);
    log.debug(result);
  }
  
  public void prettyPrint(String repoId) {
    Tenant repo = getClient().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    
    printRepo(repo);
  }

  public String toRepoExport(String repoId) {
    Tenant repo = getClient().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = new GitPrinter(createState()).print(repo);
    return result;
  }

  
  @SuppressWarnings("unused")
  public TagomiStore getPersistence(String repoId) {
    
    final GitClient client = getClient();
    final TenantCommitResult repo = getClient()
      .tenants()
      .commit()
      .name(repoId, StructureType.git)
      .build()
      .await().atMost(Duration.ofMinutes(1));
    final AtomicInteger gid = new AtomicInteger(0);
    
    final var config = ImmutableTagomiStoreConfig.builder()
      .client(client)
      .tenantName(repoId)
      .headName("tagomi-main")
      .deserializer(new FromJsonObject())
      .serializer(new ToJsonObject())
      .authorProvider(() -> "junit-test")
      .build();
    
    return new TagomiStoreImpl(config);
  }
  
}
