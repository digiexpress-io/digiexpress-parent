package io.resys.sysconfig.client.tests.config;

import java.io.IOException;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.ThenaStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.store.ImmutableThenaConfig;
import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.ExecutorClient;
import io.resys.sysconfig.client.api.ImmutableAssetClientConfig;
import io.resys.sysconfig.client.api.ImmutableExecutorClientConfig;
import io.resys.sysconfig.client.api.SysConfigClient;
import io.resys.sysconfig.client.spi.SysConfigClientImpl;
import io.resys.sysconfig.client.spi.SysConfigStore;
import io.resys.sysconfig.client.spi.asset.AssetClientImpl;
import io.resys.sysconfig.client.spi.executor.ExecutorClientImpl;
import io.resys.sysconfig.client.spi.executor.ExecutorStoreImpl;
import io.resys.thena.api.ThenaClient;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.jackson.VertexExtModule;
import io.resys.thena.projects.client.api.ProjectClient;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.projects.client.spi.ProjectStore;
import io.resys.thena.projects.client.spi.ProjectsClientImpl;
import io.resys.thena.spi.DbState;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.structures.doc.actions.DocObjectsQueryImpl;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.core.json.jackson.VertxModule;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class TestCaseBuilder {
  public final ObjectMapper objectMapper;
  
  private SysConfigClient sysConfig;
  private TenantConfig tenant;
  private AssetClient assetClient;
  private ExecutorClient executorClient;
  private ProjectClient tenantClient;
  private final ThenaClient doc;
  private final DbState docState;
  private final String author = "jane.doe@morgue.com";
  private final String repoId;
  private TestCaseReader testCaseReader;
  
  public TestCaseBuilder(io.vertx.mutiny.pgclient.PgPool pgPool, String repoId) {
    this.objectMapper = objectMapper();
    
    
    this.doc = getClient(pgPool, "junit");
    this.docState = DbStateSqlImpl.create(TenantTableNames.defaults("junit"), pgPool);
    this.repoId = repoId;
    
    final var stencil = createStencilInit(pgPool, objectMapper);
    final var wrench = createWrenchInit(pgPool, objectMapper);
    final var dialob = createDialobInit(pgPool, objectMapper);
    final var assetConfig = ImmutableAssetClientConfig.builder()
        .dialob(dialob)
        .hdes(wrench)
        .stencil(stencil)
        .tenantConfigId("")
        .build();
    
    final var tenantStore = ProjectStore.builder()
        .repoName(repoId)
        .pgPool(pgPool)
        .objectMapper(objectMapper)
        .build();
    this.tenantClient = new ProjectsClientImpl(tenantStore);
    this.assetClient = new AssetClientImpl(tenantClient, assetConfig);
    this.sysConfig = createSysConfigInit(pgPool, objectMapper, assetClient);
    this.executorClient = createExecutorInit(pgPool, objectMapper, assetClient);
  }
  
  public Uni<TestCaseBuilder> withTenant(TenantConfig tenant) {
    this.tenant = tenant;
    this.sysConfig = this.sysConfig.withRepoId(tenant.getRepoConfigs().stream().filter(c -> c.getRepoType() == TenantRepoConfigType.SYS_CONFIG).findFirst().get().getRepoId());
    return this.assetClient.withTenantConfig(tenant.getId())
    .onItem().transform(newClient -> {
      this.assetClient = newClient;
      return this;
    })
    .onItem().transformToUni(_junk -> this.executorClient.withTenantConfig(tenant.getId())
    .onItem().transform(newClient -> {
        this.executorClient = newClient;
        return this;
      })
    );
  }
  public String getRepoId() {
    return repoId;
  }

  public TestCaseBuilder testcases(String src) {
    this.testCaseReader = new TestCaseReader(objectMapper, src);
    return this;
  }
  
  public TestCaseReader reader() {
    return this.testCaseReader;
  }
  public AssetClient getClient() {
    return assetClient;
  }
  public ProjectClient getTenantClient() {
    return tenantClient;
  }
  public TenantConfig getTenant() {
    return tenant;
  }
  public SysConfigClient getSysConfig() {
    return this.sysConfig;
  }
  
  public ExecutorClient getExecutor() {
    return this.executorClient;
  }
  
  private ExecutorClient createExecutorInit(io.vertx.mutiny.pgclient.PgPool pgPool, ObjectMapper objectMapper, AssetClient assetClient) {
    final var config = ImmutableExecutorClientConfig.builder()
        .tenantConfigId("")
        .build();
    final var store = SysConfigStore.builder()
        .repoName("").pgPool(pgPool)
        .build();
    return new ExecutorClientImpl(new ExecutorStoreImpl(tenantClient, assetClient, config, store), assetClient);
  }
  
  private SysConfigClient createSysConfigInit(io.vertx.mutiny.pgclient.PgPool pgPool, ObjectMapper objectMapper, AssetClient assetClient) {
    final var store = SysConfigStore.builder()
        .repoName("").pgPool(pgPool)
        .build();
    return new SysConfigClientImpl(store, assetClient, tenantClient);
  }

  private DialobClient createDialobInit(io.vertx.mutiny.pgclient.PgPool pgPool, ObjectMapper objectMapper) {
    final var dialobFr = defaultDialobFr();
    final var asyncFunctionInvoker = new AsyncFunctionInvoker(dialobFr);
    final var config = ImmutableDialobStoreConfig.builder()
        .client(doc)
        .repoName("")
        .headName(DocObjectsQueryImpl.BRANCH_MAIN)
        .gidProvider((type) -> OidUtils.gen())
        .serializer((entity) -> applyOm((om) -> new JsonObject(om.writeValueAsString(io.dialob.client.api.ImmutableStoreEntity.builder().from(entity).build()))))
        .deserializer(new io.dialob.client.spi.store.BlobDeserializer(objectMapper))
        .authorProvider(() -> author)
        .build();
    return DialobClientImpl.builder()
        .store(new PgSqlDialobStore(config))
        .objectMapper(objectMapper)
        .eventPublisher(defaultDialobEventPub())
        .asyncFunctionInvoker(asyncFunctionInvoker)
        .functionRegistry(dialobFr)
        .build();
  }
  private HdesClient createWrenchInit(io.vertx.mutiny.pgclient.PgPool pgPool, ObjectMapper objectMapper) {
    final var config = ImmutableThenaConfig.builder()
        .client(doc)
        .repoName("")
        .headName(DocObjectsQueryImpl.BRANCH_MAIN)
        .gidProvider((type) -> OidUtils.gen())
        .serializer((entity) -> applyOm((om) -> new JsonObject(om.writeValueAsString(io.resys.hdes.client.api.ImmutableStoreEntity.builder().from(entity).hash("").build()))))
        .deserializer(new io.resys.hdes.client.spi.store.BlobDeserializer(objectMapper))
        .authorProvider(() -> author)
        .build();
      return HdesClientImpl.builder()
        .objectMapper(objectMapper)
        .store(new ThenaStore(config))
        .dependencyInjectionContext(defaultHdesDjc())
        .serviceInit(defaultHdesServiceInit())
        .build();    
  }
  
  private StencilClient createStencilInit(io.vertx.mutiny.pgclient.PgPool pgPool, ObjectMapper objectMapper) {
    final var docDb = DbStateSqlImpl.create().client(pgPool).build();
    final var deserializer = new ZoeDeserializer(objectMapper);
    final var store = StencilStoreImpl.builder()
        .config((builder) -> builder
        .client(docDb)
        .objectMapper(objectMapper)
        .repoName("")
        .headName(DocObjectsQueryImpl.BRANCH_MAIN)
        .deserializer(deserializer)
        .serializer((entity) -> {
          try {
            return new JsonObject(objectMapper.writeValueAsString(entity));
          } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        })
        .gidProvider(type -> UUID.randomUUID().toString())
        .authorProvider(() -> author))
        .build();
    return new StencilClientImpl(store);
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
  private <T> T applyOm(DoInOm<T> callback) {
    try {
      return callback.apply(this.objectMapper);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  @FunctionalInterface
  private interface DoInOm<T> {
    T apply(ObjectMapper om) throws IOException;
  }
  private ObjectMapper objectMapper() {
    final var modules = new com.fasterxml.jackson.databind.Module[] {
      new JavaTimeModule(), 
      new Jdk8Module(), 
      new GuavaModule(),
      new VertxModule(),
      new VertexExtModule()
    };
    DatabindCodec.mapper().registerModules(modules);
    DatabindCodec.prettyMapper().registerModules(modules);
    return DatabindCodec.mapper(); 
  }
  private ThenaClient getClient(io.vertx.mutiny.pgclient.PgPool pgPool, String db) {
    return DbStateSqlImpl.create().client(pgPool).db(db).build();
  }
}
