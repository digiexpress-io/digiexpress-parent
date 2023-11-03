package io.dialob.pgsql.test.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;

import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobCache;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.api.DialobErrorHandler;
import io.dialob.client.api.DialobStore;
import io.dialob.client.api.ImmutableDialobClientConfig;
import io.dialob.client.spi.DialobClientImpl;
import io.dialob.client.spi.DialobEhCache;
import io.dialob.client.spi.DialobErrorHandlerImpl;
import io.dialob.client.spi.DialobTypesMapperImpl;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.client.spi.function.FunctionRegistryImpl;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobSessionEvalContextFactory;
import io.dialob.program.EvalContext;
import io.dialob.rule.parser.function.DefaultFunctions;
import io.dialob.rule.parser.function.FunctionRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

public class DialobClientImplForTests extends DialobClientImpl {

  private static DialobClientImplForTests INSTANCE = DialobClientImplForTests.builder().build();
  
  public DialobClientImplForTests(DialobClientConfig config) {
    super(config);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public DialobClientImplForTests with(QuestionnaireEventPublisher publisher) {
    final var newConfig = ImmutableDialobClientConfig.builder().from(this.getConfig()).eventPublisher(publisher).build();
    return new DialobClientImplForTests(newConfig);
  }
  
  public static DialobClientImplForTests get() {
    return INSTANCE;
  }
  
  @Accessors(fluent = true, chain = true)
  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class Builder extends DialobClientImpl.Builder {  

    
    public DialobClientImplForTests build() {
      DialobCache cache = this.cache();
      if(cache == null) {
        cache = DialobEhCache.builder().build("inmem");
      }
      
      Clock clock = this.clock();
      if(clock == null) {
        clock = Clock.systemDefaultZone();
      }

      ObjectMapper objectMapper = this.objectMapper();
      if(objectMapper == null) {
        objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
      }
      
      QuestionnaireEventPublisher eventPublisher = this.eventPublisher();
      if(eventPublisher == null) {
        eventPublisher = Mockito.mock(QuestionnaireEventPublisher.class);
      }
      
      DialobStore store = this.store();
      if(store == null) {
        store = Mockito.mock(DialobStore.class);
      }
      DialobErrorHandler errorHandler = errorHandler();
      if(errorHandler == null) {
        errorHandler = new DialobErrorHandlerImpl(true);
      }
      
      final var visitor = mock(EvalContext.UpdatedItemsVisitor.AsyncFunctionCallVisitor.class);
      final var asyncFunctionInvoker = mock(AsyncFunctionInvoker.class);
      when(asyncFunctionInvoker.createVisitor(Mockito.any())).thenReturn(visitor);
      final var functionRegistry = createFunctionRegistry();
      
      
      final var config = ImmutableDialobClientConfig.builder()
          .asyncFunctionInvoker(asyncFunctionInvoker)
          .factory(new DialobSessionEvalContextFactory(functionRegistry, clock, dialobSessionUpdateHook()))
          .store(store)
          .cache(cache)
          .errorHandler(errorHandler)
          .eventPublisher(eventPublisher)
          .mapper(new DialobTypesMapperImpl(objectMapper))
          .compiler(new DialobProgramFromFormCompiler(functionRegistry))
          .build();
      

      return new DialobClientImplForTests(config);
    }
  }

  
  private static FunctionRegistry createFunctionRegistry() {
    try {
      final var result = new FunctionRegistryImpl();
      new DefaultFunctions(result);
      return result;
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
