package io.dialob.client.tests.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;

import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.dialob.compiler.DialobProgramFromFormCompiler;
import io.dialob.program.DialobSessionEvalContextFactory;
import io.dialob.program.EvalContext;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.function.DefaultFunctions;
import io.dialob.rule.parser.function.FunctionRegistry;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

public class DialobClientTestImpl extends DialobClientImpl {

  private static DialobClientTestImpl INSTANCE = DialobClientTestImpl.builder().build();
  
  public DialobClientTestImpl(DialobClientConfig config) {
    super(config);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public DialobClientTestImpl with(QuestionnaireEventPublisher publisher) {
    final var newConfig = ImmutableDialobClientConfig.builder().from(this.getConfig()).eventPublisher(publisher).build();
    return new DialobClientTestImpl(newConfig);
  }
  
  public static DialobClientTestImpl get() {
    return INSTANCE;
  }
  
  @Accessors(fluent = true, chain = true)
  @Data
  @EqualsAndHashCode(callSuper = false)
  public static class Builder extends DialobClientImpl.Builder {  
    public final static ObjectMapper MAPPER = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module());
    
    public DialobClientTestImpl build() {
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
        objectMapper = MAPPER;
      }
      
      QuestionnaireEventPublisher eventPublisher = this.eventPublisher();
      if(eventPublisher == null) {
        eventPublisher = Mockito.mock(QuestionnaireEventPublisher.class);
      }
      
      DialobStore store = this.store();
      if(store == null) {
        store = Mockito.mock(DialobStore.class);
      }
            
      AsyncFunctionInvoker asyncFunctionInvoker = asyncFunctionInvoker();
      if(asyncFunctionInvoker == null) {
        asyncFunctionInvoker = mock(AsyncFunctionInvoker.class);
        final var visitor = mock(EvalContext.UpdatedItemsVisitor.AsyncFunctionCallVisitor.class);
        when(asyncFunctionInvoker.createVisitor(Mockito.any())).thenReturn(visitor);        
      }
      
      FunctionRegistry functionRegistry = functionRegistry();
      if(functionRegistry == null) {
        functionRegistry = createFunctionRegistry();
      } 
      
      DialobErrorHandler errorHandler = errorHandler();
      if(errorHandler == null) {
        errorHandler = new DialobErrorHandlerImpl(true);
      }
      
      
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
      

      return new DialobClientTestImpl(config);
    }
  }

  
  private static FunctionRegistry createFunctionRegistry() {
    try {
      final FunctionRegistry functionRegistry = mock(FunctionRegistry.class);
  
      when(functionRegistry.returnTypeOf("today")).thenReturn(ValueType.DATE);
      doAnswer(invocationOnMock -> {
        ((FunctionRegistry.FunctionCallback) invocationOnMock.getArgument(0)).succeeded(LocalDate.now());
        return null;
      }).when(functionRegistry).invokeFunction(any(FunctionRegistry.FunctionCallback.class), eq("today"));
  
      when(functionRegistry.returnTypeOf(eq("isLyt"),eq(ValueType.STRING))).thenReturn(ValueType.BOOLEAN);
      doAnswer(invocationOnMock -> {
        ((FunctionRegistry.FunctionCallback) invocationOnMock.getArgument(0)).succeeded(DefaultFunctions.isLyt(invocationOnMock.getArgument(1)));
        return null;
      }).when(functionRegistry).invokeFunction(any(FunctionRegistry.FunctionCallback.class), eq("isLyt"));
  
      when(functionRegistry.returnTypeOf(eq("isHetu"),eq(ValueType.STRING))).thenReturn(ValueType.BOOLEAN);
      doAnswer(invocationOnMock -> {
        ((FunctionRegistry.FunctionCallback) invocationOnMock.getArgument(0)).succeeded(DefaultFunctions.isHetu(invocationOnMock.getArgument(1)));
        return null;
      }).when(functionRegistry).invokeFunction(any(FunctionRegistry.FunctionCallback.class), eq("isHetu"));
  
      when(functionRegistry.returnTypeOf(eq("count"),eq(ValueType.arrayOf(ValueType.INTEGER)))).thenReturn(ValueType.INTEGER);
      doAnswer(invocationOnMock -> {
        ((FunctionRegistry.FunctionCallback) invocationOnMock.getArgument(0)).succeeded(DefaultFunctions.count(invocationOnMock.getArgument(1)));
        return null;
      }).when(functionRegistry).invokeFunction(any(FunctionRegistry.FunctionCallback.class), eq("count"));
  
  
      when(functionRegistry.returnTypeOf("lengthOf", ValueType.STRING)).thenReturn(ValueType.INTEGER);
      doAnswer(invocation -> {
        FunctionRegistry.FunctionCallback cb = invocation.getArgument(0);
        String string = invocation.getArgument(2);
        if (string == null) {
          cb.succeeded(0);
        } else {
          cb.succeeded(string.length());
        }
        return null;
      }).when(functionRegistry).invokeFunction(any(), eq("lengthOf"), any());
  
      when(functionRegistry.returnTypeOf("count", ValueType.arrayOf(ValueType.STRING))).thenReturn(ValueType.INTEGER);
      return functionRegistry;
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
