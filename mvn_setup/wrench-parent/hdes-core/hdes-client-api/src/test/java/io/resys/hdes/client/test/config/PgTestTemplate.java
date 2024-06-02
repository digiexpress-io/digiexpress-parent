package io.resys.hdes.client.test.config;

import java.time.Duration;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.HdesStoreImpl;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.support.DocDbPrinter;
import io.vertx.mutiny.sqlclient.Pool;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgTestTemplate {
  private HdesStoreImpl store;
  
  @Inject
  io.vertx.mutiny.pgclient.PgPool pgPool;
  
  private final Map<String, String> static_ids = new HashedMap<>();

  @BeforeEach
  public void setUp(TestInfo testInfo) {
    
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.store = HdesStoreImpl.builder()
        .repoName("")
        .pgPool(pgPool)
        .objectMapper(TestUtils.objectMapper)
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
  
  public void addId(StoreEntity entity) {
    final String id = entity.getId();
    static_ids.put(id, String.valueOf("_" + static_ids.size() + 1));
  }
  
  public void printRepo(Tenant tenant) {
    final var ctx = TenantTableNames.defaults("").toRepo(tenant);
    final var result = new DocDbPrinter(DbStateSqlImpl.create(ctx, pgPool)).printWithStaticIds(tenant, static_ids);
    log.debug(result);
  }

  public String toRepoExport(String repoName) {
    final var client = getStore().getConfig().getClient();
    final var tenant = client.tenants().find().id(repoName).get().await().atMost(Duration.ofSeconds(10));
    final var ctx = TenantTableNames.defaults("").toRepo(tenant);
    return new DocDbPrinter(DbStateSqlImpl.create(ctx, pgPool)).printWithStaticIds(tenant, static_ids);
  }

  public HdesStoreImpl getStore() {
    return store;
  }
  
  public HdesClient getClient() {
    return TestUtils.client;
  }
  
  public HdesComposer getComposer() {
    return new HdesComposerImpl(getClient(), store);
  }
}
