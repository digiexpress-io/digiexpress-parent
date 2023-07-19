package io.dialob.spring.composer;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import org.immutables.value.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobStore;
import io.dialob.client.spi.DialobClientImpl;
import io.dialob.client.spi.DialobMemoryStore;
import io.dialob.client.spi.event.EventPublisher;
import io.dialob.client.spi.event.QuestionnaireEventPublisher;
import io.dialob.client.spi.function.AsyncFunctionInvoker;
import io.dialob.client.spi.function.FunctionRegistryImpl;
import io.dialob.rule.parser.function.DefaultFunctions;
import io.dialob.spring.composer.config.FileConfig;
import io.dialob.spring.composer.config.FileConfigBean;
import io.dialob.spring.composer.config.PgConfig;
import io.dialob.spring.composer.config.PgConfigBean;
import io.dialob.spring.composer.config.UiConfigBean;
import io.dialob.spring.composer.controllers.DialobComposerServiceController;
import io.dialob.spring.composer.controllers.DialobComposerUiController;
import io.dialob.spring.composer.controllers.DialobComposerUiRedirectController;
import io.dialob.spring.composer.controllers.util.ControllerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(name = "dialob.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({
  FileConfigBean.class,
  PgConfigBean.class,
  UiConfigBean.class})
@Import({
  FileConfig.class, 
  PgConfig.class })
@Slf4j
public class ComposerAutoConfiguration {
  
  @org.springframework.beans.factory.annotation.Value("${dialob.formdb.inmemory.path:classpath*:assets/}")
  private String inmemoryPath;
  @org.springframework.beans.factory.annotation.Value("${server.servlet.context-path:}")
  private String contextPath;
  
//@Bean
//public AssetExceptionMapping assetExceptionMapping() {
//  return new AssetExceptionMapping();
//}

  @Bean
  @ConditionalOnProperty(name = "dialob.formdb.inmemory.enabled", havingValue = "true")
  public DialobStore dialobStore(ObjectMapper objectMapper) {
    return DialobMemoryStore.builder().objectMapper(objectMapper).path(inmemoryPath).build();
  }

  @ConditionalOnProperty(name = "dialob.composer.ui.enabled", havingValue = "true")
  @Bean
  public DialobComposerUiController dialobComposerUiController(UiConfigBean composerConfig, Optional<SpringIdeTokenSupplier> token) {
    final var config = ControllerUtil.ideOnClasspath(contextPath);
    LOGGER.info("Dialob, UI Controller: " + config.getMainJs());
    return new DialobComposerUiController(composerConfig, config, token);
  }
  @ConditionalOnProperty(name = {"dialob.composer.ui.enabled", "dialob.composer.ui.redirect"}, havingValue = "true")
  @Bean
  public DialobComposerUiRedirectController dialobUIRedirectController(UiConfigBean composerConfig) {
    LOGGER.info("Dialob, UI Redirect: UP");
    return new DialobComposerUiRedirectController(composerConfig);
  }
  @ConditionalOnProperty(name = "dialob.composer.service.enabled", havingValue = "true")
  @Bean
  public DialobComposerServiceController dialobComposerServiceController(
      DialobClient client, ObjectMapper objectMapper, ApplicationContext ctx) {
    return new DialobComposerServiceController(client, objectMapper, ctx);
  }
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
    LOGGER.info("Dialob, Composer Jackson Modules: UP");
    return builder -> builder.modules(new GuavaModule(), new JavaTimeModule(), new Jdk8Module());
  }
  
  
  @Bean
  public DialobClient dialobClient(
      ApplicationEventPublisher springAppEventPublisher,
      TaskExecutor springTaskExecutor,
      ObjectMapper objectMapper, 
      Optional<DialobStore> store) {
    
    final var functionRegistry = new FunctionRegistryImpl();
    final var defaultFunctions = new DefaultFunctions(functionRegistry);
    final var appEventPublisher = new QuestionnaireSessionEventPublisher(springTaskExecutor, springAppEventPublisher);
    final var eventPublisher = new QuestionnaireEventPublisher(appEventPublisher);
    final var asyncFunctionInvoker = new AsyncFunctionInvoker(functionRegistry);
    
    if(store.isEmpty()) {
      store = Optional.of(DialobMemoryStore.builder()
          .objectMapper(objectMapper)
          .path(inmemoryPath)
          .build());
    }
    
    
    return DialobClientImpl.builder()
        .store(store.get())
        .objectMapper(objectMapper)
        .eventPublisher(eventPublisher)
        .asyncFunctionInvoker(asyncFunctionInvoker)
        .functionRegistry(functionRegistry)
        .build();
  }
  
  
  @RequiredArgsConstructor
  public static class QuestionnaireSessionEventPublisher implements EventPublisher {
    private final TaskExecutor taskExecutor;
    private final ApplicationEventPublisher delegate;

    @Override
    public void publish(@Nonnull Event event) {
      taskExecutor.execute(() -> delegate.publishEvent(event));
    }
  }
  @FunctionalInterface
  public interface SpringIdeTokenSupplier {
    Optional<IdeToken> get(HttpServletRequest request);
  }
  
  @Value.Immutable
  public interface IdeToken {
    String getKey();
    String getValue();
  }

  public String getInmemoryPath() {
    return inmemoryPath;
  }
  public void setInmemoryPath(String inmemoryPath) {
    this.inmemoryPath = inmemoryPath;
  }
  
}
