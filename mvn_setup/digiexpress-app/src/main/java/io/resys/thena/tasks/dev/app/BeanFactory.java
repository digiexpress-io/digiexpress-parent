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

import io.dialob.client.api.DialobClient;
import io.dialob.client.pgsql.PgSqlDialobStore;
import io.dialob.client.spi.DialobClientImpl;
import io.dialob.client.spi.event.EventPublisher;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.client.spi.function.FunctionRegistryImpl;
import io.dialob.client.spi.store.ImmutableDialobStoreConfig;
import io.dialob.client.spi.support.OidUtils;
import io.dialob.rule.parser.function.DefaultFunctions;
import io.quarkus.jackson.ObjectMapperCustomizer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.resys.avatar.client.api.AvatarClient;
import io.resys.avatar.client.spi.AvatarClientImpl;
import io.resys.avatar.client.spi.AvatarStore;
import io.resys.crm.client.api.CrmClient;
import io.resys.crm.client.spi.CrmClientImpl;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.ThenaStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.store.ImmutableThenaConfig;
import io.resys.permission.client.api.PermissionClient;
import io.resys.permission.client.spi.PermissionClientImpl;
import io.resys.sysconfig.client.api.ImmutableAssetClientConfig;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.spi.SysConfigClientImpl;
import io.resys.sysconfig.client.spi.SysConfigStore;
import io.resys.sysconfig.client.spi.asset.AssetClientImpl;
import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.spi.ProjectsClientImpl;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.ImmutableTaskComment;
import io.resys.thena.tasks.client.api.model.ImmutableTaskExtension;
import io.resys.thena.tasks.client.thenamission.TaskClientImpl;
import io.resys.thena.tasks.client.thenamission.TaskStoreImpl;
import io.resys.thena.tasks.dev.app.user.CurrentTenant;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.resys.userprofile.client.api.UserProfileClient;
import io.resys.userprofile.client.spi.UserProfileClientImpl;
import io.thestencil.client.api.StencilClient;
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
import jakarta.inject.Singleton;
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
  @Singleton
  public CurrentTenant currentTenant() {
    return new CurrentTenantRecord(tenantId, tenantsStoreId);
  }
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
  
  @Produces
  @Singleton
  public PermissionClient permissionClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = io.resys.permission.client.spi.PermissionStoreImpl.builder()
      .repoName("")
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new PermissionClientImpl(store);
  }

  @Produces
  @Singleton
  public TaskClient taskClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = TaskStoreImpl.builder()
      .repoName("")
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new TaskClientImpl(store);
  }
  @Singleton
  @Produces
  public ProjectClient tenantClient(ObjectMapper om, CurrentPgPool currentPgPool) {
    final var store = io.resys.thena.projects.client.spi.ProjectStore.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new ProjectsClientImpl(store);
  }
  @Singleton
  @Produces
  public StencilClient stencilClient(Vertx vertx, ObjectMapper om, CurrentTenant currentProject, CurrentPgPool currentPgPool) {
    final var docDb = DbStateSqlImpl.create().client(currentPgPool.pgPool).build();
    final var deserializer = new ZoeDeserializer(om);
    final var store = StencilStoreImpl.builder()
      .config((builder) -> builder
        .client(docDb)
        .objectMapper(om)
        .repoName("")
        .headName(DocObjectsQueryImpl.BRANCH_MAIN)
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
    return new StencilClientImpl(store);
  }
  @Singleton
  @Produces
  public StencilComposer stencilComposer(StencilClient client) {
    return new StencilComposerImpl(client);
  }
  @Singleton
  @Produces
  public CrmClient crmClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = io.resys.crm.client.spi.CrmStore.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new CrmClientImpl(store);
  }
  @Singleton
  @Produces
  public HdesClient hdesClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var config = ImmutableThenaConfig.builder()
        .client(DbStateSqlImpl.create().client(currentPgPool.pgPool).db("").build())
        .repoName("")
        .headName(DocObjectsQueryImpl.BRANCH_MAIN)
        .gidProvider((type) -> OidUtils.gen())
        .serializer((entity) -> {
          try {
            return new JsonObject(om.writeValueAsString(io.resys.hdes.client.api.ImmutableStoreEntity.builder().from(entity).hash("").build()));
          } catch(IOException e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        })
        .deserializer(new io.resys.hdes.client.spi.store.BlobDeserializer(om))
        .authorProvider(() -> "no-author")
        .build();
      return HdesClientImpl.builder()
        .objectMapper(om)
        .store(new ThenaStore(config))
        .dependencyInjectionContext(defaultHdesDjc())
        .serviceInit(defaultHdesServiceInit())
        .build();    
  }
  @Singleton
  @Produces
  public DialobClient dialobClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var dialobFr = defaultDialobFr();
    final var asyncFunctionInvoker = new AsyncFunctionInvoker(dialobFr);
    final var config = ImmutableDialobStoreConfig.builder()
        .client(DbStateSqlImpl.create().client(currentPgPool.pgPool).db("").build())
        .repoName("")
        .headName(DocObjectsQueryImpl.BRANCH_MAIN)
        .gidProvider((type) -> OidUtils.gen())
        .serializer((entity) -> {
          try {
            return new JsonObject(om.writeValueAsString(io.dialob.client.api.ImmutableStoreEntity.builder().from(entity).build()));
          } catch(IOException e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        })
        .deserializer(new io.dialob.client.spi.store.BlobDeserializer(om))
        .authorProvider(() -> "no-author")
        .build();
    return DialobClientImpl.builder()
        .store(new PgSqlDialobStore(config))
        .objectMapper(om)
        .eventPublisher(defaultDialobEventPub())
        .asyncFunctionInvoker(asyncFunctionInvoker)
        .functionRegistry(dialobFr)
        .build();
  }
  @Singleton
  @Produces
  public SysConfigClient sysConfigClient(
      CurrentPgPool currentPgPool, ObjectMapper om, 
      ProjectClient tenantClient,
      StencilClient stencil,
      HdesClient wrench,
      DialobClient dialob) {
    final var store = SysConfigStore.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    
    final var assetConfig = ImmutableAssetClientConfig.builder()
        .dialob(dialob)
        .hdes(wrench)
        .stencil(stencil)
        .tenantConfigId("")
        .build();
    
    final var assets = new AssetClientImpl(tenantClient, assetConfig);
    return new SysConfigClientImpl(store, assets, tenantClient);
  }

  @Singleton
  @Produces
  public UserProfileClient userProfileClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = io.resys.userprofile.client.spi.UserProfileStore.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new UserProfileClientImpl(store);
  }
  @Singleton
  @Produces
  public AvatarClient avatarClient(CurrentPgPool currentPgPool, ObjectMapper om) {
    final var store = AvatarStore.builder()
      .repoName(tenantsStoreId)
      .pgPool(currentPgPool.pgPool)
      .objectMapper(om)
      .build();
    return new AvatarClientImpl(store);
  }
  
  @Singleton
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

  @Singleton
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
 
  
  
  

  private QuestionnaireEventPublisher defaultDialobEventPub() {
    final var  publisher = new EventPublisher() {
      @Override
      public void publish(Event event) {
        log.debug("dialob event publisher: " + event);
      }
    };
    return new QuestionnaireEventPublisher(publisher);
  }
  private FunctionRegistryImpl defaultDialobFr() {
    final var dialobFr = new FunctionRegistryImpl();
    final var defaultFunctions = new DefaultFunctions(dialobFr);
    log.debug("dialob default functions: " + defaultFunctions.getClass().getCanonicalName());
    return dialobFr;
  }
  private DependencyInjectionContext defaultHdesDjc() {
    return new DependencyInjectionContext() {
      @Override
      public <T> T get(Class<T> type) {
        return null;
      }
    };
  }
  private ServiceInit defaultHdesServiceInit() {
    return new ServiceInit() {
      @Override
      public <T> T get(Class<T> type) {
        try {
          return type.getDeclaredConstructor().newInstance();
        } catch(Exception e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    };
  }
}
