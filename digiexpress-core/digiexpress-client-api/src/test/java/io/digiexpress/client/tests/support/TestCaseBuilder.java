package io.digiexpress.client.tests.support;

import java.io.IOException;
import java.util.UUID;

import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobClient;
import io.dialob.client.pgsql.PgSqlDialobStore;
import io.dialob.client.spi.DialobClientImpl;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.client.spi.function.FunctionRegistryImpl;
import io.dialob.client.spi.store.DialobStoreConfig;
import io.dialob.client.spi.store.ImmutableDialobStoreConfig;
import io.dialob.rule.parser.function.DefaultFunctions;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.ThenaStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.store.ImmutableThenaConfig;
import io.resys.hdes.client.spi.store.ThenaConfig;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.spi.pgsql.PgErrors;
import io.resys.thena.docdb.sql.DocDBFactorySql;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.StencilClientImpl;
import io.thestencil.client.spi.serializers.ZoeDeserializer;


public class TestCaseBuilder {
  private static final String headName = "main";
  
  private final ObjectMapper objectMapper;
  private final DialobClient dialobClient;
  private final HdesClient hdesClient;
  private final StencilClient stencilClient;
  
  
  public TestCaseBuilder(io.vertx.mutiny.pgclient.PgPool pgPool) {
    this.objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
    
    // dialob 
    {
      final var functionRegistry = new FunctionRegistryImpl();
      final var defaultFunctions = new DefaultFunctions(functionRegistry);

      final var eventPublisher = Mockito.mock(QuestionnaireEventPublisher.class);
      final var asyncFunctionInvoker = new AsyncFunctionInvoker(functionRegistry);
      
      final var repoName = "dialob";
      final DocDB db = getClient(pgPool, repoName);
      final DialobStoreConfig config = ImmutableDialobStoreConfig.builder()
          .client(db).repoName(repoName).headName(headName)
          .gidProvider((type) -> UUID.randomUUID().toString())
          .serializer((entity) -> {
            try {
              return objectMapper.writeValueAsString(io.dialob.client.api.ImmutableStoreEntity.builder().from(entity).build());
            } catch (IOException e) {
              throw new RuntimeException(e.getMessage(), e);
            }
          })
          .deserializer(new io.dialob.client.spi.store.BlobDeserializer(objectMapper))
          .authorProvider(() -> "not-configured")
          .build();
      
      this.dialobClient = DialobClientImpl.builder()
          .store(new PgSqlDialobStore(config))
          .objectMapper(objectMapper)
          .eventPublisher(eventPublisher)
          .asyncFunctionInvoker(asyncFunctionInvoker)
          .functionRegistry(functionRegistry)
          .build();
    }
    
    // hdes
    {
      final var repoName = "hdes";
      final DocDB db = getClient(pgPool, repoName);

      final ThenaConfig config = ImmutableThenaConfig.builder()
          .client(db).repoName(repoName).headName(headName)
          .gidProvider((type) -> UUID.randomUUID().toString())
          .serializer((entity) -> {
            try {
              return objectMapper.writeValueAsString(io.resys.hdes.client.api.ImmutableStoreEntity.builder().from(entity).hash("").build());
            } catch (IOException e) {
              throw new RuntimeException(e.getMessage(), e);
            }
          })
          .deserializer(new io.resys.hdes.client.spi.store.BlobDeserializer(objectMapper))
          .authorProvider(() -> "not-configured")
          .build();
      
      hdesClient = HdesClientImpl.builder()
      .objectMapper(objectMapper)
      .store(new ThenaStore(config))
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
    
    
    // stencil
    {
      final var repoName = "stencil";
      final DocDB db = getClient(pgPool, repoName);

      this.stencilClient = StencilClientImpl.builder()
      .config((builder) -> builder
        .client(db)
        .repoName(repoName)
        .headName(headName)
        .deserializer(new ZoeDeserializer(objectMapper))
        .serializer((entity) -> {
          try {
            return objectMapper.writeValueAsString(entity);
          } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
          }
        })
        .gidProvider((type) -> UUID.randomUUID().toString())
        .authorProvider(() -> "not-configured"))
      .build();
    
    }
  }
  
  private DocDB getClient(io.vertx.mutiny.pgclient.PgPool pgPool, String repoName) {
    return DocDBFactorySql.create().client(pgPool).db(repoName).errorHandler(new PgErrors()).build();
  }
}
