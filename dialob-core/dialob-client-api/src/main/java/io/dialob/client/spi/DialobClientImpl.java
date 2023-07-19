package io.dialob.client.spi;

import java.time.Clock;
import java.util.HashMap;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobCache;
import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobClientConfig;
import io.dialob.client.api.DialobErrorHandler;
import io.dialob.client.api.DialobStore;
import io.dialob.client.api.ImmutableDialobClientConfig;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.executor.QuestionnaireExecutorBuilderImpl;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.client.spi.program.ProgramBuilderImpl;
import io.dialob.client.spi.support.DialobAssert;
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.compiler.DialobSessionUpdateHook;
import io.dialob.program.DialobSessionEvalContextFactory;
import io.dialob.rule.parser.function.FunctionRegistry;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class DialobClientImpl implements DialobClient {
  private final DialobClientConfig config; 
  
  @Override
  public ProgramBuilder program() {
    return new ProgramBuilderImpl(config.getCompiler());
  }
  @Override
  public QuestionnaireExecutorBuilder executor(ProgramEnvir envir) {
    return new QuestionnaireExecutorBuilderImpl(envir, config);
  }
  @Override
  public EnvirBuilder envir() {
    return new DialobClientEnvirBuilder(new DialobProgramEnvirFactory(config));
  }
  @Override
  public DialobClientConfig getConfig() {
    return config;
  }
  @Override
  public DialobStore store() {
    return config.getStore();
  }
  
  @Override
  public RepoBuilder repo() {
    return new DialobRepoBuilderImpl(config);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Accessors(fluent = true, chain = true)
  @Data
  public static class Builder {  
    private FunctionRegistry functionRegistry;
    private AsyncFunctionInvoker asyncFunctionInvoker;
    private QuestionnaireEventPublisher eventPublisher;

    private DialobStore store;
    private @Nullable ObjectMapper objectMapper;
    private @Nullable DialobSessionUpdateHook dialobSessionUpdateHook;
    private @Nullable DialobCache cache;
    private @Nullable Clock clock;
    private @Nullable DialobErrorHandler errorHandler;
    
    
    public DialobClientImpl build() {
      //DialobAssert.notNull(store, () -> "store must be defined!");
      DialobAssert.notNull(functionRegistry, () -> "functionRegistry must be defined!");
      DialobAssert.notNull(eventPublisher, () -> "eventPublisher must be defined!");
      DialobAssert.notNull(asyncFunctionInvoker, () -> "asyncFunctionInvoker must be defined!");      

      DialobCache cache = this.cache;
      if(cache == null) {
        //cache = DialobEhCache.builder().build(store.getRepoName());
        cache = DialobEhCache.builder().build("inmem");
      }
      
      Clock clock = this.clock;
      if(clock == null) {
        clock = Clock.systemDefaultZone();
      }

      ObjectMapper objectMapper = this.objectMapper;
      if(objectMapper == null) {
        objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
      }
      
      DialobStore store = this.store;
      if(store == null) {
        store = new DialobMemoryStore(new HashMap<>());
      }
      
      DialobErrorHandler errorHandler = this.errorHandler;
      if(errorHandler == null) {
        errorHandler = new DialobErrorHandlerImpl(true);
      }
      
      final var config = ImmutableDialobClientConfig.builder()
          .asyncFunctionInvoker(asyncFunctionInvoker)
          .factory(new DialobSessionEvalContextFactory(functionRegistry, clock, dialobSessionUpdateHook))
          .store(store)
          .cache(cache)
          .errorHandler(errorHandler)
          .eventPublisher(eventPublisher)
          .mapper(new DialobTypesMapperImpl(objectMapper))
          .compiler(new DialobProgramFromFormCompiler(functionRegistry))
          .build();
      

      return new DialobClientImpl(config);
    }
  }
}
