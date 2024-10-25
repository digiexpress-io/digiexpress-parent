package io.digiexpress.eveli.client.config;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.assets.spi.EveliAssetsClientImpl;
import io.digiexpress.eveli.assets.spi.EveliAssetsComposerImpl;
import io.digiexpress.eveli.assets.spi.EveliAssetsDeserializer;
import io.digiexpress.eveli.client.web.resources.AssetReleaseController;
import io.digiexpress.eveli.client.web.resources.WorkflowController;
import io.digiexpress.eveli.client.web.resources.WorkflowReleaseController;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.ThenaStore;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.StencilStoreImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.PoolOptions;



@Configuration
public class EveliAssetAutoConfig {
  
  @Value("${spring.datasource.url}")
  private String datasourceUrl;
  @Value("${spring.datasource.username}")
  private String datasourceUsername;
  @Value("${spring.datasource.password}")
  private String datasourcePassword;
  
  
  @Bean 
  public AssetReleaseController assetReleaseController(EveliContext context) {
    return new AssetReleaseController(new EveliAssetsComposerImpl(context.getAssets()));
  }
  @Bean 
  public WorkflowController workflowController(EveliContext context) {
    return new WorkflowController(new EveliAssetsComposerImpl(context.getAssets()), context.getProgramEnvir());
  }
  @Bean 
  public WorkflowReleaseController workflowReleaseController(EveliContext context) {
    return new WorkflowReleaseController(new EveliAssetsComposerImpl(context.getAssets()));
  }

  @Bean
  public EveliContext eveliContext(
      EveliProps eveliProps, 
      ObjectMapper objectMapper,
      ApplicationContext context
    ) {
    
    final var datasourceConfig = datasourceUrl.split(":");
    final var portAndDb = datasourceConfig[datasourceConfig.length -1].split("\\/");

    
    final var pgHost = datasourceConfig[2].substring(2);
    final var pgPort = Integer.parseInt(portAndDb[0]);
    final var pgDb = portAndDb[1];
    final var sslMode = SslMode.ALLOW;
    
    final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(
        new PgConnectOptions()
          .setHost(pgHost)
          .setPort(pgPort)
          .setDatabase(pgDb)
          .setUser(datasourceUsername)
          .setPassword(datasourcePassword)
          .setSslMode(sslMode), 
        new PoolOptions().setMaxSize(5));
    
    final var wrenchClient = HdesClientImpl.builder()
        .store(ThenaStore.builder()
            .pgPool(pgPool)
            .repoName("wrench-assets")
            .headName("main")
            .authorProvider(() -> "eveli")
            .objectMapper(objectMapper)
            .build())
        .objectMapper(objectMapper)
        .serviceInit(new ServiceInit() { @Override public <T> T get(Class<T> type) { return context.getAutowireCapableBeanFactory().createBean(type); } })
        .dependencyInjectionContext( new DependencyInjectionContext() { @Override public <T> T get(Class<T> type) { return context.getBean(type); } })
        .flowVisitors(new IdValidator())
        .build();
    
    final var stencilClient = new StencilClientImpl(StencilStoreImpl.builder()
        .config((builder) -> builder
            .client(DocDBFactorySql.create().client(pgPool).errorHandler(new PgErrors()).build())
            .objectMapper(objectMapper)
            .repoName("stencil-assets")
            .headName("main")
            .deserializer(new ZoeDeserializer(objectMapper))
            .serializer((entity) -> {
              try {
                return objectMapper.writeValueAsString(entity);
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(type -> UUID.randomUUID().toString())
            .authorProvider(() -> "eveli")
        ).build());

    final Supplier<ProgramEnvir> programEnvir = () -> {
      final var state = wrenchClient.store().query().get().await().atMost(Duration.ofMinutes(1));
      return ComposerEntityMapper.toEnvir(wrenchClient.envir(), state).build();          
    };


    final var assetClient = EveliAssetsClientImpl.builder()
        .config((builder) -> builder
            .client(DocDBFactorySql.create().client(pgPool).errorHandler(new PgErrors()).build())
            .repoName("eveli-assets")
            .headName("main")
            .deserializer(new EveliAssetsDeserializer(objectMapper))
            .objectMapper(objectMapper)
            .serializer((entity) -> {
              try {
                return objectMapper.writeValueAsString(entity);
              } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
              }
            })
            .gidProvider(() -> UUID.randomUUID().toString())
            .authorProvider(() -> "junit-test"))
            
        .build();
    
    
    return EveliContext.builder()
        .stencil(stencilClient)
        .wrench(wrenchClient)
        .programEnvir(programEnvir)
        .assets(assetClient)
        .build();
  }
  
  
  /*

  @Bean
  public WrenchConfig hdesReadonly(EveliProps eveliProps, EveliPropsHdes hdesProps, ObjectMapper objectMapper, ApplicationContext context) {

        WorkflowCommandsJPA.builder().workflowJPA(workflowRepository.get()).releaseJPA(workflowReleaseRepository.get()).build();

    HdesClientImpl.builder().objectMapper(objectMapper)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return context.getBean(type);
          }
        })
        .serviceInit(new ServiceInit() {
          @Override
          public <T> T get(Class<T> type) {
            return context.getAutowireCapableBeanFactory().createBean(type);
          }
        })
        .store(HdesInMemoryStore.builder().objectMapper(objectMapper).build()).build();
    
    final var state = hdesClient.store().query().get().await().atMost(Duration.ofMinutes(1));
    final var envir = ComposerEntityMapper.toEnvir(hdesClient.envir(), state).build();
    final Supplier<ProgramEnvir> programEnvir = () -> envir;
    
    
    final var client = StencilClientImpl.builder().defaultObjectMapper().inmemory().build()
        .sites().source(buildItem.getContent())
        .imagePath("")
        .created(System.currentTimeMillis());
    final var content = client.build();
    final var contentValues = content.getSites().entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    
    if(!contentValues.containsKey(config.defaultLocale)) {
      throw new ConfigurationException("Markdowns must have localization for default-locale: '" + config.defaultLocale + "'!");
    } 
    
    WorkflowCommandsJson.builder().workflows(jsonWorkflowLocation.get(), null).build()
  }
  
  

  /*
  @Bean 
  public NotificationController notificationController() {
  }*/
}
