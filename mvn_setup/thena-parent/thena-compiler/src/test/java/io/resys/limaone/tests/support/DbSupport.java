package io.resys.limaone.tests.support;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.persistence.ImmutableAuthoringConfig;
import io.resys.limaone.persistence.world.WorldPersistenceFs;
import io.resys.limaone.spi.ast.AST_ParserImpl;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.api.actions.TenantActions.CreatedTenant;
import io.resys.thena.api.actions.TenantActions.TenantOperationStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.printer.FileSystemPrinter;
import io.resys.thena.fs.spi.FileSystem_ThenaImpl;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.spi.FsTableNames;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.test.ThenaTest;
import io.resys.thena.test.ThenaTestDbConfig;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ThenaTest(database = @ThenaTestDbConfig(
  enabled = false, 
  database = "eveli-app", 
  host = "localhost",
  port = 5433,
  user = "eveli-app", password = "password123"
))
public class DbSupport {

  private FileSystem_ThenaImpl client;
  protected io.vertx.mutiny.sqlclient.Pool pgPool;
  protected static Duration atMost = Duration.ofMinutes(1);

  private String db;
  private Tenant repo;
  private final Map<String, String> replacements = new HashMap<>();

  
  public DbSupport() {
  }

  
  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) throws InterruptedException {
    this.pgPool = pgPool;
    this.replacements.clear();
    this.client = FileSystem_ThenaImpl.createInstance()
        .tenantName("junit")
        .client(pgPool)
        .errorHandler(new PgErrors())
        .build();
    final CreatedTenant repo = this.client.tenants()
        .createOneTenant()
        .name(this.client.getTenantName(), StructureType.fs)
        .buildOnlyIfNotCreated()
        .await().atMost(Duration.ofMinutes(1)).getItem2();
    wipeRepo(repo.getRepo());
    
    this.client.withTenant()
      .commitBuilder()
      .branchName("main")
      .commitAuthor("john smith")
      .commitMessage("create main branch with some content")
      .newFile((newFile) -> newFile
          .fileName("init.md")
          .fileType("init_md")
          .fileValue(new JsonObject())
          .build())
      .build()
      .await().atMost(atMost);
    
    log.debug("created repo {}", repo);
    
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());

  }
  
  public void wipeRepo(Tenant repo) {
    final var datasource = (ThenaSqlDataSource) this.client.getStartingState().getDataSource();
    
    final var names = FsTableNames.defaults().toRepo(repo);
    
    datasource.getClient().query("delete from " + names.getObjectIndex())
      .execute().await().atMost(Duration.ofMillis(100));
    
    datasource.getClient().query("delete from " + names.getTag())
      .execute().await().atMost(Duration.ofMillis(100));
    
    datasource.getClient().query("delete from " + names.getRef())
      .execute().await().atMost(Duration.ofMillis(100));
    
    datasource.getClient().query("delete from " + names.getCommit())
      .execute().await().atMost(Duration.ofMillis(100));
    
    datasource.getClient().query("delete from " + names.getTree())
      .execute().await().atMost(Duration.ofMillis(100));
    
    datasource.getClient().query("delete from " + names.getProps())
      .execute().await().atMost(Duration.ofMillis(100));
    
    datasource.getClient().query("delete from " + names.getBlob())
      .execute().await().atMost(Duration.ofMillis(100));

    
  }

  @AfterEach
  public void tearDown() {
  }

  public FileSystem getClient() {
    return client;
  }
  
  public FsDb createState() {
    final var ctx = TenantContext.defaults(db);
    return FileSystem_ThenaImpl.createInstance(ctx, pgPool, new TenantCacheImpl(), new PgErrors());
  }
  
  public void printRepo(Tenant repo) {
    final String result = new FileSystemPrinter(createState()).printSync();
    log.debug(result);
  }
  public Tenant getRepo() {
    return repo;
  }

  
  public static String toExpectedFile(String fileName) {
    return toString(DbSupport.class, fileName);
  }
  
  public void assertRepo(Tenant client, String expectedFileName) {
    final var expected = toExpectedFile(expectedFileName);
    final var actual = toStaticData(client);
    Assertions.assertLinesMatch(expected.lines(), actual.lines(), actual);
    
  }
  public void assertEquals(String expectedFileName, Object actual) {
    final var expected = toExpectedFile(expectedFileName);
    final var actualJson = JsonObject.mapFrom(actual).encodePrettily();
    Assertions.assertLinesMatch(expected.lines(), actualJson.lines(), actualJson);  
  }
  
  public static String toString(Class<?> type, String resource) {
    try {
      return new String(type.getClassLoader().getResourceAsStream(resource).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public String toStaticData(Tenant client) {
    return new FileSystemPrinter(createState(), replacements).printSync();
  }
  
  
  public FileSystem createClient(String tenantId) {
    // create project
    CreatedTenant repo = getClient().tenants().createOneTenant()
        .name(tenantId)
        .build()
        .await().atMost(atMost);
    log.debug("created repo {}", repo);
    Assertions.assertEquals(TenantOperationStatus.OK, repo.getStatus());
    
    final var tenant = repo.getRepo();
    
    return FileSystem_ThenaImpl.createInstance()
        .client(pgPool)
        .tenantName(tenant.getName())
        .errorHandler(new PgErrors())
        .build();
    
  }

  
  public AuthoringImpl.AuthoringConfig createConfig() {
    return createConfig(null);
  }
  
  public AuthoringImpl.AuthoringConfig createConfig(@Nullable FormDb formDb) {
    final var workerTimeout = Duration.ofMinutes(1);
    final var workerPool = Infrastructure.getDefaultWorkerPool();
    return ImmutableAuthoringConfig.builder()
        .astParser(AST_ParserImpl.builder().dev(true).build())
        .workerPool(workerPool)
        .workerTimeout(workerTimeout)
        .persistence(new WorldPersistenceFs(formDb, client, workerPool, workerTimeout))
        .author(() -> "sam vimes")
        .build();
  }
}
