package io.thestencil.client.tests.util;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.support.DocDbPrinter;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgTestTemplate {
  private ThenaClient client;
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  private final Map<String, String> init = new HashMap<>();

  @BeforeEach
  public void setUp() {
    init.clear();
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.client = DbStateSqlImpl.create()
        .db("junit")
        .client(pgPool)
        .build();
    this.client.tenants().commit().name("junit", StructureType.doc).build().await().atMost(Duration.ofSeconds(2));
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

  public ThenaClient getClient() {
    return client;
  }
  
  public DbState createState() {
    final var ctx = TenantTableNames.defaults("junit");
    return DbStateSqlImpl.create(ctx, pgPool);
  }
  
  public void printRepo(Tenant repo) {
    final String result = new DocDbPrinter(createState()).print(repo);
    log.debug(result);
  }
  
  public void prettyPrint(String repoId) {
    Tenant repo = getClient().tenants().find().id(repoId).get()
        .await().atMost(Duration.ofMinutes(1));
    
    printRepo(repo);
  }

  public String toRepoExport(String repoId) {
    Tenant repo = getClient().tenants().find().id(repoId).get()
        .await().atMost(Duration.ofMinutes(1));
    final String result = new DocDbPrinter(createState()).printWithStaticIds(repo, init, true);
    return result;
  }

  
  @SuppressWarnings("unused")
  public StencilComposer getPersistence(String repoId) {
    final ThenaClient client = getClient();
    
    // create project
    TenantCommitResult repo = getClient().tenants().commit()
        .name(repoId, StructureType.doc)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    
    final AtomicInteger gid = new AtomicInteger(0);
    
    final var store = StencilStoreImpl.builder()
        .authorProvider(() -> "junit-test")
        .repoName(repoId)
        .pgPool(pgPool)
        .build();
    
    
    return new StencilComposerImpl(new StencilClientImpl(), store);
  }
  
}
