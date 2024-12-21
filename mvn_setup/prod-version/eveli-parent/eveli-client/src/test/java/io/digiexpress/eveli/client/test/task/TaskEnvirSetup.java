package io.digiexpress.eveli.client.test.task;

import java.time.Duration;

import org.testcontainers.containers.PostgreSQLContainer;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.event.NotificationMessagingComponent;
import io.digiexpress.eveli.client.event.TaskEventPublisher;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.spi.task.ImmutableTaskStoreConfig;
import io.digiexpress.eveli.client.spi.task.TaskClientImpl;
import io.digiexpress.eveli.client.spi.task.TaskStoreImpl;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.TenantCommitResult;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.git.GitPrinter;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;



@Slf4j
public class TaskEnvirSetup {
  private final TaskEventPublisher publisher;
  private final ThenaClient dbState;
  private final io.vertx.mutiny.pgclient.PgPool pgPool;
  private final String repoId;
  
  
  public TaskEnvirSetup(PostgreSQLContainer<?> cont, TaskEventPublisher publisher, String repoId) {
    this.pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(cont.getHost())
          .setPort(cont.getFirstMappedPort())
          .setDatabase(cont.getDatabaseName())
          .setUser(cont.getUsername())
          .setPassword(cont.getPassword()), 
        new PoolOptions().setMaxSize(5));
    waitUntilPostgresqlAcceptsConnections(pgPool);
    this.dbState = DbStateSqlImpl.create()
        .db("junit")
        .client(pgPool)
        .build();
    this.publisher = publisher;
    this.repoId = repoId;
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

  public ThenaClient getDbState() {
    return dbState;
  }
  
  public DbState createState() {
    final var ctx = TenantTableNames.defaults("junit");
    return DbStateSqlImpl.create(ctx, pgPool);
  }
  
  public void printRepo(Tenant repo) {
    final String result = new GitPrinter(createState()).print(repo);
    log.debug(result);
  }
  
  public void prettyPrint(String repoId) {
    Tenant repo = getDbState().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    
    printRepo(repo);
  }

  public String toRepoExport(String repoId) {
    Tenant repo = getDbState().git(repoId).tenants().get()
        .await().atMost(Duration.ofMinutes(1)).getRepo();
    final String result = null;//new TestExporter(createState()).print(repo);
    return result;
  }

  
  public TaskClient getTaskClient() {
    final TaskNotificator notificator = new NotificationMessagingComponent(publisher);
    final var config = ImmutableTaskStoreConfig.builder()
        .tenantName(repoId)
        .client(dbState)
        .build();
    final var store = new TaskStoreImpl(config);
    
    // create task project
    TenantCommitResult repo = dbState.tenants().commit()
        .name(repoId, StructureType.grim)
        .build()
        .await().atMost(Duration.ofMinutes(1));
    log.info("Repo created: " + repo);
    
    return new TaskClientImpl(notificator, store);
  }
  
}
