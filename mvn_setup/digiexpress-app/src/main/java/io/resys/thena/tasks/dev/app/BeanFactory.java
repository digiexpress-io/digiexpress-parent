package io.resys.thena.tasks.dev.app;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Nullable;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.jackson.ObjectMapperCustomizer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.spi.CrmClientImpl;
import io.resys.thena.docdb.jackson.VertexExtModule;
import io.resys.thena.docdb.store.sql.DbStateSqlImpl;
import io.resys.thena.docdb.store.sql.PgErrors;
import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.spi.ProjectsClientImpl;
import io.resys.thena.projects.client.spi.store.MainBranch;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.spi.TaskClientImpl;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.spi.UserProfileClientImpl;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.VertxModule;
import io.vertx.mutiny.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;

@Dependent
@RegisterForReflection(targets = {
  ImmutableTask.class,
  ImmutableTaskExtension.class,
  ImmutableTaskComment.class,
  BeanFactory.CurrentUserRecord.class,
  BeanFactory.CurrentTenantRecord.class,
})
@Slf4j
public class BeanFactory {

  @ConfigProperty(name = "tenant.db.pg.repositoryName")
  String tenantsStoreId;
  @ConfigProperty(name = "tenant.db.pg.pgPoolSize")
  Integer pgPoolSize;
  @ConfigProperty(name = "tenant.db.pg.pgHost")
  String pgHost;
  @ConfigProperty(name = "tenant.db.pg.pgPort")
  Integer pgPort;
  @ConfigProperty(name = "tenant.db.pg.pgDb")
  String pgDb;
  @ConfigProperty(name = "tenant.db.pg.pgUser")
  String pgUser;
  @ConfigProperty(name = "tenant.db.pg.pgPass")
  String pgPass;

  @ConfigProperty(name = "tenant.currentTenantId")
  String tenantId;

  public record CurrentPgPool(io.vertx.mutiny.pgclient.PgPool pgPool) {}
  public record CurrentTenantRecord(String tenantId, String tenantsStoreId) implements CurrentTenant { }

  public record CurrentUserRecord(
    String userId,
    @Nullable String givenName,
    @Nullable String familyName,
    @Nullable String email
  ) implements CurrentUser {
    
  }

  @Produces
  @RequestScoped
  public CurrentTenant currentTenant() {
    return new CurrentTenantRecord(tenantId, tenantsStoreId);
  }

  @IfBuildProfile("prod")
  @Produces
  @RequestScoped
  public CurrentUser currentUserClaims(
    @Claim(standard = Claims.sub) String userId,
    @Claim(standard = Claims.given_name) String givenName,
    @Claim(standard = Claims.family_name) String familyName,
    @Claim(standard = Claims.email) String email)
  {
    return new CurrentUserRecord(userId, givenName, familyName, email);
  }
  
  @IfBuildProfile("dev")
  @Produces
  @RequestScoped
  public CurrentUser currentUserDev() {
    return new CurrentUserRecord("local-tester", "first name", "last-name", "first.last@digiexpress.io");
  }


  @Produces
  public TaskClient taskClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = io.resys.thena.tasks.client.spi.DocumentStoreImpl.builder()
      .repoName("")
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new TaskClientImpl(store);
  }

  @Produces
  public TenantConfigClient tenantClient(ObjectMapper om, CurrentPgPool currentPgPool) {
    final var store = io.resys.thena.projects.client.spi.DocumentStoreImpl.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new ProjectsClientImpl(store);
  }

  @Produces
  public StencilComposer stencilComposer(Vertx vertx, ObjectMapper om, CurrentTenant currentProject, CurrentPgPool currentPgPool) {
    final var docDb = DbStateSqlImpl.create().client(currentPgPool.pgPool).errorHandler(new PgErrors()).build();
    final var deserializer = new ZoeDeserializer(om);
    final var store = StencilStoreImpl.builder()
      .config((builder) -> builder
        .client(docDb)
        .objectMapper(om)
        .repoName("")
        .headName(MainBranch.HEAD_NAME)
        .deserializer(deserializer)
        .serializer((entity) -> {
          try {
            return new JsonObject(om.writeValueAsString(entity));
          } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        })
        .gidProvider(type -> UUID.randomUUID().toString())
        .authorProvider(() -> "no-author"))
      .build();
    final var client = new StencilClientImpl(store);
    return new StencilComposerImpl(client);
  }

  @Produces
  public CrmClient crmClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = io.resys.crm.client.spi.DocumentStoreImpl.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new CrmClientImpl(store);
  }

  @Produces
  public UserProfileClient userProfileClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = io.resys.userprofile.client.spi.DocumentStoreImpl.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new UserProfileClientImpl(store);
  }

  
  @Produces
  public CurrentPgPool currentPgPool(Vertx vertx) {
    log.debug("PgConnectOptions: pgHost={}, pgPort={}, pgUser={}, pgPass={}, pgDb={}, pgPoolSize={}",
      pgHost, pgPort, pgUser, pgPass != null ? "***" : "null", pgDb, pgPoolSize);
    final var connectOptions = new PgConnectOptions().setDatabase(pgDb)
      .setHost(pgHost).setPort(pgPort)
      .setUser(pgUser).setPassword(pgPass);
    final var poolOptions = new PoolOptions().setMaxSize(pgPoolSize);
    final var pgPool = io.vertx.mutiny.pgclient.PgPool.pool(vertx, connectOptions, poolOptions);
    return new CurrentPgPool(pgPool);
  }

  @Produces
  public ObjectMapperCustomizer objectMapperCustomizer() {
    return mapper -> mapper
      .registerModules(
        new JavaTimeModule(),
        new Jdk8Module(),
        new GuavaModule(),
        new VertxModule(),
        new VertexExtModule())
      // without this, local dates will be serialized as int array
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
  
}
